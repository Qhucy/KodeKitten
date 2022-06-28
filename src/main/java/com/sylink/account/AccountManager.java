package com.sylink.account;

import com.sylink.KodeKitten;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class that handles the management of Accounts stored and processed in memory.
 */
public final class AccountManager
{

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
    // The SQL query used to test whether an account exists in the database.
    private static final String SQL_EXISTS_QUERY = "SELECT id FROM accounts WHERE id=%d";
    // The SQL query used to insert new account data in to the database.
    private static final String SQL_INSERT_QUERY = """
            INSERT INTO accounts
            (id,permissions,roles,balance)
            VALUES(%s,%s,%s,%g)
            """;
    // The SQL query used to update existing column data for an account.
    private static final String SQL_UPDATE_QUERY = """
            UPDATE accounts
            SET permissions = %s,
                roles = %s,
                balance = %g
            WHERE id = %d
            """;
    // The SQL query used to load account data from the database.
    private static final String SQL_LOAD_QUERY = """
            SELECT
                permissions,
                roles,
                balance
            FROM
                accounts
            WHERE
                id=%d;
            """;
    // The SQL query used to delete an account from the database.
    private final static String SQL_DELETE = "DELETE FROM accounts WHERE id = %d";

    private static AccountManager accountManager = null;

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
    @Getter(AccessLevel.PUBLIC)
    private long connectionLastActivity = System.currentTimeMillis();
    // Map the stores the Discord Id associated with its account.
    private final Map<Long, Account> accounts = new HashMap<>();

    /**
     * Sets the time the connection was last accessed.
     */
    void setConnectionLastActivity(final long connectionLastActivity)
    {
        this.connectionLastActivity = connectionLastActivity;
    }

    /**
     * Returns the account from its discord id.
     * If it does not exist in the internal database, it loads it from the database file.
     *
     * @param createNewIfNotFound Creates a new account if it doesn't exist in the database.
     */
    public Account getAccount(final long discordId, boolean createNewIfNotFound)
    {
        // Try and retrieve the account from local memory.
        if (existsInMemory(discordId))
        {
            final Account account = accounts.get(discordId);

            // The account has been accessed, so we bump it's last activity time.
            account.bumpLastActivityTime();
            return account;
        }

        // Try and retrieve the account from the database.
        final boolean existsInDatabase = existsInDatabase(discordId);

        if (!existsInDatabase && !createNewIfNotFound)
        {
            return null;
        }

        final Account account = new Account(discordId);

        if (existsInDatabase && !loadFromDatabase(account))
        {
            KodeKitten.logWarning(String.format("""
                    Unable to load account data for discord id %d \
                    in the accounts database.""", discordId));
            return null;
        }

        accounts.put(discordId, account);

        return account;
    }

    /**
     * @return The account attached to the Discord Id, creating a new account by default if it is not found.
     */
    public Account getAccount(final long discordId)
    {
        return getAccount(discordId, true);
    }

    /**
     * @return The opened connection to the database.
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
     * @return Whether a successful connection with the accounts database was created.
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

    /**
     * @return Whether a successful connection with the accounts database was created.
     * Uses the default SQL_TABLE statement.
     */
    public boolean openDatabaseConnection(@NonNull final String databaseUrl)
    {
        return openDatabaseConnection(databaseUrl, SQL_TABLE);
    }

    /**
     * @return Whether a successful connection with the accounts database was created.
     * Uses the default DATABASE_URL and SQL_TABLE statements.
     */
    public boolean openDatabaseConnection()
    {
        return openDatabaseConnection(DATABASE_URL, SQL_TABLE);
    }

