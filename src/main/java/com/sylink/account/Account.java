package com.sylink.account;

import com.sylink.KodeKitten;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Class that contains all data about a user account.
 */
public class Account
{

    @Getter(AccessLevel.PUBLIC)
    private final long discordId;
    // Last activity time to track how long an account has been inactive in memory.
    private long lastActivity = 0;

    @Getter(AccessLevel.PUBLIC)
    private double balance = 0.0;

    // Whether account information has been changed and needs to be synced to the database.
    private boolean needsToSync = false;

    protected Account(final long discordId)
    {
        this.discordId = discordId;
    }

    /**
     * Sets the last activity time to the current time.
     */
    public final void bumpLastActivityTime()
    {
        lastActivity = System.currentTimeMillis();
    }

    /**
     * Returns true if the last activity time of the account was over 10 minutes ago.
     */
    public final boolean isInactive()
    {
        int seconds = (int) ((System.currentTimeMillis() - lastActivity) / 1000);

        return seconds > 600;
    }

    /**
     * Returns true if the last activity time of the account was over 1 hour ago.
     */
    public final boolean isDead()
    {
        int seconds = (int) ((System.currentTimeMillis() - lastActivity) / 1000);

        return seconds > 3600;
    }

    public final void setBalance(final double balance)
    {
        this.balance = balance;
        this.needsToSync = true;
    }

    public final void addBalance(final double balance)
    {
        this.balance += balance;
        this.needsToSync = true;
    }

    public final void removeBalance(final double balance)
    {
        // Balance cannot be less than 0.
        this.balance = Math.max(0, this.balance - balance);
        this.needsToSync = true;
    }

    /**
     * Resets the account's balance to 0.0
     */
    public final void resetBalance()
    {
        this.balance = 0.0;
        this.needsToSync = true;
    }

    /**
     * Loads all account data from the database connection and returns whether the operation was successful.
     */
    public boolean loadFromDatabase()
    {
        final Connection connection = AccountManager.getConnection();

        if (connection == null)
        {
            return false;
        }

        try(final Statement statement = connection.createStatement();
            final ResultSet resultSet = statement.executeQuery(String.format("""
                    SELECT
                        balance
                    FROM
                        accounts
                    WHERE
                        id=%d;
                    """, discordId)))
        {
            if (resultSet.next())
            {
                balance = resultSet.getDouble("balance");
            }

            lastActivity = System.currentTimeMillis();
            return true;
        } catch (final SQLException sqlException)
        {
            sqlException.printStackTrace();
            return false;
        }
    }

    /**
     * Saves all data from this object to the account's column in the database.
     *
     * @return False if there was an error saving the data.
     */
    public boolean saveToDatabase()
    {
        if (!needsToSync)
            return true;

        final Connection connection = AccountManager.getConnection();

        try (final Statement statement = connection.createStatement())
        {
            // Insert into database as a new column.
            if (!existsInDatabase(connection))
            {
                statement.executeUpdate(String.format("INSERT INTO accounts (%s) VALUES(%g)", discordId, balance));
            }
            // Update the existing column with new data.
            else
            {
                statement.executeUpdate(String.format("""
                        UPDATE table
                        SET balance = %g
                        WHERE id = %d
                        """, balance, discordId));
            }

            return true;
        } catch (final SQLException sqlException)
        {
            KodeKitten.logSevere("Unable to save account data for discord id " + discordId);
            sqlException.printStackTrace();
            return false;
        }
    }

    /**
     * Returns true if the account exists as a column in the database.
     */
    public boolean existsInDatabase(@NonNull final Connection connection)
    {
        try (final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery("SELECT id FROM accounts WHERE id=" + discordId))
        {
            return resultSet.next();
        } catch (final SQLException sqlException)
        {
            return false;
        }
    }

    /**
     * Returns true if the account data was loaded from the database.
     */
    public boolean isLoaded()
    {
        // Last activity time is 0 when first loaded and non-zero when loaded.
        return lastActivity != 0;
    }

}