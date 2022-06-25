package com.sylink.account;

import com.sylink.KodeKitten;
import com.sylink.util.Snowflake;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class that contains all data about a user account.
 */
public class Account
{

    @Getter(AccessLevel.PUBLIC)
    private final long discordId;
    // Whether the Account has been loaded from the database.
    @Getter(AccessLevel.PUBLIC)
    private boolean loaded = false;
    // Last activity time to track how long an account has been inactive in memory.
    @Getter(AccessLevel.PUBLIC)
    private long lastActivityTime = 0;

    // List of permission strings for this account.
    @Getter(AccessLevel.PUBLIC)
    private final List<String> permissions = new ArrayList<>();
    // List of role ids for this account.
    @Getter(AccessLevel.PUBLIC)
    private final List<Long> roles = new ArrayList<>();
    @Getter(AccessLevel.PUBLIC)
    private double balance = 0.0;

    // Whether account information has been changed and needs to be synced to the database.
    private boolean needsToSync = false;

    protected Account(final long discordId)
    {
        this.discordId = discordId;
    }

    /**
     * Flags that the account has been loaded.
     */
    protected final void setLoaded()
    {
        this.loaded = true;
    }

    /**
     * @return The Discord user object of the Account.
     */
    public final User getUser()
    {
        return KodeKitten.getJdaBot().retrieveUserById(discordId).complete();
    }

    /**
     * @return The Discord member object of the Account.
     */
    public final Member getMember(@NonNull final Guild guild)
    {
        return guild.retrieveMemberById(discordId).complete();
    }

    public final Member getMember()
    {
        final Guild guild = Snowflake.getInstance().getMainGuild();

        return (guild == null) ? null : getMember(guild);
    }

    /**
     * Sets the last activity time to the current time.
     */
    public final void bumpLastActivityTime()
    {
        lastActivityTime = System.currentTimeMillis();
    }

    /**
     * Returns true if the last activity time of the account was over 10 minutes ago.
     */
    public final boolean isInactive()
    {
        int seconds = (int) ((System.currentTimeMillis() - lastActivityTime) / 1000);

        return seconds > 600;
    }

    /**
     * Returns true if the last activity time of the account was over 1 hour ago.
     */
    public final boolean isDead()
    {
        int seconds = (int) ((System.currentTimeMillis() - lastActivityTime) / 1000);

        return seconds > 3600;
    }

    /**
     * @return True if Account data has been changed and needs to be saved to the database.
     */
    protected final boolean needsToSync()
    {
        return needsToSync;
    }

    /**
     * Flags that the account needs to sync to database.
     */
    protected final void setNeedsToSync()
    {
        this.needsToSync = true;
    }

    /**
     * @return True if the account has at least 1 permission.
     */
    public final boolean hasPermissions()
    {
        return !permissions.isEmpty();
    }

    public final boolean hasPermission(@NonNull final String permission)
    {
        for (final String perm : permissions)
        {
            if (perm.equalsIgnoreCase(permission))
            {
                return true;
            }
        }

        return false;
    }

    public final void addPermission(@NonNull final String permission)
    {
        if (hasPermission(permission))
        {
            return;
        }

        this.permissions.add(permission);
        this.needsToSync = true;
    }

    public final void removePermission(@NonNull final String permission)
    {
        for (final String perm : permissions)
        {
            if (!perm.equalsIgnoreCase(permission))
            {
                continue;
            }

            this.permissions.remove(perm);
            this.needsToSync = true;
            return;
        }
    }

    /**
     * Removes all permissions from the account.
     */
    public final void clearPermissions()
    {
        permissions.clear();
    }

    /**
     * @return True if the account has at least 1 role.
     */
    public final boolean hasRoles()
    {
        return !roles.isEmpty();
    }

    public final boolean hasRole(final long roleId)
    {
        return roles.contains(roleId);
    }

    public final boolean hasRole(@NonNull final Role role)
    {
        return hasRole(role.getIdLong());
    }

    public final void addRole(final long roleId)
    {
        if (hasRole(roleId))
        {
            return;
        }

        this.roles.add(roleId);
        this.needsToSync = true;
    }

    public final void addRole(@NonNull final Role role)
    {
        addRole(role.getIdLong());
    }

    public final void removeRole(final long roleId)
    {
        if (!hasRole(roleId))
        {
            return;
        }

        this.roles.remove(roleId);
        this.needsToSync = true;
    }

    public final void removeRole(@NonNull final Role role)
    {
        removeRole(role.getIdLong());
    }

    /**
     * Clears all roles from the account.
     */
    public final void clearRoles()
    {
        roles.clear();
    }

    /**
     * Syncs internal account role data to the roles the user has on the main guild.
     */
    public final void syncRoles()
    {
        final Guild guild = Snowflake.getInstance().getMainGuild();

        if (guild != null)
        {
            final Member member = guild.retrieveMemberById(discordId).complete();

            if (member != null)
            {
                roles.clear();

                for (final Role role : member.getRoles())
                {
                    roles.add(role.getIdLong());
                }
            }
        }
    }

    public final void setBalance(final double balance)
    {
        // Balance cannot be less than 0.
        this.balance = Math.max(0, balance);
        this.needsToSync = true;
    }

    public final void addBalance(final double balance)
    {
        // Balance cannot be less than 0.
        this.balance = Math.max(0, this.balance + balance);
        this.needsToSync = true;
    }

    public final void removeBalance(final double balance)
    {
        // Balance cannot be less than 0.
        this.balance = Math.max(0, this.balance - balance);
        this.needsToSync = true;
    }

    /**
     * Resets the account's balance to 0.0
     */
    public final void resetBalance()
    {
        this.balance = 0.0;
        this.needsToSync = true;
    }

    /**
     * Loads permission data from the SQL column string to a list of permissions.
     */
    protected void loadPermissions(@NonNull final String permissionData)
    {
        permissions.clear();

        if (!permissionData.isBlank())
        {
            permissions.addAll(Arrays.asList(permissionData.split(",")));
        }
    }

    /**
     * Loads role data from the SQL column string to a list of roles.
     */
    protected void loadRoles(@NonNull final String roleData)
    {
        roles.clear();

        if (!roleData.isBlank())
        {
            for (final String role : roleData.split(","))
            {
                roles.add(Long.parseLong(role));
            }
        }
    }

    /**
     * @return The list of account permissions as a string of data.
     */
    protected final String getPermissionData()
    {
        if (permissions.isEmpty())
        {
            return "''";
        }

        final StringBuilder stringBuilder = new StringBuilder("'");

        permissions.forEach((permission) ->
        {
            if (stringBuilder.isEmpty())
            {
                stringBuilder.append(permission);
            }
            else
            {
                stringBuilder.append(permission).append(",");
            }
        });

        return stringBuilder.append("'").toString();
    }

    /**
     * @return The list of account roles as a string of data.
     */
    protected final String getRoleData()
    {
        if (roles.isEmpty())
        {
            return "''";
        }

        final StringBuilder stringBuilder = new StringBuilder("'");

        roles.forEach((roleId) ->
        {
            if (stringBuilder.isEmpty())
            {
                stringBuilder.append(roleId);
            }
            else
            {
                stringBuilder.append(roleId).append(",");
            }
        });

        return stringBuilder.append("'").toString();
    }

    @Override
    public final String toString()
    {
        return "Account(" + discordId + ")";
    }

}