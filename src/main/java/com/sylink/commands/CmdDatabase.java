package com.sylink.commands;

import com.sylink.account.Account;
import com.sylink.account.AccountManager;
import lombok.NonNull;

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
        super(CommandType.CONSOLE, "", """
                usages:
                  create [id]: Creates a new account with the given id.
                  load [id]: Load the account id from the database.
                  save [id]: Save the account id to the database.
                  exists [id]: Prints whether the account id exists in the database and memory.
                  query [sqlQuery]: Executes the query to the SQL database.
                  update [id] [data] [value]: Updates account data in memory.
                  check [id] [data]: Checks the value of a certain piece of account data.
                """, null, "database", "db", "sql");
    }

    @Override
    public void onConsoleCommand(@NonNull final String label, @NonNull final String[] args)
    {
        if (args.length < 2)
        {
            super.sendUsage(label);
        }
        else
        {
            if (args[0].equalsIgnoreCase("query"))
            {
                final String query = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

                AccountManager.getInstance().executeQuery(query);
                System.out.println("Executed the query.");
            }
            else
            {
                long discordId;

                try
                {
                    discordId = Long.parseLong(args[1]);
                } catch (final NumberFormatException exception)
                {
                    System.out.println("You must input a valid number for the id");
                    return;
                }

                Account account = null;

                switch (args[0].toLowerCase(Locale.ROOT))
                {
                    case "create":
                        if (AccountManager.getInstance().existsInMemory(discordId))
                        {
                            System.out.println("That account id already exists in memory");
                        }
                        else
                        {
                            AccountManager.getInstance().getAccount(discordId);
                            System.out.println("Created the account in the system");
                        }
                        break;
                    case "load":
                        if (!AccountManager.getInstance().existsInDatabase(discordId))
                        {
                            System.out.println("That account id does not exist in the database");
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
                                System.out.println("Loaded the account from the database");
                            }
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
                                switch (args[2].toLowerCase(Locale.ROOT))
                                {
                                    case "balance":
                                    case "bal":
                                        System.out.printf("This account's balance is $%g\n", account.getBalance());
                                        break;
                                    default:
                                        System.out.println("""
                                                Available keys to check:
                                                  balance
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
                                switch(args[2].toLowerCase(Locale.ROOT))
                                {
                                    case "balance":
                                    case "bal":
                                        double balance;

                                        try
                                        {
                                            balance = Double.parseDouble(args[3]);
                                        } catch(final NumberFormatException exception)
                                        {
                                            System.out.println("Value must be a double");
                                            return;
                                        }

                                        account.setBalance(balance);

                                        System.out.printf("Set account %d's balance to $%g\n", account.getDiscordId(), account.getBalance());
                                        break;
                                    default:
                                        System.out.println("""
                                                Available keys to update:
                                                  balance [double]
                                                """);
                                }
                            }
                        }
                        break;
                    default:
                        sendUsage(label);
                }
            }
        }
    }

}