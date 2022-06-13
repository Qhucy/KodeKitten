package com.sylink.account;

import com.sylink.KodeKitten;
import lombok.AccessLevel;
import lombok.Getter;

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
    // Whether the Account has been loaded from the database.
    @Getter(AccessLevel.PUBLIC)
    private boolean loaded = false;
    // Last activity time to track how long an account has been inactive in memory.
    @Getter(AccessLevel.PUBLIC)
    private long lastActivityTime = 0;

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
        lastActivityTime = System.currentTimeMillis();
    }

    /**
     * Returns true if the last activity time of the account was over 10 minutes ago.
     */
    public final boolean isInactive()
    {
        int seconds = (int) ((System.currentTimeMillis() - lastActivityTime) / 1000);

        return seconds > 600;
    }

    /**
     * Returns true if the last activity time of the account was over 1 hour ago.
     */
    public final boolean isDead()
    {
        int seconds = (int) ((System.currentTimeMillis() - lastActivityTime) / 1000);

        return seconds > 3600;
    }

    /**
     * @return True if Account data has been changed and needs to be saved to the database.
     */
    public boolean needsToSync()
    {
        return needsToSync;
    }

    public final void setBalance(final double balance)
    {
        // Balance cannot be less than 0.
        this.balance = Math.max(0, balance);
        this.needsToSync = true;
    }

    public final void addBalance(final double balance)
    {
        // Balance cannot be less than 0.
        this.balance = Math.max(0, this.balance + balance);

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
     * Loads all account data from the database connection and returns whether the data was loaded.
     */
    public boolean loadFromDatabase()
    {
        if (AccountManager.getConnection() == null)
            return false;

        try(final Statement statement = AccountManager.getConnection().createStatement();
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

            lastActivityTime = System.currentTimeMillis();

            this.loaded = true;
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
     * @return True if data was saved to the database if needed.
     */
    public boolean saveToDatabase()
    {
        if (!needsToSync)
            return true;

        if (AccountManager.getConnection() == null)
            return false;

        try (final Statement statement = AccountManager.getConnection().createStatement())
        {
            // Insert into database as a new column.
            if (!existsInDatabase())
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

            // Data no longer needs to be updated.
            this.needsToSync = false;
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
    public boolean existsInDatabase()
    {
        if (AccountManager.getConnection() == null)
            return false;

        try (final Statement statement = AccountManager.getConnection().createStatement();
             final ResultSet resultSet = statement.executeQuery("SELECT id FROM accounts WHERE id=" + discordId))
        {
            return resultSet.next();
        } catch (final SQLException sqlException)
        {
            return false;
        }
    }

}