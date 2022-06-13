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
        Account account = new Account(0);

        assertFalse(account.isLoaded());
    }

    @Test
    void accountDoesntNeedsToSyncIfNotUpdated()
    {
        Account account = new Account(0);

        assertFalse(account.needsToSync());
    }

}