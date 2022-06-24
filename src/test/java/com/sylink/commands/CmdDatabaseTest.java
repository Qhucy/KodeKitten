package com.sylink.commands;

import com.sylink.account.Account;
import com.sylink.account.AccountManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CmdDatabaseTest
{

    private final static String DATABASE_URL = "jdbc:sqlite:src/test/java/com/sylink/account/database_test.db";
    private static AccountManager accountManager;

    @BeforeAll
    static void setUpAll()
    {
        accountManager = AccountManager.getInstance();
        accountManager.openDatabaseConnection(DATABASE_URL);

        Command.registerCommand(new CmdDatabase());
    }

    @Test
    void createAccount()
    {
        Command.runCommands("db", new String[] {"create", "0"});

        assertTrue(accountManager.existsInMemory(0L));
        assertNotNull(accountManager.getAccount(0L));
    }

    @Test
    void loadAccount()
    {
        assertTrue(accountManager.existsInDatabase(10L));

        Command.runCommands("db", new String[] {"load", "10"});

        assertTrue(accountManager.existsInMemory(10L));

        Account account = accountManager.getAccount(10L);

        assertNotNull(account);
        assertEquals(10.0, account.getBalance());
        assertTrue(account.hasPermission("admin"));
        assertTrue(account.hasRole(10L));
    }

    @Test
    void loadAccountWhenNotExist()
    {
        assertFalse(accountManager.existsInMemory(55L));
        assertFalse(accountManager.existsInDatabase(55L));

        Command.runCommands("db", new String[] {"load", "55"});

        assertFalse(accountManager.existsInMemory(55L));
    }

    @Test
    void saveAccount()
    {
        if (accountManager.existsInDatabase(44L))
        {
            accountManager.deleteFromDatabase(44L);
        }

        Account account = accountManager.getAccount(44L);

        account.setBalance(5.0);

        assertFalse(accountManager.existsInDatabase(44L));

        Command.runCommands("db", new String[] {"save", "44"});

        assertTrue(accountManager.existsInDatabase(44L));

        accountManager.loadFromDatabase(account);

        assertTrue(accountManager.existsInMemory(44L));

        account = accountManager.getAccount(44L);

        assertNotNull(account);
        assertEquals(5.0, account.getBalance());
    }

    @Test
    void flushAccount()
    {
        if (accountManager.existsInDatabase(44L))
        {
            accountManager.deleteFromDatabase(44L);
        }

        Account account = accountManager.getAccount(44L);

        account.setBalance(7.0);

        assertFalse(accountManager.existsInDatabase(44L));
        assertTrue(accountManager.existsInMemory(44L));

        Command.runCommands("db", new String[] {"flush", "44"});

        assertTrue(accountManager.existsInDatabase(44L));
        assertFalse(accountManager.existsInMemory(44L));

        account = accountManager.getAccount(44L);

        assertTrue(accountManager.existsInMemory(44L));

        account = accountManager.getAccount(44L);

        assertNotNull(account);
        assertEquals(7.0, account.getBalance());
    }

    @Test
    void deleteAccount()
    {
        Account account = accountManager.getAccount(44L);

        assertNotNull(account);

        accountManager.saveToDatabase(account);

        assertTrue(accountManager.existsInDatabase(44L));
        assertTrue(accountManager.existsInMemory(44L));

        Command.runCommands("db", new String[] {"delete", "44"});

        assertFalse(accountManager.existsInDatabase(44L));
        assertFalse(accountManager.existsInMemory(44L));
    }

    @Test
    void deleteAccountWhenNotExist()
    {
        Command.runCommands("db", new String[] {"delete", "111"});

        assertFalse(accountManager.existsInDatabase(111L));
        assertFalse(accountManager.existsInMemory(111L));
    }

    @AfterAll
    static void afterAll()
    {
        // Id 44 is used for testing and needs to be removed at the end.
        accountManager.deleteFromDatabase(44L);

        accountManager.closeDatabaseConnection();
    }

}