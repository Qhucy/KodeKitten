package com.sylink.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest
{

    private Account account;

    @BeforeEach
    void setUp()
    {
        account = new Account(0);
    }

    @Test
    void constructorTest()
    {
        assertEquals(0, account.getDiscordId());
        assertEquals(0.0, account.getBalance());
        assertEquals(0, account.getLastActivityTime());
    }

    @Test
    void setLoaded()
    {
        assertFalse(account.isLoaded());

        account.setLoaded();

        assertTrue(account.isLoaded());
    }

    @Test
    void bumpingActivityTimeChangesValue()
    {
        assertEquals(0, account.getLastActivityTime());

        account.bumpLastActivityTime();

        assertNotEquals(0, account.getLastActivityTime());
    }

    @Test
    void settingBalanceChangesBalance()
    {
        assertEquals(0.0, account.getBalance());

        account.setBalance(10.0);

        assertEquals(10.0, account.getBalance());
    }

    @Test
    void balanceCannotBeSetUnderZero()
    {
        account.setBalance(-10.0);

        assertEquals(0.0, account.getBalance());
    }

    @Test
    void settingBalanceNeedsToSync()
    {
        account.setBalance(10.0);

        assertTrue(account.needsToSync());
    }

    @Test
    void addingBalanceAddsToBalance()
    {
        assertEquals(0.0, account.getBalance());

        account.addBalance(10.0);

        assertEquals(10.0, account.getBalance());
    }

    @Test
    void addingBalanceResultCannotBeUnderZero()
    {
        assertEquals(0.0, account.getBalance());

        account.addBalance(-1000.0);

        assertEquals(0.0, account.getBalance());
    }

    @Test
    void addingBalanceNeedsToSync()
    {
        account.addBalance(10.0);

        assertTrue(account.needsToSync());
    }

    @Test
    void removingBalanceRemovesBalance()
    {
        account.setBalance(10.0);

        assertEquals(10.0, account.getBalance());

        account.removeBalance(5.0);

        assertEquals(5.0, account.getBalance());
    }

    @Test
    void removingBalanceResultCannotBeUnderZero()
    {
        account.setBalance(10.0);

        assertEquals(10.0, account.getBalance());

        account.removeBalance(20.0);

        assertEquals(0.0, account.getBalance());
    }

    @Test
    void removingBalanceNeedsToSync()
    {
        account.removeBalance(10.0);

        assertTrue(account.needsToSync());
    }

    @Test
    void resettingBalanceSetsToZero()
    {
        account.setBalance(10.0);

        assertEquals(10.0, account.getBalance());

        account.resetBalance();

        assertEquals(0.0, account.getBalance());
    }

    @Test
    void resettingBalanceNeedsToSync()
    {
        account.resetBalance();

        assertTrue(account.needsToSync());
    }

    @Test
    void accountIsntLoadedIfNotConnected()
    {
        assertFalse(account.isLoaded());
    }

    @Test
    void accountDoesntNeedsToSyncIfNotUpdated()
    {
        assertFalse(account.needsToSync());
    }

    @Test
    void accountDoesntHaveRolesOnCreation()
    {
        assertFalse(account.hasRoles());
    }

    @Test
    void accountCanAddRoles()
    {
        account.addRole(1L);

        assertTrue(account.hasRoles());
        assertTrue(account.hasRole(1L));
    }

    @Test
    void accountCanRemoveRoles()
    {
        account.addRole(1L);

        assertTrue(account.hasRoles());
        assertTrue(account.hasRole(1L));

        account.removeRole(1L);

        assertFalse(account.hasRoles());
        assertFalse(account.hasRole(1L));
    }

    @Test
    void doesntHavePermissionsOnCreation()
    {
        assertFalse(account.hasPermissions());
    }

    @Test
    void canAddPermissions()
    {
        account.addPermission("admin");

        assertTrue(account.hasPermissions());
        assertTrue(account.hasPermission("admin"));
    }

    @Test
    void canRemovePermissions()
    {
        account.addPermission("admin");

        assertTrue(account.hasPermissions());
        assertTrue(account.hasPermission("admin"));

        account.removePermission("admin");

        assertFalse(account.hasPermissions());
        assertFalse(account.hasPermission("admin"));
    }

    @Test
    void clearingPermissions()
    {
        account.addPermission("admin");

        assertTrue(account.hasPermissions());
        assertTrue(account.hasPermission("admin"));

        account.clearPermissions();

        assertFalse(account.hasPermissions());
        assertFalse(account.hasPermission("admin"));
        assertEquals(0, account.getPermissions().size());
    }

    @Test
    void clearingRoles()
    {
        account.addRole(1L);

        assertTrue(account.hasRoles());
        assertTrue(account.hasRole(1L));

        account.clearRoles();

        assertFalse(account.hasRoles());
        assertFalse(account.hasRole(1L));
        assertEquals(0, account.getRoles().size());
    }

}