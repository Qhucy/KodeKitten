package com.sylink.account;

import com.sylink.KodeKitten;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    private List<String> permissions = new ArrayList<>();
    // List of role ids for this account.
    private List<AccountRole> roles = new ArrayList<>();
    @Getter(AccessLevel.PUBLIC)
    private double balance = 0.0;

    // Whether account information has been changed and needs to be synced to the database.
    private boolean needsToSync = false;

    protected Account(final long discordId)
    {
        this.discordId = discordId;
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
    public final boolean needsToSync()
    {
        return needsToSync;
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
            return;

        this.permissions.add(permission);
        this.needsToSync = true;
    }

    public final void removePermission(@NonNull final String permission)
    {
        for (final String perm : permissions)
        {
            if (!perm.equalsIgnoreCase(permission))
                continue;

            this.permissions.remove(perm);
            this.needsToSync = true;
            return;
        }
    }

    /**
     * @return True if the account has at least 1 role.
     */
    public final boolean hasRoles()
    {
        return !roles.isEmpty();
    }

    /**
     * @return True if the account has a given role.
     */
    public final boolean hasRole(@NonNull final AccountRole accountRole)
    {
        for (final AccountRole role : roles)
        {
            if (role.equals(accountRole))
                return true;
        }

        return false;
    }

    /**
     * @return True if the account has a given role.
     */
    public final boolean hasRole(final long roleId)
    {
        final AccountRole accountRole = AccountRole.fromId(roleId);

        if (accountRole == null)
        {
            return false;
        }
        else
        {
            return hasRole(accountRole);
        }
    }

    /**
     * @return True if the account has a given role.
     */
    public final boolean hasRole(@NonNull final Role role)
    {
        return hasRole(role.getIdLong());
    }

    public final void addRole(@NonNull final AccountRole accountRole)
    {
        if (hasRole(accountRole))
            return;

        this.roles.add(accountRole);
        this.needsToSync = true;
    }

    public final void addRole(final long roleId)
    {
        final AccountRole accountRole = AccountRole.fromId(roleId);

        if (accountRole == null)
            return;

        addRole((accountRole));
    }

    public final void addRole(@NonNull final Role role)
    {
        addRole(role.getIdLong());
    }

    public final void removeRole(@NonNull final AccountRole accountRole)
    {
        if (!hasRole(accountRole))
            return;

        this.roles.remove(accountRole);
        this.needsToSync = true;
    }

    public final void removeRole(final long roleId)
    {
        final AccountRole accountRole = AccountRole.fromId(roleId);

        if (accountRole == null)
            return;

        removeRole(accountRole);
    }

    public final void removeRole(@NonNull final Role role)
    {
        removeRole(role.getIdLong());
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
     * Loads all account data from the database connection and returns whether the data was loaded.
     */
    public boolean loadFromDatabase(@NonNull final Connection connection)
    {
        try(final Statement statement = connection.createStatement();
            final ResultSet resultSet = statement.executeQuery(String.format("""
                    SELECT
                        permissions,
                        balance
                    FROM
                        accounts
                    WHERE
                        id=%d;
                    """, discordId)))
        {
            if (resultSet.next())
            {
                loadPermissions(resultSet.getString("permissions"));
                loadRoles(resultSet.getString("roles"));
                balance = resultSet.getDouble("balance");
            }

            lastActivityTime = System.currentTimeMillis();

            this.loaded = true;
            return true;
        } catch (final SQLException sqlException)
        {
            sqlException.printStackTrace();
            return false;
        }
    }

    /**
     * Loads permission data from the SQL column string to a list of permissions.
     */
    private void loadPermissions(@NonNull final String permissionData)
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
    private void loadRoles(@NonNull final String roleData)
    {
        roles.clear();

        if (!roleData.isBlank())
        {
            for (final String role : roleData.split(","))
            {
                final long roleId = Long.parseLong(role);
                final AccountRole accountRole = AccountRole.fromId(roleId);

                roles.add(accountRole);
            }
        }
    }

    /**
     * Saves all data from this object to the account's column in the database.
     *
     * @return True if data was saved to the database if needed.
     */
    public boolean saveToDatabase(@NonNull final Connection connection)
    {
        if (!needsToSync)
            return true;

        try (final Statement statement = connection.createStatement())
        {
            // Insert into database as a new column.
            if (!existsInDatabase(connection))
            {
                statement.executeUpdate(String.format("INSERT INTO accounts (id,permissions,balance) VALUES(%s,%s,%g)", discordId, getPermissionData(), balance));
            }
            // Update the existing column with new data.
            else
            {
                statement.executeUpdate(String.format("""
                        UPDATE table
                        SET permissions = %s,
                            roles = %s,
                            balance = %g
                        WHERE id = %d
                        """, getPermissionData(), getRoleData(), balance, discordId));
            }

            // Data no longer needs to be updated.
            this.needsToSync = false;
            return true;
        } catch (final SQLException sqlException)
        {
            KodeKitten.logSevere("Unable to save account data for discord id " + discordId);
            sqlException.printStackTrace();
            return false;
        }
    }

    /**
     * Returns true if the account exists as a column in the database.
     */
    public boolean existsInDatabase(@NonNull final Connection connection)
    {
        try (final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery("SELECT id FROM accounts WHERE id=" + discordId))
        {
            return resultSet.next();
        } catch (final SQLException sqlException)
        {
            return false;
        }
    }

    /**
     * @return The list of account permissions as a string of data.
     */
    private String getPermissionData()
    {
        if (permissions.isEmpty())
            return "";

        final StringBuilder stringBuilder = new StringBuilder();

        for (final String permission : permissions)
        {
            if (stringBuilder.isEmpty())
            {
                stringBuilder.append(permission);
            }
            else
            {
                stringBuilder.append(permission).append(",");
            }
        }

        return stringBuilder.toString();
    }

    /**
     * @return The list of account roles as a string of data.
     */
    private String getRoleData()
    {
        if (roles.isEmpty())
            return "";

        final StringBuilder stringBuilder = new StringBuilder();

        for (final AccountRole role : roles)
        {
            if (stringBuilder.isEmpty())
            {
                stringBuilder.append(role.getRoleId());
            }
            else
            {
                stringBuilder.append(role.getRoleId()).append(",");
            }
        }

        return stringBuilder.toString();
    }

}