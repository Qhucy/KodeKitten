package com.sylink.account;

import com.sylink.KodeKitten;
import lombok.NonNull;

import javax.annotation.Nullable;
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
    // The statement used to create a new table if one does not exist.
    private final static String SQL_TABLE = """
            CREATE TABLE IF NOT EXISTS accounts
            (
                id BIGINT NOT NULL UNIQUE,
                permissions TEXT NOT NULL DEFAULT '',
                roles TEXT NOT NULL DEFAULT '',
                balance DOUBLE NOT NULL DEFAULT 0.0
            );
            """;
    private final static String SQL_DELETE = "DELETE FROM accounts WHERE id = %d";

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
     *
     * @param createNewIfNotFound Creates a new account if it doesn't exist in the database.
     */
    public Account getAccount(final long discordId, boolean createNewIfNotFound)
    {
        if (existsInMemory(discordId))
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
        final boolean existsInDatabase = account.existsInDatabase(connection);

        if (!existsInDatabase && !createNewIfNotFound)
        {
            return null;
        }

        if (existsInDatabase && !account.loadFromDatabase(connection))
        {
            KodeKitten.logWarning(String.format("Unable to load account data for discord id %d in the accounts " +
                    "database.", discordId));
            return null;
        }

        accounts.put(discordId, account);

        return account;
    }

    public Account getAccount(final long discordId)
    {
        return getAccount(discordId, true);
    }

    /**
     * Loads an account's data from the database.
     *
     * @return True if the account was loaded.
     */
    public boolean loadFromDatabase(final long discordId)
    {
        final Account account;

        if (existsInMemory(discordId))
        {
            account = getAccount(discordId);
            account.loadFromDatabase(connection);

            return true;
        }
        else
        {
            account = getAccount(discordId, false);

            return account != null;
        }
    }

    public boolean loadFromDatabase(@NonNull final Account account)
    {
        return loadFromDatabase(account.getDiscordId());
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

    public void flushFromMemory(@NonNull final Account account)
    {
        flushFromMemory(account, true);
    }

    /**
     * Removes a given account from memory.
     */
    public void deleteFromMemory(final long discordId)
    {
        accounts.remove(discordId);
    }

    public void deleteFromMemory(@NonNull final Account account)
    {
        deleteFromMemory(account.getDiscordId());
    }

    /**
     * Deletes the given account id from the database.
     */
    public void deleteFromDatabase(final long discordId)
    {
        if (existsInDatabase(discordId))
        {
            executeQuery(String.format(SQL_DELETE, discordId));
        }
    }

    public void deleteFromDatabase(@NonNull final Account account)
    {
        deleteFromDatabase(account.getDiscordId());
    }

    /**
     * Deletes the given account id from the database AND from memory.
     */
    public void delete(final long discordId)
    {
        deleteFromDatabase(discordId);
        deleteFromMemory(discordId);
    }

    public void delete(@NonNull final Account account)
    {
        delete(account.getDiscordId());
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
        final Account account = getAccount(discordId, false);

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

    /**
     * @return True if the given discord id exists in the SQL Database.
     */
    public boolean existsInDatabase(final long discordId)
    {
        final Account account = getAccount(discordId, false);

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
     * @return True if the account exists in memory OR in the SQL Database.
     */
    public boolean exists(final long discordId)
    {
        return existsInMemory(discordId) || existsInDatabase(discordId);
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
    public boolean openDatabaseConnection(@NonNull final String databaseUrl, @Nullable final String sqlTableStatement)
    {
        try
        {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection(databaseUrl);
            connectionLastActivity = System.currentTimeMillis();

            if (sqlTableStatement != null)
            {
                try (final Statement statement = connection.createStatement())
                {
                    statement.executeUpdate(sqlTableStatement);
                }
            }
            return true;
        }
        catch (final SQLException sqlException)
        {
            KodeKitten.logSevere("Unable to access database");
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

    public boolean openDatabaseConnection(@NonNull final String databaseUrl)
    {
        return openDatabaseConnection(databaseUrl, SQL_TABLE);
    }

    public boolean openDatabaseConnection()
    {
        return openDatabaseConnection(DATABASE_URL, SQL_TABLE);
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
     * Executes a given query to the SQL Database.
     */
    public void executeQuery(@NonNull final String sqlQuery)
    {
        if (connection == null)
        {
            return;
        }

        try (final Statement statement = connection.createStatement())
        {
            statement.executeQuery(sqlQuery);
        }
        catch (final SQLException exception)
        {
            exception.printStackTrace();
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