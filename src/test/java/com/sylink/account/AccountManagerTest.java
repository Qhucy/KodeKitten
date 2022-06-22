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
    void removingAccountFromMemory()
    {
        Account account = accountManager.getAccount(11L);

        assertNotNull(account);
        assertTrue(accountManager.existsInMemory(11L));

        accountManager.removeFromMemory(account);

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
        // Id 100 is used for saving / loading tests so we need to remove it so it
        // does not impact the result of tests when re-run.
        accountManager.executeQuery("DELETE FROM accounts WHERE id = 100");

        accountManager.closeDatabaseConnection();
    }

}