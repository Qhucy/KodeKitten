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
    public void onConsoleCommand(@NonNull final String label, @NonNull final String[] args)
    {
        if (args.length < 2)
        {
            super.sendUsage(label);
            return;
        }

        if (args[0].equalsIgnoreCase("query"))
        {
            final String query = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

            AccountManager.getInstance().executeQuery(query);
            System.out.println("Executed the query.");
            return;
        }

        long discordId;

        try
        {
            discordId = Long.parseLong(args[1]);
        }
        catch (final NumberFormatException exception)
        {
            System.out.println("You must input a valid number for the id");
            return;
        }

        Account account;

        switch (args[0].toLowerCase(Locale.ROOT))
        {
            case "create":
                if (AccountManager.getInstance().existsInMemory(discordId))
                {
                    System.out.println("That account id already exists in memory");
                    return;
                }

                AccountManager.getInstance().getAccount(discordId);
                System.out.println("Created the account in the system");
                break;
            case "load":
                if (!AccountManager.getInstance().existsInDatabase(discordId))
                {
                    System.out.println("That account id does not exist in the database");
                    return;
                }

                account = AccountManager.getInstance().getAccount(discordId, false);

                if (account == null)
                {
                    System.out.println("Unable to load the account from the database");
                }
                else
                {
                    System.out.println("Loaded the account from the database");
                }
                break;
            case "save":
                account = AccountManager.getInstance().getAccount(discordId, false);

                if (account == null)
                {
                    System.out.println("This account does not exist in data to be saved");
                }
                else
                {
                    AccountManager.getInstance().saveToDatabase(account);
                    System.out.println("Saved the account to the database");
                }
                break;
            case "flush":
                account = AccountManager.getInstance().getAccount(discordId, false);

                if (account == null)
                {
                    System.out.println("This account does not exist in data to be flushed");
                }
                else
                {
                    AccountManager.getInstance().flushFromMemory(account, true);
                    System.out.println("Saved the account to the database");
                }
                break;
            case "delete":
            case "del":
                account = AccountManager.getInstance().getAccount(discordId, false);

                if (account == null)
                {
                    System.out.println("This account does not exist");
                }
                else
                {
                    AccountManager.getInstance().delete(account);
                    System.out.println("Deleted this account from the database and memory");
                }
                break;
            case "exists":
                if (AccountManager.getInstance().existsInMemory(discordId))
                {
                    System.out.println("This account exists in memory");
                }
                else
                {
                    System.out.println("This account does not exist in memory");
                }

                if (AccountManager.getInstance().existsInDatabase(discordId))
                {
                    System.out.println("This account exists in the SQL Database");
                }
                else
                {
                    System.out.println("This account does not exist in the SQL Database");
                }
                break;
            case "check":
                if (!AccountManager.getInstance().exists(discordId))
                {
                    System.out.println("This account does not exist in data");
                }
                else
                {
                    account = AccountManager.getInstance().getAccount(discordId, false);

                    if (account == null)
                    {
                        System.out.println("Unable to load the account from the database");
                    }
                    else
                    {
                        StringBuilder display;

                        switch (args[2].toLowerCase(Locale.ROOT))
                        {
                            case "balance":
                            case "bal":
                                System.out.printf("This account's balance is $%g\n", account.getBalance());
                                break;
                            case "permissions":
                            case "permission":
                            case "perms":
                            case "perm":
                                if (!account.hasPermissions())
                                {
                                    System.out.println("This account does not have any permissions");
                                    return;
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

                                System.out.println("Permissions on this account:" + display);
                                break;
                            case "roles":
                            case "role":
                                if (!account.hasRoles())
                                {
                                    System.out.println("This account does not have any roles");
                                    return;
                                }

                                display = new StringBuilder("\n");

                                for (final long roleId : account.getRoles())
                                {
                                    final Role role =
                                            Snowflake.MAIN.getGuild().getRoleById(roleId);
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

                                System.out.println("Roles on this account:" + display);
                                break;
                            default:
                                System.out.println("""
                                                Available keys to check:
                                                  balance
                                                  permissions
                                                  roles
                                                """);
                        }
                    }
                }
            case "update":
                if (!AccountManager.getInstance().exists(discordId))
                {
                    System.out.println("This account does not exist in data");
                }
                else
                {
                    account = AccountManager.getInstance().getAccount(discordId, false);

                    if (account == null)
                    {
                        System.out.println("Unable to load the account from the database");
                    }
                    else
                    {
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
                                    System.out.println("Value must be a double");
                                    return;
                                }

                                account.setBalance(balance);

                                System.out.printf("Set account's balance to $%g\n", account.getBalance());
                                break;
                            case "permissions":
                            case "permission":
                            case "perms":
                            case "perm":
                                if (args.length < 5)
                                {
                                    sendAvailableUpdateKeys();
                                    return;
                                }

                                final String permission = args[4].toLowerCase(Locale.ROOT);

                                switch (args[3].toLowerCase(Locale.ROOT))
                                {
                                    case "add":
                                        if (account.hasPermission(permission))
                                        {
                                            System.out.println("This account already has this permission");
                                        }
                                        else
                                        {
                                            account.addPermission(permission);
                                            System.out.println("Added permission '" + permission + "' to " +
                                                    "this" + " account");
                                        }
                                        break;
                                    case "remove":
                                        if (account.hasPermission(permission))
                                        {
                                            account.removePermission(permission);
                                            System.out.println("Removed permission '" + permission + "' from "
                                                    + "this account");
                                        }
                                        else
                                        {
                                            System.out.println("This account does not have this permission");
                                        }
                                        break;
                                    case "clear":
                                        if (account.hasPermissions())
                                        {
                                            account.clearPermissions();
                                            System.out.println("Cleared all permissions from this account");
                                        }
                                        else
                                        {
                                            System.out.println("This account does not have any permissions " + "to" + " clear");
                                        }
                                        break;
                                    default:
                                        sendAvailableUpdateKeys();
                                }
                                break;
                            case "roles":
                            case "role":
                                if (args.length < 5)
                                {
                                    sendAvailableUpdateKeys();
                                    return;
                                }

                                long roleId;

                                try
                                {
                                    roleId = Long.parseLong(args[4]);
                                }
                                catch (final NumberFormatException exception)
                                {
                                    System.out.println("The role id must be a number");
                                    return;
                                }

                                switch (args[3].toLowerCase(Locale.ROOT))
                                {
                                    case "add":
                                        if (account.hasRole(roleId))
                                        {
                                            System.out.println("This account already has this role");
                                        }
                                        else
                                        {
                                            account.addRole(roleId);
                                            final Role role =
                                                    Snowflake.MAIN.getGuild().getRoleById(discordId);

                                            if (role != null)
                                            {
                                                Snowflake.MAIN.getGuild().addRoleToMember(account.getDiscordId(), role).queue();
                                            }

                                            System.out.println("Added role " + roleId + " to this account");
                                        }
                                        break;
                                    case "remove":
                                        if (account.hasRole(roleId))
                                        {
                                            account.removeRole(roleId);
                                            final Role role =
                                                    Snowflake.MAIN.getGuild().getRoleById(discordId);

                                            if (role != null)
                                            {
                                                Snowflake.MAIN.getGuild().removeRoleFromMember(account.getDiscordId(), role).queue();
                                            }

                                            System.out.println("Removed role " + roleId + " from this account");
                                        }
                                        else
                                        {
                                            System.out.println("This account does not have this role");
                                        }
                                        break;
                                    case "clear":
                                        if (account.hasRoles())
                                        {
                                            account.clearRoles();

                                            final Member member = account.getMember();

                                            if (member != null)
                                            {
                                                Snowflake.MAIN.getGuild().modifyMemberRoles(member).queue();
                                            }

                                            System.out.println("Cleared all roles from this account");
                                        }
                                        else
                                        {
                                            System.out.println("This account does not have any roles to clear");
                                        }
                                        break;
                                    default:
                                        sendAvailableUpdateKeys();
                                }
                                break;
                            default:
                                sendAvailableUpdateKeys();
                        }
                    }
                }
                break;
            default:
                sendUsage(label);
        }
    }

    /**
     * Prints to console the available keys to update in an account.
     */
    private void sendAvailableUpdateKeys()
    {
        System.out.println("""
                Available keys to update:
                  balance [double]
                  permission [add:remove:clear] [permission]
                  role [add:remove:clear] [permission]
                """);
    }

}