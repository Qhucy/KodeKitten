package com.sylink.account;

import com.sylink.KodeKitten;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that handles the management of Accounts stored and processed in memory.
 */
public final class AccountManager
{

    // JDBC URL to the accounts database.
    private final static String DATABASE_URL = "jdbc:sqlite:database.db";
    // Connection to the accounts database.
    private static Connection connection = null;
    // Connection activity time to track how long a connection has been inactive.
    private static long connectionLastActivity = System.currentTimeMillis();
    // Map the stores the Discord Id associated with its account.
    private final static Map<Long, Account> accounts = new HashMap<>();

    /**
     * Returns the account from its discord id.
     * If it does not exist in the internal database, it loads it from the database file.
     */
    public static Account getAccount(final long discordId)
    {
        if (accounts.containsKey(discordId))
            return accounts.get(discordId);

        if (connection == null)
            return null;

        final Account account = new Account(discordId);

        if (account.existsInDatabase(connection) && !account.loadFromDatabase(connection))
        {
            KodeKitten.logWarning(String.format("Unable to load account data for discord id %d in the accounts " +
                    "database.", discordId));
            return null;
        }

        accounts.put(discordId, account);

        return account;
    }

    /**
     * Returns the opened connection to the database.
     * If the connection is not open then it opens a connection.
     */
    public static Connection getConnection()
    {
        if (connection == null && !openDatabaseConnection())
            return null;

        // Connection has been accessed so we reset connection activity time.
        connectionLastActivity = System.currentTimeMillis();

        return connection;
    }

    /**
     * Returns whether a successful connection with the accounts database was created.
     */
    public static boolean openDatabaseConnection()
    {
        try
        {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection(DATABASE_URL);
            connectionLastActivity = System.currentTimeMillis();

            try (final Statement statement = connection.createStatement())
            {
                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS accounts
                        (
                            id BIGINT NOT NULL UNIQUE,
                            balance DOUBLE NOT NULL DEFAULT 0.0
                        );
                        """);
            }
            return true;
        }
        catch (final SQLException sqlException)
        {
            KodeKitten.logSevere("Unable to access 'database.db' database");
            sqlException.printStackTrace();
            return false;
        }
         catch (final ClassNotFoundException classNotFoundException)
         {
             KodeKitten.logSevere("Unable to access JDBC SQLite drivers");
             classNotFoundException.printStackTrace();
             return false;
         }
    }

    /**
     * Closes the database connection.
     */
    public static void closeDatabaseConnection()
    {
        try
        {
            connection.close();
        }
        catch (final SQLException sqlException)
        {
            KodeKitten.logSevere("Unable to properly close database connection.");
            sqlException.printStackTrace();
        }
        finally
        {
            connection = null;
        }
    }

    /**
     * Closes the account database connection if it has been inactive for over 5 minutes.
     */
    public static void checkConnection()
    {
        if (connection == null)
        {
            return;
        }

        int seconds = (int) ((System.currentTimeMillis() - connectionLastActivity) / 1000);

        // Been over 5 minutes.
        if (seconds > 300)
        {
            closeDatabaseConnection();
        }
    }

    /**
     * Removes accounts flagged as inactive from internal memory.
     * Saves all account data before removing them.
     */
    public static void checkAccounts()
    {
        for (var entry : accounts.entrySet())
        {
            final Account account = entry.getValue();

            if (account.isInactive())
            {
                // If there is no active connection we only remove the account from memory if it is dead.
                if (connection == null)
                {
                    if (account.isDead())
                    {
                        accounts.remove(entry.getKey());
                    }
                }
                // If there is an active connection we attempt to save the account to the database before removing it
                // from memory.
                else
                {
                    if (account.saveToDatabase(connection) || account.isDead())
                    {
                        accounts.remove(entry.getKey());
                    }
                }
            }
        }
    }

}