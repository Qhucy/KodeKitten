package com.sylink;

import com.sylink.account.AccountManager;
import com.sylink.commands.CmdHelp;
import com.sylink.commands.Command;
import com.sylink.commands.CommandHandler;
import com.sylink.util.SchedulerManager;
import lombok.NonNull;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.SelfUser;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class entry point of the program that handles bot set, bot startup connection to Discord, and bot shutdown.
 */
public final class KodeKitten
{

    /**
     * TODO
     * -----
     * <p>
     * test account saving / loading to database (NOT IN UNIT TESTS)
     * continue account testing with the help command
     * think about making all database saving/loading in AccountManager instead of accounts
     * push all to github
     * unit testing for command
     * timer for status messages so the status message changes every once in a while
     *
     * figure out unit tests for JDBC databases
     * https://stackoverflow.com/questions/266370/how-do-i-unit-test-jdbc-code-in-java
     *
     * go through code and make sure there's enough comments / clarity on things
     *
     * ACCOUNT ROLES
     * --------------
     * change to a singleton class that stores all roles from
     * loaded data roles.toml
     * with unit testing
     *
     * syncRoles method in Account.java to sync account roles with the guild.
     */

    private static final Logger logger = Logger.getLogger(KodeKitten.class.getName());

    private static Bot bot = null;

    /**
     * Processes setup of the bot and connection to Discord servers as well as console inputs and shutdown.
     */
    public static void main(@NonNull final String[] args)
    {
        setupLogger();

        // Whether or not a connection was successfully opened.
        if (!AccountManager.getInstance().openDatabaseConnection())
        {
            System.exit(0);
            return;
        }

        bot = new Bot(Bot.findToken());

        bot.connect();

        if (!bot.isConnected())
        {
            System.exit(0);
            return;
        }

        // The bot is now connected to Discord.
        logInfo(getBotUser().getName() + "#" + getBotUser().getDiscriminator() + " connected to Discord!");

        SchedulerManager.getInstance().startTimers();
        getJdaBot().addEventListener(new CommandHandler());
        registerCommands();

        bot.setStatus("hiya :3");

        // Read console commands while the bot is running.
        readConsoleCommands();

        // The program is now exiting.
        logInfo("Exiting the program");

        SchedulerManager.getInstance().stopTimers();
        bot.disconnect();
        AccountManager.getInstance().closeDatabaseConnection();
        System.exit(0);
    }

    /**
     * Sets up the format of the logger.
     */
    private static void setupLogger()
    {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$-7s] %5$s %n");
    }

    public static Bot getBot()
    {
        return bot;
    }

    public static JDA getJdaBot()
    {
        return bot.getBot();
    }

    /**
     * Returns the JDA Bot's SelfUser.
     */
    public static SelfUser getBotUser()
    {
        return bot.getSelfUser();
    }

    public static String getBotToken()
    {
        return bot.getToken();
    }

    /**
     * Registers all commands to Discord and our internal command system.
     * NOTE: Register them as normal commands instead of guild commands for deployment.
     */
    private static void registerCommands()
    {
        Command.registerGuildCommand(new CmdHelp());
    }

    /**
     * Continually reads console commands for the bot until 'stop' or 'exit' is entered.
     */
    private static void readConsoleCommands()
    {
        final Scanner scanner = new Scanner(System.in);

        String input;

        while (true)
        {
            input = scanner.nextLine();

            if (input.equalsIgnoreCase("stop") || input.equalsIgnoreCase("exit"))
            {
                break;
            }

            final String[] inputSplit = input.split(" ");

            final String label = inputSplit[0];
            final String[] args = Arrays.copyOfRange(inputSplit, 1, inputSplit.length);

            if (!Command.runCommands(label, args))
            {
                System.out.println("Unknown command. Enter 'help' for help or 'exit' to exit.");
            }
        }

        scanner.close();
    }

    public static void log(@NonNull final Level logLevel, @Nullable final String... messages)
    {
        if (messages == null || messages.length == 0)
        {
            return;
        }

        for (final String message : messages)
        {
            logger.log(logLevel, message);
        }
    }

    public static void logInfo(@Nullable final String... messages)
    {
        if (messages == null || messages.length == 0)
        {
            return;
        }

        for (final String message : messages)
        {
            logger.info(message);
        }
    }

    public static void logWarning(@Nullable final String... messages)
    {
        if (messages == null || messages.length == 0)
        {
            return;
        }

        for (final String message : messages)
        {
            logger.warning(message);
        }
    }

    public static void logSevere(@Nullable final String... messages)
    {
        if (messages == null || messages.length == 0)
        {
            return;
        }

        for (final String message : messages)
        {
            logger.severe(message);
        }
    }

}