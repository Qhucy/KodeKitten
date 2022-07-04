package com.sylink.commands;

import com.sylink.util.account.Account;
import com.sylink.util.account.AccountManager;
import com.sylink.util.Snowflake;
import com.sylink.util.config.MessageConfig;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.Arrays;
import java.util.Locale;

/**
 * Console only command for managing database testing and queries.
 */
public final class CmdDatabase
        extends Command
{

    public CmdDatabase()
    {
        super(CommandType.CONSOLE, "Console command used for managing database account data", """
                usages:
                  create [id]: Creates a new account with the given id.
                  load [id]: Load the account id from the database.
                  save [id]: Save the account id to the database.
                  flush [id]: Save the account id to the database and removes it from memory.
                  delete [id]: Deletes a given account id from the database and memory.
                  exists [id]: Prints whether the account id exists in the database and memory.
                  query [sqlQuery]: Executes the query to the SQL database.
                  update [id] [data] {[add:remove:clear]} [value]: Updates account data in memory.
                  check [id] [data]: Checks the value of a certain piece of account data.
                """, null, "database", "db", "sql");
    }

    @Override
    public String onConsoleCommand(@NonNull final String label, @NonNull final String[] args)
    {
        if (args.length < 2)
        {
            return super.consoleOutput(super.getUsage(label));
        }

        if (args[0].equalsIgnoreCase("query"))
        {
            final String query = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

            AccountManager.getInstance().executeQuery(query);

            return super.consoleOutput("executed_query");
        }

        long discordId;

        try
        {
            discordId = Long.parseLong(args[1]);
        }
        catch (final NumberFormatException exception)
        {
            return super.consoleOutput("proper_account_id");
        }

        Account account;

        switch (args[0].toLowerCase(Locale.ROOT))
        {
            case "create":
                if (AccountManager.getInstance().existsInMemory(discordId))
                {
                    return super.consoleOutput("account_already_exist");
                }

                AccountManager.getInstance().getAccount(discordId);

                return super.consoleOutput("created_account");
            case "load":
                if (!AccountManager.getInstance().existsInDatabase(discordId))
                {
                    return super.consoleOutput("account_no_exist");
                }

                account = AccountManager.getInstance().getAccount(discordId, false);

                if (account == null)
                {
                    return super.consoleOutput("cant_load_account");
                }

                return super.consoleOutput("loaded_account");
            case "save":
                account = AccountManager.getInstance().getAccount(discordId, false);

                if (account == null)
                {
                    return super.consoleOutput("account_no_exist");
                }

                AccountManager.getInstance().saveToDatabase(account);

                return super.consoleOutput("saved_account");
            case "flush":
                account = AccountManager.getInstance().getAccount(discordId, false);

                if (account == null)
                {
                    return super.consoleOutput("account_no_exist");
                }

                AccountManager.getInstance().flushFromMemory(account, true);

                return super.consoleOutput("saved_account");
            case "delete":
            case "del":
                account = AccountManager.getInstance().getAccount(discordId, false);

                if (account == null)
                {
                    return super.consoleOutput("account_no_exist");
                }

                AccountManager.getInstance().delete(account);

                return super.consoleOutput("deleted_account");
            case "exists":
                final StringBuilder output = new StringBuilder();

                if (AccountManager.getInstance().existsInMemory(discordId))
                {
                    output.append(MessageConfig.getInstance().getCommand("account_in_memory"));
                }
                else
                {
                    output.append(MessageConfig.getInstance().getCommand("account_not_in_memory"));
                }

                if (AccountManager.getInstance().existsInDatabase(discordId))
                {
                    output.append("\n").append(MessageConfig.getInstance().getCommand("account_in_sql"));
                }
                else
                {
                    output.append("\n").append(MessageConfig.getInstance().getCommand("account_not_in_sql"));
                }

                System.out.println(output);
                return output.toString();
            case "check":
                if (!AccountManager.getInstance().exists(discordId))
                {
                    return super.consoleOutput("account_no_exist");
                }

                account = AccountManager.getInstance().getAccount(discordId, false);

                if (account == null)
                {
                    return super.consoleOutput("cant_load_account");
                }

                StringBuilder display;

                switch (args[2].toLowerCase(Locale.ROOT))
                {
                    case "balance":
                    case "bal":
                        return super.consoleOutput("display_balance_other", account.getDiscordId(),
                                account.getBalance());
                    case "permissions":
                    case "permission":
                    case "perms":
                    case "perm":
                        if (!account.hasPermissions())
                        {
                            return super.consoleOutput("account_no_permissions");
                        }

                        display = new StringBuilder("\n");

                        for (final String permission : account.getPermissions())
                        {
                            if (display.isEmpty())
                            {
                                display.append(permission);
                            }
                            else
                            {
                                display.append(permission).append("\n");
                            }
                        }

                        return super.consoleOutput("display_permissions", display);
                    case "roles":
                    case "role":
                        if (!account.hasRoles())
                        {
                            return super.consoleOutput("account_no_roles");
                        }

                        display = new StringBuilder("\n");

                        for (final long roleId : account.getRoles())
                        {
                            final Role role = Snowflake.MAIN.getGuild().getRoleById(roleId);
                            final String roleName = (role == null) ? "null" : role.getName();

                            if (display.isEmpty())
                            {
                                display.append("(").append(roleId).append(") ").append(roleName);
                            }
                            else
                            {
                                display.append("(").append(roleId).append(") ").append(roleName).append("\n");
                            }
                        }

                        return super.consoleOutput("display_roles", display);
                    default:
                        return sendAvailableUpdateKeys();
                }
            case "update":
                if (!AccountManager.getInstance().exists(discordId))
                {
                    return super.consoleOutput("account_no_exist");
                }

                account = AccountManager.getInstance().getAccount(discordId, false);

                if (account == null)
                {
                    return super.consoleOutput("cant_load_account");
                }

                switch (args[2].toLowerCase(Locale.ROOT))
                {
                    case "balance":
                    case "bal":
                        double balance;

                        try
                        {
                            balance = Double.parseDouble(args[3]);
                        }
                        catch (final NumberFormatException exception)
                        {
                            return super.consoleOutput("value_double");
                        }

                        account.setBalance(balance);

                        return super.consoleOutput("set_balance_to", account.getBalance());
                    case "permissions":
                    case "permission":
                    case "perms":
                    case "perm":
                        if (args.length < 5)
                        {
                            return sendAvailableUpdateKeys();
                        }

                        final String permission = args[4].toLowerCase(Locale.ROOT);

                        switch (args[3].toLowerCase(Locale.ROOT))
                        {
                            case "add":
                                if (account.hasPermission(permission))
                                {
                                    super.consoleOutput("already_have_permission");
                                }

                                account.addPermission(permission);

                                return super.consoleOutput("added_permission", permission);
                            case "remove":
                                if (account.hasPermission(permission))
                                {
                                    account.removePermission(permission);

                                    return super.consoleOutput("removed_permission", permission);
                                }

                                return super.consoleOutput("account_no_permission");
                            case "clear":
                                if (account.hasPermissions())
                                {
                                    account.clearPermissions();

                                    return super.consoleOutput("cleared_permissions");
                                }

                                return super.consoleOutput("account_no_permissions");
                            default:
                                return sendAvailableUpdateKeys();
                        }
                    case "roles":
                    case "role":
                        if (args.length < 5)
                        {
                            return sendAvailableUpdateKeys();
                        }

                        long roleId;

                        try
                        {
                            roleId = Long.parseLong(args[4]);
                        }
                        catch (final NumberFormatException exception)
                        {
                            return super.consoleOutput("value_integer");
                        }

                        Role role;

                        switch (args[3].toLowerCase(Locale.ROOT))
                        {
                            case "add":
                                if (account.hasRole(roleId))
                                {
                                    return super.consoleOutput("already_have_role");
                                }

                                account.addRole(roleId);
                                role = Snowflake.MAIN.getGuild().getRoleById(discordId);

                                if (role != null)
                                {
                                    Snowflake.MAIN.getGuild().addRoleToMember(account.getDiscordId(), role).queue();
                                }

                                return super.consoleOutput("added_role", roleId);
                            case "remove":
                                if (account.hasRole(roleId))
                                {
                                    account.removeRole(roleId);
                                    role = Snowflake.MAIN.getGuild().getRoleById(discordId);

                                    if (role != null)
                                    {
                                        Snowflake.MAIN.getGuild().removeRoleFromMember(account.getDiscordId(), role).queue();
                                    }

                                    return super.consoleOutput("removed_role", roleId);
                                }

                                super.consoleOutput("no_role");
                            case "clear":
                                if (account.hasRoles())
                                {
                                    account.clearRoles();

                                    final Member member = account.getMember();

                                    if (member != null)
                                    {
                                        Snowflake.MAIN.getGuild().modifyMemberRoles(member).queue();
                                    }

                                    super.consoleOutput("cleared_roles");
                                }

                                super.consoleOutput("no_roles");
                            default:
                                return sendAvailableUpdateKeys();
                        }
                    default:
                        return sendAvailableUpdateKeys();
                }
            default:
                return super.consoleOutput(super.getUsage(label));
        }
    }

    /**
     * Prints to console the available keys to update in an account.
     */
    private String sendAvailableUpdateKeys()
    {
        return super.consoleOutput("""
                Available keys to update:
                  balance [double]
                  permission [add:remove:clear] [permission]
                  role [add:remove:clear] [permission]
                """);
    }

}