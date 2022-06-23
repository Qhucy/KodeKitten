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
        accountManager = AccountManager.getInstance();

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
    void loadingExistingAccountFromDatabase()
    {
        Account account = accountManager.getAccount(10L);

        account.setBalance(20.0);

        assertEquals(20.0, account.getBalance());
        assertTrue(accountManager.existsInMemory(10L));

        assertTrue(accountManager.loadFromDatabase(10L));

        assertEquals(10.0, account.getBalance());
    }

    @Test
    void loadingExistingAccountFromDatabaseAgain()
    {
        Account account = accountManager.getAccount(10L);

        account.setBalance(20.0);

        assertEquals(20.0, account.getBalance());
        assertTrue(accountManager.existsInMemory(10L));

        assertTrue(accountManager.loadFromDatabase(account));

        assertEquals(10.0, account.getBalance());
    }

    @Test
    void loadingNewAccountFromDatabase()
    {
        if (accountManager.existsInMemory(10L))
        {
            accountManager.deleteFromMemory(10L);
        }

        assertFalse(accountManager.existsInMemory(10L));

        accountManager.loadFromDatabase(10L);

        assertTrue(accountManager.existsInMemory(10L));

        Account account = accountManager.getAccount(10L);

        assertNotNull(account);
        assertEquals(10.0, account.getBalance());
    }

    @Test
    void deletingAccountFromMemory()
    {
        Account account = accountManager.getAccount(11L);

        assertNotNull(account);
        assertTrue(accountManager.existsInMemory(11L));

        accountManager.deleteFromMemory(account);

        assertFalse(accountManager.existsInMemory(11L));
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
    void flushingRemovesAccountFromMemoryAgain()
    {
        Account account = accountManager.getAccount(4L);

        assertNotNull(account);
        assertEquals(4L, account.getDiscordId());
        assertTrue(accountManager.existsInMemory(4L));

        accountManager.flushFromMemory(account);

        assertFalse(accountManager.existsInMemory(4L));
    }

    @Test
    void deletingAccountFromDatabase()
    {
        Account account = accountManager.getAccount(200L);

        assertNotNull(account);
        assertTrue(accountManager.existsInMemory(200L));

        accountManager.saveToDatabase(account);

        assertTrue(accountManager.existsInDatabase(200L));

        accountManager.deleteFromDatabase(200L);

        assertFalse(accountManager.existsInDatabase(200L));
    }

    @Test
    void deletingAccountFromDatabaseAgain()
    {
        Account account = accountManager.getAccount(200L);

        assertNotNull(account);
        assertTrue(accountManager.existsInMemory(200L));

        accountManager.saveToDatabase(200L);

        assertTrue(accountManager.existsInDatabase(200L));

        accountManager.deleteFromDatabase(account);

        assertFalse(accountManager.existsInDatabase(200L));
    }

    @Test
    void deletingAccountEntirely()
    {
        Account account = accountManager.getAccount(300L);

        assertNotNull(account);
        assertTrue(accountManager.existsInMemory(300L));

        accountManager.saveToDatabase(account);

        assertTrue(accountManager.existsInDatabase(300L));

        accountManager.delete(300L);

        assertFalse(accountManager.existsInMemory(300L));
        assertFalse(accountManager.existsInDatabase(300L));
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

    @Test
    void savingAccountToDatabase()
    {
        Account account = accountManager.getAccount(100L);

        assertNotNull(account);

        assertTrue(accountManager.existsInMemory(100L));

        accountManager.flushFromMemory(account, false);

        assertFalse(accountManager.existsInMemory(100L));
        assertTrue(accountManager.existsInDatabase(100L));

        account = accountManager.getAccount(100L, false);

        assertNotNull(account);
    }

    @Test
    void savingLoadingAccountPermission()
    {
        Account account = accountManager.getAccount(100L);

        assertNotNull(account);

        account.addPermission("admin");

        accountManager.flushFromMemory(account, false);

        assertFalse(accountManager.existsInMemory(100L));

        account = accountManager.getAccount(100L, false);

        assertNotNull(account);
        assertTrue(account.hasPermission("admin"));
        assertTrue(account.hasPermissions());
    }

    @Test
    void savingLoadingAccountPermissions()
    {
        Account account = accountManager.getAccount(100L);

        assertNotNull(account);

        account.addPermission("admin");
        account.addPermission("*");
        account.addPermission("all");

        accountManager.flushFromMemory(account, false);

        assertFalse(accountManager.existsInMemory(100L));

        account = accountManager.getAccount(100L, false);

        assertNotNull(account);
        assertTrue(account.hasPermission("admin"));
        assertTrue(account.hasPermission("*"));
        assertTrue(account.hasPermission("all"));
        assertTrue(account.hasPermissions());
    }

    @Test
    void savingLoadingAccountRole()
    {
        Account account = accountManager.getAccount(100L);

        assertNotNull(account);

        account.addRole(1L);

        accountManager.flushFromMemory(account, false);

        assertFalse(accountManager.existsInMemory(100L));

        account = accountManager.getAccount(100L, false);

        assertNotNull(account);
        assertTrue(account.hasRole(1L));
        assertTrue(account.hasRoles());
    }

    @Test
    void savingLoadingAccountRoles()
    {
        Account account = accountManager.getAccount(100L);

        assertNotNull(account);

        account.addRole(1L);
        account.addRole(2L);
        account.addRole(3L);

        accountManager.flushFromMemory(account, false);

        assertFalse(accountManager.existsInMemory(100L));

        account = accountManager.getAccount(100L, false);

        assertNotNull(account);
        assertTrue(account.hasRole(1L));
        assertTrue(account.hasRole(2L));
        assertTrue(account.hasRole(3L));
        assertTrue(account.hasRoles());
    }

    @Test
    void savingLoadingBalance()
    {
        Account account = accountManager.getAccount(100L);

        assertNotNull(account);

        account.setBalance(100.0);

        accountManager.flushFromMemory(account, false);

        assertFalse(accountManager.existsInMemory(100L));

        account = accountManager.getAccount(100L, false);

        assertNotNull(account);
        assertEquals(100.0, account.getBalance());
    }

    @AfterAll
    static void afterAll()
    {
        // Id 100 & 4 are used for saving during tests and need to be deleted at the end.
        accountManager.deleteFromDatabase(100L);
        accountManager.deleteFromDatabase(4L);

        accountManager.closeDatabaseConnection();
    }

}