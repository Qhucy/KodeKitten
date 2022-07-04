package com.sylink.commands;

import com.sylink.util.account.Account;
import com.sylink.util.account.AccountManager;
import com.sylink.util.Snowflake;
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

            return super.consoleOutput("Executed the query.");
        }

        long discordId;

        try
        {
            discordId = Long.parseLong(args[1]);
        }
        catch (final NumberFormatException exception)
        {
            return super.consoleOutput("You must input a valid number for the id");
        }

        Account account;

        switch (args[0].toLowerCase(Locale.ROOT))
        {
            case "create":
                if (AccountManager.getInstance().existsInMemory(discordId))
                {
                    return super.consoleOutput("That account id already exists in memory");
                }

                AccountManager.getInstance().getAccount(discordId);

                return super.consoleOutput("Created the account in the system");
            case "load":
                if (!AccountManager.getInstance().existsInDatabase(discordId))
                {
                    return super.consoleOutput("That account id does not exist in the database");
                }

                account = AccountManager.getInstance().getAccount(discordId, false);

                if (account == null)
                {
                    return super.consoleOutput("Unable to load the account from the database");
                }

                return super.consoleOutput("Loaded the account from the database");
            case "save":
                account = AccountManager.getInstance().getAccount(discordId, false);

                if (account == null)
                {
                    return super.consoleOutput("This account does not exist in data to be saved");
                }

                AccountManager.getInstance().saveToDatabase(account);

                return super.consoleOutput("Saved the account to the database");
            case "flush":
                account = AccountManager.getInstance().getAccount(discordId, false);

                if (account == null)
                {
                    return super.consoleOutput("This account does not exist in data to be flushed");
                }

                AccountManager.getInstance().flushFromMemory(account, true);

                return super.consoleOutput("Saved the account to the database");
            case "delete":
            case "del":
                account = AccountManager.getInstance().getAccount(discordId, false);

                if (account == null)
                {
                    return super.consoleOutput("This account does not exist");
                }

                AccountManager.getInstance().delete(account);

                return super.consoleOutput("Deleted this account from the database and memory");
            case "exists":
                final StringBuilder output = new StringBuilder();

                if (AccountManager.getInstance().existsInMemory(discordId))
                {
                    output.append("This account exists in memory");
                }
                else
                {
                    output.append("This account does not exist in memory");
                }

                if (AccountManager.getInstance().existsInDatabase(discordId))
                {
                    output.append("\n").append("This account exists in the SQL Database");
                }
                else
                {
                    output.append("\n").append("This account does not exist in the SQL Database");
                }

                System.out.println(output);
                return output.toString();
            case "check":
                if (!AccountManager.getInstance().exists(discordId))
                {
                    return super.consoleOutput("This account does not exist in data");
                }

                account = AccountManager.getInstance().getAccount(discordId, false);

                if (account == null)
                {
                    return super.consoleOutput("Unable to load the account from the database");
                }

                StringBuilder display;

                switch (args[2].toLowerCase(Locale.ROOT))
                {
                    case "balance":
                    case "bal":
                        return super.consoleOutput("This account's balance is $%g", account.getBalance());
                    case "permissions":
                    case "permission":
                    case "perms":
                    case "perm":
                        if (!account.hasPermissions())
                        {
                            return super.consoleOutput("This account does not have any permissions");
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

                        return super.consoleOutput("Permissions on this account:", display);
                    case "roles":
                    case "role":
                        if (!account.hasRoles())
                        {
                            return super.consoleOutput("This account does not have any roles");
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

                        return super.consoleOutput("Roles on this account:", display);
                    default:
                        return super.consoleOutput("""
                                Available keys to check:
                                  balance
                                  permissions
                                  roles
                                """);
                }
            case "update":
                if (!AccountManager.getInstance().exists(discordId))
                {
                    return super.consoleOutput("This account does not exist in data");
                }

                account = AccountManager.getInstance().getAccount(discordId, false);

                if (account == null)
                {
                    return super.consoleOutput("Unable to load the account from the database");
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
                            return super.consoleOutput("Value must be a double");
                        }

                        account.setBalance(balance);

                        return super.consoleOutput("Set account's balance to $%g", account.getBalance());
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
                                    super.consoleOutput("This account already has this permission");
                                }

                                account.addPermission(permission);

                                return super.consoleOutput("Added permission '%s' to this account", permission);
                            case "remove":
                                if (account.hasPermission(permission))
                                {
                                    account.removePermission(permission);

                                    return super.consoleOutput("Removed permission '%s' from this account", permission);
                                }

                                return super.consoleOutput("This account does not have this permission");
                            case "clear":
                                if (account.hasPermissions())
                                {
                                    account.clearPermissions();

                                    return super.consoleOutput("Cleared all permissions from this account");
                                }

                                return super.consoleOutput("This account does not have any permissions to clear");
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
                            return super.consoleOutput("The role id must be a number");
                        }

                        Role role;

                        switch (args[3].toLowerCase(Locale.ROOT))
                        {
                            case "add":
                                if (account.hasRole(roleId))
                                {
                                    return super.consoleOutput("This account already has this role");
                                }

                                account.addRole(roleId);
                                role = Snowflake.MAIN.getGuild().getRoleById(discordId);

                                if (role != null)
                                {
                                    Snowflake.MAIN.getGuild().addRoleToMember(account.getDiscordId(), role).queue();
                                }

                                return super.consoleOutput("Added role %d to this account", roleId);
                            case "remove":
                                if (account.hasRole(roleId))
                                {
                                    account.removeRole(roleId);
                                    role = Snowflake.MAIN.getGuild().getRoleById(discordId);

                                    if (role != null)
                                    {
                                        Snowflake.MAIN.getGuild().removeRoleFromMember(account.getDiscordId(), role).queue();
                                    }

                                    return super.consoleOutput("Removed role %d from this account", roleId);
                                }

                                super.consoleOutput("This account does not have this role");
                            case "clear":
                                if (account.hasRoles())
                                {
                                    account.clearRoles();

                                    final Member member = account.getMember();

                                    if (member != null)
                                    {
                                        Snowflake.MAIN.getGuild().modifyMemberRoles(member).queue();
                                    }

                                    super.consoleOutput("Cleared all roles from this account");
                                }

                                super.consoleOutput("This account does not have any roles to clear");
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