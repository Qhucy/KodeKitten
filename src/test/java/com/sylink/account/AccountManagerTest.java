package com.sylink.account;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountManagerTest
{

    private final static String DATABASE_URL = "jdbc:sqlite:src/test/java/com/sylink/account/database_test.db";
    private static AccountManager accountManager = null;

    @BeforeAll
    static void setUpAll()
    {
        accountManager = new AccountManager();

        accountManager.openDatabaseConnection(DATABASE_URL);
    }

    @Test
    void getAccountNullIfNotExist()
    {
        assertNull(accountManager.getAccount(1L, false));
    }

    @Test
    void getAccountCreatesNewAccountIfNotExist()
    {
        final Account account = accountManager.getAccount(5L);

        assertNotNull(account);
        assertEquals(5L, account.getDiscordId());
        assertTrue(accountManager.existsInMemory(5L));
    }

    @Test
    void loadingAccountFromDatabase()
    {
        Account account = accountManager.getAccount(10L, false);

        assertNotNull(account);
        assertEquals(10L, account.getDiscordId());
        assertTrue(account.hasPermission("admin"));
        assertTrue(account.hasRole(10L));
        assertEquals(10.0, account.getBalance());
    }

    @Test
    void flushingRemovesAccountFromMemory()
    {
        Account account = accountManager.getAccount(4L);

        assertNotNull(account);
        assertEquals(4L, account.getDiscordId());
        assertTrue(accountManager.existsInMemory(4L));

        accountManager.flushFromMemory(account, false);

        assertFalse(accountManager.existsInMemory(4L));
    }

    @Test
    void doesntExistInDatabase()
    {
        assertFalse(accountManager.existsInDatabase(2L));
    }

    @Test
    void doesExistInDatabase()
    {
        assertTrue(accountManager.existsInDatabase(10L));
    }

    @Test
    void accountExistsIfInMemory()
    {
        accountManager.getAccount(20L, true);

        assertTrue(accountManager.exists(20L));
    }

    @Test
    void accountExistsIfInDatabase()
    {
        assertTrue(accountManager.existsInDatabase(10L));
    }

    @AfterAll
    static void afterAll()
    {
        accountManager.closeDatabaseConnection();
    }

}