    /**
     * Executes given queries to the SQL Database.
     */
    public void executeQuery(@NonNull final String... sqlQueries)
    {
        final Connection connection = getConnection();

        if (connection == null)
        {
            return;
        }

        try (final Statement statement = connection.createStatement())
        {
            for (final String sqlQuery : sqlQueries)
            {
                statement.execute(sqlQuery);
            }
        }
        catch (final SQLException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * @return True if the given discord id exists in the SQL Database.
     */
    public boolean existsInDatabase(final long discordId)
    {
        final Connection connection = getConnection();

        if (connection == null)
        {
            KodeKitten.logWarning(String.format("""
                    Unable to check if account %d exists in \
                    database with an inactive connection
                    """, discordId));
            return false;
        }

        try (final Statement statement = connection.createStatement(); final ResultSet resultSet =
                statement.executeQuery(String.format(SQL_EXISTS_QUERY, discordId)))
        {
            return resultSet.next();
        }
        catch (final SQLException sqlException)
        {
            return false;
        }
    }

    /**
     * @return True if the given account exists as a row in the database.
     */
    public boolean existsInDatabase(@NonNull final Account account)
    {
        return existsInDatabase(account.getDiscordId());
    }

    /**
     * @return True if the given discord id exists in local memory.
     */
    public boolean existsInMemory(final long discordId)
    {
        return accounts.containsKey(discordId);
    }

    /**
     * @return True if the account exists in memory OR in the SQL Database.
     */
    public boolean exists(final long discordId)
    {
        return existsInMemory(discordId) || existsInDatabase(discordId);
    }

    /**
     * Saves a given account to the SQL database.
     *
     * @return True if the account was successfully saved to the database.
     */
    public boolean saveToDatabase(@NonNull final Account account)
    {
        final Connection connection = getConnection();

        if (connection == null)
        {
            KodeKitten.logWarning(String.format("""
                    Unable to save account %d to the database as \
                    there is no connection to the database
                    """, account.getDiscordId()));
            return false;
        }

        final boolean existsInDatabase = existsInDatabase(account.getDiscordId());

        if (existsInDatabase && !account.needsToSync())
        {
            return false;
        }

        try (final Statement statement = connection.createStatement())
        {
            // Insert into database as a new column.
            if (!existsInDatabase)
            {
                statement.executeUpdate(String.format(SQL_INSERT_QUERY, account.getDiscordId(),
                        account.getPermissionData(), account.getRoleData(), account.getBalance()));
            }
            // Update the existing column with new data.
            else
            {
                statement.executeUpdate(String.format(SQL_UPDATE_QUERY, account.getPermissionData(),
                        account.getRoleData(), account.getBalance(), account.getDiscordId()));
            }

            // Data no longer needs to be updated.
            account.setNeedsToSync(false);
            return true;
        }
        catch (final SQLException sqlException)
        {
            KodeKitten.logSevere("Unable to save account data for discord id " + account.getDiscordId());
            sqlException.printStackTrace();
            return false;
        }
    }

    /**
     * Obtains the account from its discord id and saves it to the database.
     *
     * @return True if the account was successfully saved to the database.
     */
    public boolean saveToDatabase(final long discordId)
    {
        final Account account = getAccount(discordId, false);

        return account != null && saveToDatabase(account);
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
     * Saves an account to the database and keeps the account in memory if it was unable to save it.
     */
    public void flushFromMemory(@NonNull final Account account)
    {
        flushFromMemory(account, true);
    }

    /**
     * Loads an account's data from the database.
     *
     * @return True if the account was loaded.
     */
    public boolean loadFromDatabase(@NonNull final Account account)
    {
        final Connection connection = getConnection();

        if (connection == null)
        {
            return false;
        }

        try (final Statement statement = connection.createStatement(); final ResultSet resultSet =
                statement.executeQuery(String.format(SQL_LOAD_QUERY, account.getDiscordId())))
        {
            if (resultSet.next())
            {
                account.loadPermissions(resultSet.getString("permissions"));
                account.loadRoles(resultSet.getString("roles"));
                account.setBalance(resultSet.getDouble("balance"));
            }

            account.bumpLastActivityTime();
            account.setLoaded();
            return true;
        }
        catch (final SQLException sqlException)
        {
            sqlException.printStackTrace();
            return false;
        }
    }

    /**
     * Removes a given account from memory.
     */
    public void deleteFromMemory(final long discordId)
    {
        accounts.remove(discordId);
    }

    /**
     * Removes a given account from memory.
     */
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

    /**
     * Deletes the given account from the database.
     */
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

    /**
     * Deletes the given account from the database AND from memory.
     */
    public void delete(@NonNull final Account account)
    {
        delete(account.getDiscordId());
    }

    /**
     * Closes the database connection.
     */
    public void closeDatabaseConnection()
    {
        final Connection connection = getConnection();

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
            this.connection = null;
        }
    }

    /**
     * Closes the account database connection if it has been inactive for over 5 minutes.
     */
    public boolean cleanupConnectionInactivity()
    {
        if (getConnection() == null)
        {
            return false;
        }

        int seconds = (int) ((System.currentTimeMillis() - connectionLastActivity) / 1000);

        // Been over 5 minutes.
        if (seconds > 300)
        {
            closeDatabaseConnection();
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Removes accounts flagged as inactive from internal memory.
     * Saves all account data before removing them.
     */
    public boolean cleanupAccountInactivity()
    {
        final Connection connection = getConnection();
        boolean accountsFlushed = false;

        for (var entry : accounts.entrySet())
        {
            final Account account = entry.getValue();

            if (!account.isInactive())
            {
                continue;
            }

            // If there is no active connection we only remove the account from memory if it is dead.
            if (connection == null)
            {
                if (account.isDead())
                {
                    accounts.remove(entry.getKey());
                    accountsFlushed = true;
                }
            }
            // If there is an active connection we attempt to save the account to the database before removing it
            // from memory.
            else if (saveToDatabase(account) || account.isDead())
            {
                accounts.remove(entry.getKey());
                accountsFlushed = true;
            }
        }

        return accountsFlushed;
    }

}