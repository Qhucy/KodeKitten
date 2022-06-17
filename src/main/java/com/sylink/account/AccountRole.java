package com.sylink.account;

import com.sylink.KodeKitten;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Role;

/**
 * Class that holds data for an account's role with utility methods.
 */
public enum AccountRole
{

    MEMBER(1L, "Member");

    /**
     * @return The stored AccountRole from its attached role id.
     */
    public static AccountRole fromId(final long roleId)
    {
        for (final AccountRole accountRole : values())
        {
            if (accountRole.getRoleId() == roleId)
                return accountRole;
        }

        KodeKitten.logWarning(String.format("Invalid role retrieval from AccountRole data with role id %d", roleId));
        return null;
    }

    long roleId;
    String display;

    AccountRole(final long roleId, @NonNull final String display)
    {
        this.roleId = roleId;
        this.display = display;
    }

    public long getRoleId()
    {
        return roleId;
    }

    public String getDisplay()
    {
        return display;
    }

    /**
     * @return The Role object from the guild with the role id.
     */
    public Role retrieveRole()
    {
        return KodeKitten.getBot().getMainGuild().getRoleById(roleId);
    }

}