package com.sylink.account;

import com.sylink.KodeKitten;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that contains all data about a user account.
 */
public class Account
{

    private final long discordId;

    private double balance = 0.0;

    // The list of statements to run to update user data in the SQL database.
    private final List<String> updateStatements = new ArrayList<>();

    public Account(final long discordId)
    {
        this.discordId = discordId;
    }

    public final long getDiscordId()
    {
        return discordId;
    }

    public final double getBalance()
    {
        return balance;
    }

    public final void setBalance(final double balance)
    {
        this.balance = balance;
    }

    public final void addBalance(final double balance)
    {
        this.balance += balance;
    }

    public final void removeBalance(final double balance)
    {
        // Balance cannot be less than 0.
        this.balance = Math.max(0, this.balance - balance);
    }

    /**
     * Resets the account's balance to 0.0
     */
    public final void resetBalance()
    {
        this.balance = 0.0;
    }

    /**
     * Loads all account data from the database connection and returns whether the operation was successful.
     */
    public boolean loadFromDatabase()
    {
        if (AccountManager.getConnection() == null)
        {
            return false;
        }

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

            return true;
        } catch (final SQLException sqlException)
        {
            sqlException.printStackTrace();
            return false;
        }
    }

    /**
     * Saves all data from this object to the account's column in the database.
     */
    public boolean saveToDatabase()
    {

        try (final Statement statement = AccountManager.getConnection().createStatement())
        {
            // Insert into database as a new column.
            if (!existsInDatabase())
            {
                statement.executeUpdate(String.format("INSERT INTO accounts (id) VALUES(%d)", discordId));
            }

            // Update the existing column with new data.
            for (final String sql : updateStatements)
            {
                statement.executeUpdate(sql);
            }

            updateStatements.clear();
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
        try (final Statement statement = AccountManager.getConnection().createStatement();
             final ResultSet resultSet = statement.executeQuery("SELECT id FROM accounts WHERE id=" + discordId))
        {
            return resultSet.next();
        } catch (final SQLException sqlException)
        {
            KodeKitten.logSevere("Unable to find discord id " + discordId + " in the database.");
            sqlException.printStackTrace();
            return false;
        }
    }

}