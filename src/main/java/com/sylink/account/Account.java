package com.sylink.account;

import com.sylink.util.Snowflake;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import javax.annotation.Nullable;
import java.util.*;

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
    private long lastActivityTime = System.currentTimeMillis();

    // List of permission strings for this account.
    @Getter(AccessLevel.PUBLIC)
    private final Set<String> permissions = new HashSet<>();
    // List of role ids for this account.
    @Getter(AccessLevel.PUBLIC)
    private final Set<Long> roles = new HashSet<>();
    @Getter(AccessLevel.PUBLIC)
    private double balance = 0.0;

    // Whether account information has been changed and needs to be synced to the database.
    private boolean needsToSync = false;

    protected Account(final long discordId)
    {
        this.discordId = discordId;
    }

    protected Account(final long discordId, final long lastActivityTime)
    {
        this.discordId = discordId;
        this.lastActivityTime = lastActivityTime;
    }

    /**
     * Flags that the account has been loaded.
     */
    protected final void setLoaded()
    {
        this.loaded = true;
    }

    /**
     * Sets the last activity time to the current time.
     */
    protected final void bumpLastActivityTime()
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
    protected final void setNeedsToSync(final boolean needsToSync)
    {
        this.needsToSync = needsToSync;
    }

    /**
     * @return The Discord member object of the Account in the given guild.
     */
    public final Member getMember(@NonNull final Guild guild)
    {
        try
        {
            return guild.retrieveMemberById(discordId).complete();
        }
        catch (final ErrorResponseException ignored)
        {
            return null;
        }
    }

    /**
     * @return The Discord member object of the Account in the main guild.
     */
    public final Member getMember()
    {
        final Guild guild = Snowflake.getInstance().getMainGuild();

        return (guild == null) ? null : getMember(guild);
    }

    /**
     * @return The Discord user object of the Account from the given guild.
     */
    public final User getUser(@NonNull final Guild guild)
    {
        final Member member = getMember(guild);

        return (member == null) ? null : member.getUser();
    }

    /**
     * @return The Discord user object of the Account from the main guild.
     */
    public final User getUser()
    {
        final Member member = getMember();

        return (member == null) ? null : member.getUser();
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
        return permissions.contains(permission.toLowerCase(Locale.ROOT));
    }

    public final void addPermission(@NonNull final String permission)
    {
        if (permission.isBlank() || hasPermission(permission))
        {
            return;
        }

        this.permissions.add(permission.toLowerCase(Locale.ROOT));
        this.needsToSync = true;
    }

    public final void removePermission(@NonNull final String permission)
    {
        if (permission.isBlank() || !hasPermission(permission))
        {
            return;
        }

        permissions.remove(permission.toLowerCase(Locale.ROOT));
        this.needsToSync = true;
    }

    /**
     * Removes all permissions from the account.
     */
    public final void clearPermissions()
    {
        if (permissions.isEmpty())
        {
            return;
        }

        permissions.clear();
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
            if (stringBuilder.length() != 1)
            {
                stringBuilder.append(",");
            }

            stringBuilder.append(permission.toLowerCase(Locale.ROOT));
        });

        return stringBuilder.append("'").toString();
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
        if (roles.isEmpty())
        {
            return;
        }

        roles.clear();
        this.needsToSync = true;
    }

    /**
     * Syncs internal account role data to the roles the user has on the given guild.
     */
    public final boolean syncRolesFromServer(@Nullable final Guild guild)
    {
        if (guild == null)
        {
            return false;
        }

        final Member member = guild.retrieveMemberById(discordId).complete();

        if (member == null)
        {
            return false;
        }

        final Set<Long> newRoles = new HashSet<>();

        for (final Role role : member.getRoles())
        {
            newRoles.add(role.getIdLong());
        }

        if (!newRoles.equals(roles))
        {
            roles.clear();
            roles.addAll(newRoles);

            this.needsToSync = true;
        }

        return true;
    }

    /**
     * Syncs internal account role data to the roles the user has on the main guild.
     */
    public final boolean syncRolesFromServer()
    {
        return syncRolesFromServer(Snowflake.getInstance().getMainGuild());
    }

    /**
     * Sets all the roles to the ones stored on this account's data on the given discord server.
     */
    public final boolean syncRolesToServer(@Nullable final Guild guild)
    {
        if (guild == null)
        {
            return false;
        }

        final Member member = guild.retrieveMemberById(discordId).complete();

        if (member == null)
        {
            return false;
        }

        final Set<Role> newRoles = new HashSet<>();

        for (final long roleId : roles)
        {
            final Role role = guild.getRoleById(roleId);

            if (role == null)
            {
                continue;
            }

            newRoles.add(role);
        }

        guild.modifyMemberRoles(member, newRoles).complete();

        return true;
    }

    /**
     * Sets all the roles to the ones stored on this account's data on the main discord server.
     */
    public final boolean syncRolesToServer()
    {
        return syncRolesToServer(Snowflake.getInstance().getMainGuild());
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
            if (stringBuilder.length() != 1)
            {
                stringBuilder.append(",");
            }

            stringBuilder.append(roleId);
        });

        return stringBuilder.append("'").toString();
    }

    public final void setBalance(final double balance)
    {
        if (this.balance == balance)
        {
            return;
        }

        // Balance cannot be less than 0.
        this.balance = Math.max(0, balance);
        this.needsToSync = true;
    }

    public final void addBalance(final double balance)
    {
        final double oldBalance = this.balance;

        // Balance cannot be less than 0.
        this.balance = Math.max(0, this.balance + balance);

        if (oldBalance != this.balance)
        {
            this.needsToSync = true;
        }
    }

    public final void removeBalance(final double balance)
    {
        final double oldBalance = this.balance;

        // Balance cannot be less than 0.
        this.balance = Math.max(0, this.balance - balance);

        if (oldBalance != this.balance)
        {
            this.needsToSync = true;
        }
    }

    /**
     * Resets the account's balance to 0.0
     */
    public final void resetBalance()
    {
        if (this.balance == 0.0)
        {
            return;
        }

        this.balance = 0.0;
        this.needsToSync = true;
    }

    @Override
    public final String toString()
    {
        return toString(Snowflake.getInstance().getMainGuild());
    }

    public final String toString(@Nullable final Guild guild)
    {
        if (guild != null)
        {
            final User user = getUser(guild);

            if (user != null)
            {
                return String.format("Account(%d, %s#%s)", discordId, user.getName(), user.getDiscriminator());
            }
        }

        return String.format("Account(%d)", discordId);
    }

}