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
    private final static String DATABASE_URL = "";
    // Connection to the accounts database.
    private static Connection dbConnection = null;
    // Map the stores the Discord Id associated with its account.
    private final static Map<Long, Account> accounts = new HashMap<>();

    public static Account getAccount(final long discordId)
    {
        if (accounts.containsKey(discordId))
        {
            return accounts.get(discordId);
        }

        final Account account = new Account(discordId);


    }

    /**
     * Returns whether a successful connection with the accounts database was created.
     */
    public static boolean openDatabaseConnection()
    {
        try
        {
            dbConnection = DriverManager.getConnection(DATABASE_URL);
            final Statement statement = dbConnection.createStatement();

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS accounts
                    (
                        id BIGINT NOT NULL UNIQUE,
                        balance DOUBLE NOT NULL DEFAULT 0.0
                    );
                    """);

            statement.close();
            return true;
        } catch (final SQLException sqlException)
        {
            KodeKitten.logSevere("Unable to access 'database.db' database");
            sqlException.printStackTrace();
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
            dbConnection.close();
        } catch (final SQLException sqlException)
        {
            KodeKitten.logSevere("Unable to properly close database connection.");
            sqlException.printStackTrace();
        }
    }

}