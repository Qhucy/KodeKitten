package com.sylink.account;

import com.sylink.Bot;
import com.sylink.util.Testing;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest
{

    private Account account;

    @BeforeEach
    void setUp()
    {
        account = new Account(0L);
    }

    @Test
    void constructorTest()
    {
        assertEquals(0L, account.getDiscordId());
        assertEquals(0.0, account.getBalance());
    }

    @Test
    void constructorWithLastActivityTime()
    {
        Account account = new Account(0L, 100L);

        assertEquals(0L, account.getDiscordId());
        assertEquals(0.0, account.getBalance());
        assertEquals(100L, account.getLastActivityTime());
    }

    @Test
    void accountIsntLoadedIfNotConnected()
    {
        assertFalse(account.isLoaded());
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
        final long activityTime = account.getLastActivityTime();

        assertDoesNotThrow(() -> Thread.sleep(5));

        account.bumpLastActivityTime();

        assertNotEquals(activityTime, account.getLastActivityTime());
    }

    @Test
    void accountIsntInactive()
    {
        final long activeLessThan10MinutesAgo = System.currentTimeMillis() - (500 * 1000);
        final Account account = new Account(0L, activeLessThan10MinutesAgo);

        assertFalse(account.isInactive());
    }

    @Test
    void accountIsInactive()
    {
        final long activeMoreThan10MinutesAgo = System.currentTimeMillis() - (700 * 1000);
        final Account account = new Account(0L, activeMoreThan10MinutesAgo);

        assertTrue(account.isInactive());
    }

    @Test
    void accountIsntDead()
    {
        final long activeLessThanAnHourAgo = System.currentTimeMillis() - (3500 * 1000);
        final Account account = new Account(0L, activeLessThanAnHourAgo);

        assertFalse(account.isDead());
    }

    @Test
    void accountIsDead()
    {
        final long activeOverAnHourAgo = System.currentTimeMillis() - (3700 * 1000);
        final Account account = new Account(0L, activeOverAnHourAgo);

        assertTrue(account.isDead());
    }

    @Test
    void accountDoesntNeedsToSyncIfNotUpdated()
    {
        assertFalse(account.needsToSync());
    }

    @Test
    void settingNeedsToSync()
    {
        account.setNeedsToSync(true);

        assertTrue(account.needsToSync());

        account.setNeedsToSync(false);

        assertFalse(account.needsToSync());
    }

    @Test
    void doesntHavePermissionsOnCreation()
    {
        assertFalse(account.hasPermissions());
    }

    @Test
    void hasPermissionIgnoreCase()
    {
        account.addPermission("admin");

        assertTrue(account.hasPermission("admin"));
        assertTrue(account.hasPermission("ADMIN"));
    }

    @Test
    void addPermissions()
    {
        account.addPermission("admin");

        assertTrue(account.hasPermissions());
        assertTrue(account.hasPermission("admin"));
    }

    @Test
    void addPermissionIgnoreCase()
    {
        account.addPermission("ADMIN");

        assertTrue(account.hasPermission("admin"));
        assertEquals("'admin'", account.getPermissionData());
    }

    @Test
    void addingPermissionsNeedsToSync()
    {
        assertFalse(account.needsToSync());

        account.addPermission("admin");

        assertTrue(account.needsToSync());
    }

    @Test
    void addingEmptyPermissionDoesNothing()
    {
        assertFalse(account.needsToSync());
        assertFalse(account.hasPermissions());

        account.addPermission("     ");
        account.addPermission("");

        assertFalse(account.needsToSync());
        assertFalse(account.hasPermissions());
    }

    @Test
    void addingDuplicatePermissionDoesntNeedToSync()
    {
        assertFalse(account.needsToSync());

        account.addPermission("admin");

        assertTrue(account.needsToSync());

        account.setNeedsToSync(false);

        assertFalse(account.needsToSync());

        account.addPermission("admin");

        assertFalse(account.needsToSync());
    }

    @Test
    void removePermissions()
    {
        account.addPermission("admin");

        assertTrue(account.hasPermissions());
        assertTrue(account.hasPermission("admin"));

        account.removePermission("admin");

        assertFalse(account.hasPermissions());
        assertFalse(account.hasPermission("admin"));
    }

    @Test
    void removePermissionsIgnoreCase()
    {
        account.addPermission("admin");

        assertTrue(account.hasPermission("admin"));

        account.removePermission("ADMIN");

        assertFalse(account.hasPermission("admin"));
    }

    @Test
    void removingEmptyPermissionDoesNothing()
    {
        assertFalse(account.needsToSync());
        assertFalse(account.hasPermissions());

        account.removePermission("      ");
        account.removePermission("");

        assertFalse(account.needsToSync());
        assertFalse(account.hasPermissions());
    }

    @Test
    void removingPermissionsNeedsToSync()
    {
        assertFalse(account.needsToSync());

        account.addPermission("admin");

        assertTrue(account.needsToSync());

        account.setNeedsToSync(false);

        assertFalse(account.needsToSync());

        account.removePermission("admin");

        assertTrue(account.needsToSync());
    }

    @Test
    void removingNonExistentPermissionDoesntNeedToSync()
    {
        assertFalse(account.needsToSync());

        account.removePermission("all");

        assertFalse(account.needsToSync());
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
    void clearingPermissionsNeedsToSync()
    {
        assertFalse(account.needsToSync());

        account.addPermission("admin");
        account.addPermission("all");

        assertTrue(account.needsToSync());

        account.setNeedsToSync(false);

        assertFalse(account.needsToSync());

        account.clearPermissions();

        assertTrue(account.needsToSync());
    }

    @Test
    void clearingZeroPermissionsDoesntNeedToSync()
    {
        assertFalse(account.needsToSync());

        account.clearPermissions();

        assertFalse(account.needsToSync());
    }

    @Test
    void loadingEmptyPermissionData()
    {
        account.addPermission("admin");

        assertTrue(account.hasPermissions());
        assertTrue(account.hasPermission("admin"));

        account.loadPermissions("");

        assertFalse(account.hasPermissions());
        assertFalse(account.hasPermission("admin"));
    }

    @Test
    void loadingSinglePermissionData()
    {
        account.addPermission("admin");

        assertTrue(account.hasPermissions());
        assertTrue(account.hasPermission("admin"));

        account.loadPermissions("all");

        assertTrue(account.hasPermissions());
        assertFalse(account.hasPermission("admin"));
        assertTrue(account.hasPermission("all"));
    }

    @Test
    void loadingMultiplePermissionData()
    {
        account.addPermission("admin");

        assertTrue(account.hasPermissions());
        assertTrue(account.hasPermission("admin"));

        account.loadPermissions("all,everything,*");

        assertTrue(account.hasPermissions());
        assertFalse(account.hasPermission("admin"));
        assertTrue(account.hasPermission("all"));
        assertTrue(account.hasPermission("everything"));
        assertTrue(account.hasPermission("*"));
    }

    @Test
    void gettingEmptyPermissionData()
    {
        assertEquals("''", account.getPermissionData());
    }

    @Test
    void gettingSinglePermissionData()
    {
        account.addPermission("admin");

        assertTrue(account.hasPermission("admin"));
        assertEquals("'admin'", account.getPermissionData());
    }

    @Test
    void gettingMultiplePermissionData()
    {
        account.addPermission("admin");
        account.addPermission("all");

        assertTrue(account.hasPermission("admin"));
        assertTrue(account.hasPermission("all"));

        final String permissionData = account.getPermissionData();

        assertTrue(permissionData.equals("'admin,all'") || permissionData.equals("'all,admin'"));
    }

    @Test
    void accountDoesntHaveRolesOnCreation()
    {
        assertFalse(account.hasRoles());
    }

    @Test
    void accountAddRoles()
    {
        account.addRole(1L);

        assertTrue(account.hasRoles());
        assertTrue(account.hasRole(1L));
    }

    @Test
    void accountAddRolesNeedsToSync()
    {
        assertFalse(account.needsToSync());

        account.addRole(1L);

        assertTrue(account.needsToSync());
    }

    @Test
    void accountAddDuplicateRoleDoesntNeedToSync()
    {
        assertFalse(account.needsToSync());

        account.addRole(1L);

        assertTrue(account.needsToSync());

        account.setNeedsToSync(false);

        assertFalse(account.needsToSync());

        account.addRole(1L);

        assertFalse(account.needsToSync());
    }

    @Test
    void accountRemoveRoles()
    {
        account.addRole(1L);

        assertTrue(account.hasRoles());
        assertTrue(account.hasRole(1L));

        account.removeRole(1L);

        assertFalse(account.hasRoles());
        assertFalse(account.hasRole(1L));
    }

    @Test
    void accountRemoveRolesNeedsToSync()
    {
        account.addRole(1L);

        assertTrue(account.needsToSync());

        account.setNeedsToSync(false);

        assertFalse(account.needsToSync());

        account.removeRole(1L);

        assertTrue(account.needsToSync());
    }

    @Test
    void accountRemoveNonExistenceRoleDoesntNeedToSync()
    {
        assertFalse(account.needsToSync());

        account.removeRole(1L);

        assertFalse(account.needsToSync());
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

    @Test
    void clearingRolesNeedsToSync()
    {
        account.addRole(1L);

        assertTrue(account.needsToSync());

        account.setNeedsToSync(false);

        assertFalse(account.needsToSync());

        account.clearRoles();

        assertTrue(account.needsToSync());
    }

    @Test
    void clearingNoRolesDoesntNeedToSync()
    {
        assertFalse(account.needsToSync());

        account.clearRoles();

        assertFalse(account.needsToSync());
    }

    @Test
    void loadingEmptyRoleData()
    {
        account.addRole(1L);

        assertTrue(account.hasRoles());
        assertTrue(account.hasRole(1L));

        account.loadRoles("");

        assertFalse(account.hasRoles());
        assertFalse(account.hasRole(1L));
    }

    @Test
    void loadingSingleRoleData()
    {
        account.addRole(1L);

        assertTrue(account.hasRoles());
        assertTrue(account.hasRole(1L));

        account.loadRoles("10");

        assertTrue(account.hasRoles());
        assertFalse(account.hasRole(1L));
        assertTrue(account.hasRole(10L));
    }

    @Test
    void loadingMultipleRoleData()
    {
        account.addRole(1L);

        assertTrue(account.hasRoles());
        assertTrue(account.hasRole(1L));

        account.loadRoles("10,11,12");

        assertTrue(account.hasRoles());
        assertFalse(account.hasRole(1L));
        assertTrue(account.hasRole(10L));
        assertTrue(account.hasRole(11L));
        assertTrue(account.hasRole(12L));
    }

    @Test
    void gettingEmptyRoleData()
    {
        assertEquals("''", account.getRoleData());
    }

    @Test
    void gettingSingleRoleData()
    {
        account.addRole(1L);

        assertTrue(account.hasRole(1L));
        assertEquals("'1'", account.getRoleData());
    }

    @Test
    void gettingMultipleRoleData()
    {
        account.addRole(1L);
        account.addRole(2L);

        assertTrue(account.hasRole(1L));
        assertTrue(account.hasRole(2L));

        final String roleData = account.getRoleData();

        assertTrue(roleData.equals("'1,2'") || roleData.equals("'2,1'"));
    }

    @Test
    void accountBalanceZeroOnCreation()
    {
        assertEquals(0.0, account.getBalance());
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
        assertFalse(account.needsToSync());

        account.setBalance(10.0);

        assertTrue(account.needsToSync());
    }

    @Test
    void settingSameBalanceDoesntNeedToSync()
    {
        assertFalse(account.needsToSync());

        account.setBalance(0.0);

        assertFalse(account.needsToSync());
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
        assertFalse(account.needsToSync());

        account.addBalance(10.0);

        assertTrue(account.needsToSync());
    }

    @Test
    void addingZeroBalanceDoesntNeedToSync()
    {
        assertFalse(account.needsToSync());

        account.addBalance(0.0);

        assertFalse(account.needsToSync());
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
        account.addBalance(10.0);
        account.setNeedsToSync(false);

        assertFalse(account.needsToSync());

        account.removeBalance(10.0);

        assertTrue(account.needsToSync());
    }

    @Test
    void removingNoBalanceDoesntNeedToSync()
    {
        assertFalse(account.needsToSync());

        account.removeBalance(0.0);

        assertFalse(account.needsToSync());
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
        account.addBalance(10.0);
        account.setNeedsToSync(false);

        assertFalse(account.needsToSync());

        account.resetBalance();

        assertTrue(account.needsToSync());
    }

    @Test
    void resettingAlreadyZeroBalanceDoesntNeedToSync()
    {
        assertFalse(account.needsToSync());
        assertEquals(0.0, account.getBalance());

        account.resetBalance();

        assertFalse(account.needsToSync());
    }

    @Test
    void toStringWithNullUser()
    {
        assertEquals("Account(0)", account.toString());
    }

    @Nested
    class ConnectionTesting
    {

        private static Bot bot;

        @BeforeAll
        static void setUpAll()
        {
            bot = Testing.getBot();

            assertNotNull(bot);
            bot.connect();
        }

        private Account account;

        @BeforeEach
        void setUp()
        {
            account = new Account(Testing.BOT_ID);
        }

        @Test
        void getValidMemberFromNullGuild()
        {
            assertNull(account.getMember());
        }

        @Test
        void getValidMemberFromValidGuild()
        {
            final Guild guild = bot.getBot().getGuildById(Testing.GUILD_ID);

            assertNotNull(guild);

            final Member member = account.getMember(guild);

            assertNotNull(member);
            assertEquals(Testing.BOT_ID, member.getIdLong());
        }

        @Test
        void getInvalidMemberFromValidGuild()
        {
            final Guild guild = bot.getBot().getGuildById(Testing.GUILD_ID);

            assertNotNull(guild);

            final Member member = (new Account(0)).getMember(guild);

            assertNull(member);
        }

        @Test
        void getValidUserFromNullGuild()
        {
            assertNull(account.getUser());
        }

        @Test
        void getValidUserFromValidGuild()
        {
            final Guild guild = bot.getBot().getGuildById(Testing.GUILD_ID);

            assertNotNull(guild);

            final User user = account.getUser(guild);

            assertNotNull(user);
            assertEquals(Testing.BOT_ID, user.getIdLong());
        }

        @Test
        void getInvalidUserFromValidGuild()
        {
            final Guild guild = bot.getBot().getGuildById(Testing.GUILD_ID);

            assertNotNull(guild);

            final User user = (new Account(0)).getUser(guild);

            assertNull(user);
        }

        @Test
        void toStringValidGuildNonNullUser()
        {
            final Guild guild = bot.getBot().getGuildById(Testing.GUILD_ID);

            assertNotNull(guild);

            assertEquals("Account(" + Testing.BOT_ID + ", KodeKitten Testing#1981)", account.toString(guild));
        }

        @Test
        void toStringValidGuildNullUser()
        {
            final Guild guild = bot.getBot().getGuildById(Testing.GUILD_ID);

            assertNotNull(guild);

            final Account account = new Account(0);

            assertEquals("Account(0)", account.toString(guild));
        }

        @Test
        void hasRoleObject()
        {
            assertFalse(account.hasRoles());

            account.addRole(Testing.ROLE_BOT_ID);

            assertTrue(account.hasRole(Testing.ROLE_BOT_ID));

            final Guild guild = bot.getBot().getGuildById(Testing.GUILD_ID);

            assertNotNull(guild);

            final Role role = guild.getRoleById(Testing.ROLE_BOT_ID);

            assertNotNull(role);

            assertTrue(account.hasRole(role));
        }

        @Test
        void addRoleObject()
        {
            assertFalse(account.hasRoles());

            final Guild guild = bot.getBot().getGuildById(Testing.GUILD_ID);

            assertNotNull(guild);

            final Role role = guild.getRoleById(Testing.ROLE_BOT_ID);

            assertNotNull(role);

            account.addRole(role);

            assertTrue(account.hasRoles());
            assertTrue(account.hasRole(role));
            assertTrue(account.hasRole(Testing.ROLE_BOT_ID));
        }

        @Test
        void removeRoleObject()
        {
            assertFalse(account.hasRoles());

            final Guild guild = bot.getBot().getGuildById(Testing.GUILD_ID);

            assertNotNull(guild);

            final Role role = guild.getRoleById(Testing.ROLE_BOT_ID);

            assertNotNull(role);

            account.addRole(role);

            assertTrue(account.hasRoles());
            assertTrue(account.hasRole(role));
            assertTrue(account.hasRole(Testing.ROLE_BOT_ID));

            account.removeRole(role);

            assertFalse(account.hasRoles());
            assertFalse(account.hasRole(role));
            assertFalse(account.hasRole(Testing.ROLE_BOT_ID));
        }

        @Test
        void syncRolesFromNullServer()
        {
            account.addRole(1L);

            assertTrue(account.hasRoles());
            assertTrue(account.hasRole(1L));

            assertFalse(account.syncRolesFromServer());

            assertTrue(account.hasRoles());
            assertTrue(account.hasRole(1L));
        }

        @Test
        void syncRolesFromValidServer()
        {
            account.addRole(1L);

            assertTrue(account.hasRoles());
            assertTrue(account.hasRole(1L));

            final Guild guild = bot.getBot().getGuildById(Testing.GUILD_ID);

            assertNotNull(guild);

            assertTrue(account.syncRolesFromServer(guild));

            assertTrue(account.hasRoles());
            assertTrue(account.hasRole(Testing.ROLE_BOT_ID));
            assertTrue(account.hasRole(Testing.ROLE_KK_ID));
        }

        @Test
        void syncRolesFromValidServerNeedsToSync()
        {
            assertFalse(account.needsToSync());

            final Guild guild = bot.getBot().getGuildById(Testing.GUILD_ID);

            assertNotNull(guild);

            assertTrue(account.syncRolesFromServer(guild));

            assertTrue(account.hasRoles());
            assertTrue(account.hasRole(Testing.ROLE_BOT_ID));
            assertTrue(account.hasRole(Testing.ROLE_KK_ID));

            assertTrue(account.needsToSync());
        }

        @Test
        void syncRolesFromValidServerDoesntNeedToSync()
        {
            account.addRole(Testing.ROLE_BOT_ID);
            account.addRole(Testing.ROLE_KK_ID);
            account.setNeedsToSync(false);

            assertFalse(account.needsToSync());

            final Guild guild = bot.getBot().getGuildById(Testing.GUILD_ID);

            assertNotNull(guild);

            assertTrue(account.syncRolesFromServer(guild));

            assertTrue(account.hasRoles());
            assertTrue(account.hasRole(Testing.ROLE_BOT_ID));
            assertTrue(account.hasRole(Testing.ROLE_KK_ID));

            assertFalse(account.needsToSync());
        }

        @Test
        void syncRolesToInvalidServer()
        {
            account.addRole(Testing.ROLE_KK_ID);

            final Guild guild = bot.getBot().getGuildById(Testing.GUILD_ID);

            assertNotNull(guild);

            final Member member = account.getMember(guild);

            assertNotNull(member);
            assertEquals(2, member.getRoles().size());

            assertFalse(account.syncRolesToServer());

            assertNotEquals(1, member.getRoles().size());
        }

        @Test
        void syncRolesToValidServer()
        {
            account.addRole(Testing.ROLE_KK_ID);

            final Guild guild = bot.getBot().getGuildById(Testing.GUILD_ID);

            assertNotNull(guild);

            final Member member = account.getMember(guild);

            assertNotNull(member);
            assertEquals(2, member.getRoles().size());

            assertTrue(account.syncRolesToServer(guild));

            assertEquals(1, member.getRoles().size());
            assertEquals(Testing.ROLE_KK_ID, member.getRoles().get(0).getIdLong());

            final Role role = guild.getRoleById(Testing.ROLE_BOT_ID);

            assertNotNull(role);

            guild.addRoleToMember(member, role).complete();

            assertTrue(member.getRoles().contains(role));
        }

        @AfterAll
        static void afterAll()
        {
            bot.disconnect();
        }

    }

}