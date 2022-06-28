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
    void setConnectionLastActivity()
    {
        assertDoesNotThrow(() -> Thread.sleep(5));

        final long currentTime = System.currentTimeMillis();

        accountManager.setConnectionLastActivity(currentTime);

        assertEquals(currentTime, accountManager.getConnectionLastActivity());
    }

    @Test
    void getAccountFromMemory()
    {
        assertNotNull(accountManager.getAccount(76L, true));
        assertNotNull(accountManager.getAccount(76L, false));
    }

    @Test
    void gettingAccountFromMemoryBumpsActivityTime()
    {
        final long activityTime = accountManager.getAccount(78L).getLastActivityTime();

        assertDoesNotThrow(() -> Thread.sleep(5));

        assertNotNull(accountManager.getAccount(78L, false));
        assertNotEquals(activityTime, accountManager.getAccount(78L).getLastActivityTime());
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
        assertEquals(0.0, account.getBalance());
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

        assertTrue(accountManager.loadFromDatabase(account));

        assertEquals(10.0, account.getBalance());
    }

    @Test
    void getAccountCreatesNewByDefault()
    {
        assertFalse(accountManager.existsInMemory(444L));

        final Account account = accountManager.getAccount(444L);

        assertTrue(accountManager.existsInMemory(444L));
        assertEquals(0.0, account.getBalance());
    }

    @Test
    void getConnectionBumpsLastActivity()
    {
        final long activityTime = accountManager.getConnectionLastActivity();

        assertDoesNotThrow(() -> Thread.sleep(5));
        assertNotNull(accountManager.getConnection());
        assertNotEquals(activityTime, accountManager.getConnectionLastActivity());
    }

    @Test
    void executingQueryToDatabase()
    {
        assertNotNull(accountManager.getAccount(578L));

        accountManager.saveToDatabase(578L);

        assertTrue(accountManager.existsInDatabase(578L));

        accountManager.executeQuery("DELETE FROM accounts WHERE id = 578");

        assertFalse(accountManager.existsInDatabase(578L));
    }

    @Test
    void executingMultipleQueriesToDatabase()
    {
        assertNotNull(accountManager.getAccount(579L));
        assertNotNull(accountManager.getAccount(580L));

        accountManager.saveToDatabase(579L);
        accountManager.saveToDatabase(580L);

        assertTrue(accountManager.existsInDatabase(579L));
        assertTrue(accountManager.existsInDatabase(580L));

        accountManager.executeQuery("DELETE FROM accounts WHERE id = 579", "DELETE FROM accounts WHERE id = 580");

        assertFalse(accountManager.existsInDatabase(579L));
        assertFalse(accountManager.existsInDatabase(580L));
    }

    @Test
    void accountDoesntExistInDatabase()
    {
        assertFalse(accountManager.existsInDatabase(2L));
    }

    @Test
    void accountDoesExistInDatabase()
    {
        assertTrue(accountManager.existsInDatabase(10L));
    }

    @Test
    void accountExistsIfInDatabaseAgain()
    {
        final Account account = new Account(10L);

        assertTrue(accountManager.existsInDatabase(account));
    }

    @Test
    void accountExistsInMemory()
    {
        assertNotNull(accountManager.getAccount(617L));
        assertTrue(accountManager.existsInMemory(617L));
    }

    @Test
    void accountExistsEntirelyIfInMemory()
    {
        assertFalse(accountManager.existsInDatabase(851L));
        assertNotNull(accountManager.getAccount(851L));
        assertTrue(accountManager.exists(851L));
    }

    @Test
    void accountExistsEntirelyIfInDatabase()
    {
        accountManager.deleteFromMemory(10L);

        assertFalse(accountManager.existsInMemory(10L));
        assertTrue(accountManager.exists(10L));
    }

    @Test
    void accountDoesntExist()
    {
        assertFalse(accountManager.exists(9999999L));
    }

    @Test
    void savingNonExistentAccount()
    {
        assertFalse(accountManager.saveToDatabase(432908587L));
    }

    @Test
    void dontSaveAccountToDatabaseIfExistsAndNoChanges()
    {
        final Account account = accountManager.getAccount(100L);

        assertNotNull(account);

        account.setNeedsToSync(false);

        assertFalse(account.needsToSync());

        if (!accountManager.existsInDatabase(100L))
        {
            assertTrue(accountManager.saveToDatabase(account));
        }

        assertFalse(accountManager.saveToDatabase(100L));
    }

    @Test
    void savingNewAccountToDatabase()
    {
        accountManager.delete(100L);

        assertNotNull(accountManager.getAccount(100L));
        assertTrue(accountManager.existsInMemory(100L));
        assertFalse(accountManager.existsInDatabase(100L));

        assertTrue(accountManager.saveToDatabase(100L));

        assertTrue(accountManager.existsInDatabase(100L));
        assertNotNull(accountManager.getAccount(100L, false));
    }

    @Test
    void updatingExistingAccountInDatabase()
    {
        Account account = accountManager.getAccount(100L);

        assertNotNull(account);

        if (!accountManager.existsInDatabase(100L))
        {
            assertTrue(accountManager.saveToDatabase(100L));
        }

        account.setBalance(7.7);

        assertTrue(accountManager.saveToDatabase(100L));

        accountManager.deleteFromMemory(100L);

        account = accountManager.getAccount(100L, false);

        assertNotNull(account);
        assertEquals(7.7, account.getBalance());
    }

    @Test
    void savingLoadingAccountPermission()
    {
        Account account = accountManager.getAccount(100L);

        assertNotNull(account);

        account.addPermission("admin");

        accountManager.saveToDatabase(account);
        accountManager.deleteFromMemory(account);

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

        accountManager.saveToDatabase(account);
        accountManager.deleteFromMemory(account);

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

        accountManager.saveToDatabase(account);
        accountManager.deleteFromMemory(account);

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

        accountManager.saveToDatabase(account);
        accountManager.deleteFromMemory(account);

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

        accountManager.saveToDatabase(account);
        accountManager.deleteFromMemory(account);

        assertFalse(accountManager.existsInMemory(100L));

        account = accountManager.getAccount(100L, false);

        assertNotNull(account);
        assertEquals(100.0, account.getBalance());
    }

    @Test
    void flushingAccountSavesItToDatabase()
    {
        accountManager.delete(100L);

        assertFalse(accountManager.existsInDatabase(100L));

        Account account = accountManager.getAccount(100L);

        assertNotNull(account);

        account.setBalance(6.6);
        accountManager.flushFromMemory(account, false);

        assertFalse(accountManager.existsInMemory(100L));
        assertTrue(accountManager.existsInDatabase(100L));

        account = accountManager.getAccount(100L, false);

        assertNotNull(account);
        assertEquals(6.6, account.getBalance());
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
    void loadingAccountDataFromDatabase()
    {
        Account account = accountManager.getAccount(10L, false);

        assertNotNull(account);

        account.setBalance(0.0);
        account.clearRoles();
        account.clearPermissions();

        assertEquals(0.0, account.getBalance());
        assertFalse(account.hasRoles());
        assertFalse(account.hasPermissions());

        assertTrue(accountManager.loadFromDatabase(account));

        assertEquals(10.0, account.getBalance());
        assertTrue(account.hasPermission("admin"));
        assertTrue(account.hasRole(10L));
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
    void deletingAccountFromMemoryAgain()
    {
        assertNotNull(accountManager.getAccount(11L));
        assertTrue(accountManager.existsInMemory(11L));

        accountManager.deleteFromMemory(11L);

        assertFalse(accountManager.existsInMemory(11L));
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

        accountManager.delete(account);

        assertFalse(accountManager.existsInMemory(300L));
        assertFalse(accountManager.existsInDatabase(300L));
    }

    @Test
    void deletingAccountEntirelyAgain()
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
    void cleanupNullConnection()
    {
        accountManager.closeDatabaseConnection();

        assertFalse(accountManager.cleanupConnectionInactivity());

        accountManager.openDatabaseConnection(DATABASE_URL);
    }

    @Test
    void cleanupActiveConnection()
    {
        final long activityOneMinuteAgo = System.currentTimeMillis() - (60 * 1000);

        accountManager.setConnectionLastActivity(activityOneMinuteAgo);

        assertFalse(accountManager.cleanupConnectionInactivity());
    }

    @Test
    void cleanupInactiveConnection()
    {
        final long activityTenMinutesAgo = System.currentTimeMillis() - (600 * 1000);

        accountManager.setConnectionLastActivity(activityTenMinutesAgo);

        assertTrue(accountManager.cleanupConnectionInactivity());

        accountManager.openDatabaseConnection(DATABASE_URL);
    }

    @Test
    void cleanupZeroInactiveAccounts()
    {
        assertFalse(accountManager.cleanupAccountInactivity());
    }

    @Test
    void cleanupInactiveAccount()
    {
        accountManager.delete(4L);

        assertFalse(accountManager.existsInDatabase(4L));

        final Account account = accountManager.getAccount(4L);
        final long activeTwentyMinutesAgo = System.currentTimeMillis() - (1200 * 1000);

        account.addBalance(100.0);
        account.setLastActivityTime(activeTwentyMinutesAgo);

        assertTrue(account.needsToSync());
        assertTrue(account.isInactive());
        assertFalse(account.isDead());

        assertTrue(accountManager.cleanupAccountInactivity());
        assertFalse(accountManager.existsInMemory(4L));
        assertTrue(accountManager.existsInDatabase(4L));
    }

    @Test
    void cleanupInactiveAccountNoSave()
    {
        assertTrue(accountManager.existsInDatabase(10L));

        final Account account = accountManager.getAccount(10L);
        final long activeTwentyMinutesAgo = System.currentTimeMillis() - (1200 * 1000);

        account.setLastActivityTime(activeTwentyMinutesAgo);

        assertFalse(account.needsToSync());
        assertTrue(account.isInactive());
        assertFalse(account.isDead());

        assertFalse(accountManager.cleanupAccountInactivity());
        assertTrue(accountManager.existsInMemory(10L));
    }

    @Test
    void cleanupInactiveAccountNoSaveAndIsDead()
    {
        assertTrue(accountManager.existsInDatabase(10L));

        final Account account = accountManager.getAccount(10L);
        final long activeTwoHoursAgo = System.currentTimeMillis() - (7200 * 1000);

        account.setLastActivityTime(activeTwoHoursAgo);

        assertFalse(account.needsToSync());
        assertTrue(account.isInactive());
        assertTrue(account.isDead());

        assertTrue(accountManager.cleanupAccountInactivity());
        assertFalse(accountManager.existsInMemory(10L));
    }

    @Test
    void cleanupInactiveAccountNullConnection()
    {
        accountManager.closeDatabaseConnection();

        assertFalse(accountManager.cleanupAccountInactivity());

        accountManager.openDatabaseConnection(DATABASE_URL);
    }

    @Test
    void cleanupInactiveAccountNullConnectionAndIsDead()
    {
        accountManager.closeDatabaseConnection();

        final Account account = accountManager.getAccount(407L);
        final long activeTwoHoursAgo = System.currentTimeMillis() - (7200 * 1000);

        account.setLastActivityTime(activeTwoHoursAgo);

        assertTrue(account.isInactive());
        assertTrue(account.isDead());

        assertTrue(accountManager.cleanupAccountInactivity());

        accountManager.openDatabaseConnection(DATABASE_URL);
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