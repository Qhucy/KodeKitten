package com.sylink.account;

import com.sylink.KodeKitten;
import lombok.NonNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class that handles the management of Accounts stored and processed in memory.
 */
public final class AccountManager
{

    private static AccountManager accountManager = null;
    // JDBC URL to the accounts database.
    private final static String DATABASE_URL = "jdbc:sqlite:database.db";
    private final static String SQL_TABLE = """
            CREATE TABLE IF NOT EXISTS accounts
            (
                id BIGINT NOT NULL UNIQUE,
                permissions TEXT NOT NULL DEFAULT '',
                roles TEXT NOT NULL DEFAULT '',
                balance DOUBLE NOT NULL DEFAULT 0.0
            );
            """;

    public static AccountManager getInstance()
    {
        if (accountManager == null)
        {
            accountManager = new AccountManager();
        }

        return accountManager;
    }

    // Connection to the accounts database.
    private Connection connection = null;
    // Connection activity time to track how long a connection has been inactive.
    private long connectionLastActivity = System.currentTimeMillis();
    // Map the stores the Discord Id associated with its account.
    private final Map<Long, Account> accounts = new HashMap<>();

    /**
     * Returns the account from its discord id.
     * If it does not exist in the internal database, it loads it from the database file.
     */
    public Account getAccount(final long discordId)
    {
        if (accounts.containsKey(discordId))
        {
            final Account account = accounts.get(discordId);

            account.bumpLastActivityTime();
            return account;
        }

        final Connection connection = getConnection();

        if (connection == null)
        {
            return null;
        }

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
     * Saves an account to the database and removes it from memory.
     *
     * @param keepIfUnableToSave If true, doesn't remove the account from memory if it was unable to be saved to the
     *                           database.
     */
    public void flushFromMemory(@NonNull final Account account, final boolean keepIfUnableToSave)
    {
        if (saveToDatabase(account) || !keepIfUnableToSave)
        {
            accounts.remove(account.getDiscordId());
        }
    }

    /**
     * Saves a given account to the SQL database.
     *
     * @return True if the account was successfully saved to the database.
     */
    public boolean saveToDatabase(@NonNull final Account account)
    {
        if (connection == null)
        {
            KodeKitten.logWarning(String.format("Unable to save account %d to the database as there is no connection "
                    + "to the database", account.getDiscordId()));
            return false;
        }

        return account.saveToDatabase(connection);
    }

    public boolean saveToDatabase(final long discordId)
    {
        final Account account = getAccount(discordId);

        return account != null && saveToDatabase(account);
    }

    /**
     * @return True if the given account exists as a column in the SQL database.
     */
    public boolean existsInDatabase(@NonNull final Account account)
    {
        if (connection == null)
        {
            KodeKitten.logWarning(String.format("Unable to check if account %d exists in database with an inactive " + "connection", account.getDiscordId()));
            return false;
        }

        return account.existsInDatabase(connection);
    }

    public boolean existsInDatabase(final long discordId)
    {
        final Account account = getAccount(discordId);

        return account != null && existsInDatabase(account);
    }

    /**
     * @return True if the given discord id exists in local memory.
     */
    public boolean existsInMemory(final long discordId)
    {
        for (var entry : accounts.entrySet())
        {
            if (entry.getKey() == discordId)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the opened connection to the database.
     * If the connection is not open then it opens a connection.
     */
    public Connection getConnection()
    {
        if (connection == null && !openDatabaseConnection())
        {
            return null;
        }

        // Connection has been accessed, so we reset connection activity time.
        connectionLastActivity = System.currentTimeMillis();

        return connection;
    }

    /**
     * Returns whether a successful connection with the accounts database was created.
     */
    public boolean openDatabaseConnection()
    {
        try
        {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection(DATABASE_URL);
            connectionLastActivity = System.currentTimeMillis();

            try (final Statement statement = connection.createStatement())
            {
                statement.executeUpdate(SQL_TABLE);
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
    public void closeDatabaseConnection()
    {
        if (connection == null)
        {
            return;
        }

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
    public void cleanupConnectionInactivity()
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
    public void cleanupAccountInactivity()
    {
        final Connection connection = getConnection();

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