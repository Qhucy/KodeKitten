package com.sylink;

import com.sylink.account.AccountManager;
import com.sylink.commands.*;
import com.sylink.util.ConfigManager;
import com.sylink.util.SchedulerManager;
import com.sylink.util.Snowflake;
import lombok.NonNull;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.SelfUser;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
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
     * ========
     * | TODO |
     * ========
     * (1) Write Unit tests for KodeKitten.java
     * (2) review all code for the entire program so far
     * (KodeKitten, Bot) CURRENTLY ON: Account
     * add command to talk in channels thru console
     * add role check command in console only
     * finish balance command
     * add coinflip command
     *
     * Develop TEST discord bot to help run unit tests with JDA
     * Have the test discord server mirror actual one and methods to use
     * snowflakes for test instead of real thing
     * ================
     * | UNIT TESTING |
     * ================
     * (3) figure out a way to test the output of commands (console, internal logic, etc)
     * (maybe will need a messages.toml file or similar to have same messages to test)
     * (4) Then redo tests for all commands
     * unit tests for KodeKitten.java main setup startup methods?
     */

    /**
     * =============
     * | IMPORTANT |
     * =============
     * The path to the text file that contains the Discord Bot Token for the testing bot.
     * This is used for unit testing and must be correct to run tests.
     */
    public static final Path TEST_TOKEN_PATH = Paths.get("../test_token.txt");
    /**
     * The path to the test snowflake config that contains the Discord Snowflake Ids
     * for the unit test bot to use in the testing discord server.
     */
    public static final String TEST_SNOWFLAKE_PATH = "../test_snowflake.toml";

    private static final Logger logger = Logger.getLogger(KodeKitten.class.getName());

    // The object that manages the Discord connection of the bot.
    private static Bot bot = null;

    // Internal list of all registered commands.
    private static final Command[] commands = new Command[]{new CmdHelp(), new CmdBalance(), new CmdDatabase()};

    /**
     * Processes setup of the bot and connection to Discord servers as well as console inputs and shutdown.
     */
    public static void main(@NonNull final String[] args)
    {
        setupLogger();

        // Exits the program if the connection couldn't be opened.
        if (!AccountManager.getInstance().openDatabaseConnection())
        {
            System.exit(0);
            return;
        }

        // Retrieves the token from startup arguments otherwise finds it elsewhere.
        bot = new Bot((args.length > 0) ? args[0] : Bot.findToken());

        bot.connect();

        if (!bot.isConnected())
        {
            System.exit(0);
            return;
        }

        // The bot is now connected to Discord.
        logInfo(getBotUser().getName() + "#" + getBotUser().getDiscriminator() + " connected to Discord!");

        // Load all needed data.
        Snowflake.getInstance().loadFromConfig();
        Snowflake.getInstance().loadMainGuild();
        ConfigManager.getInstance().load();
        SchedulerManager.getInstance().startTimers();
        registerCommands();
        getJdaBot().addEventListener(new CommandHandler());

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

    /**
     * @return The unit testing bot that is separate from the main discord bot.
     */
    public static Bot getTestBot()
    {
        final String token = Bot.getTokenFromFile(TEST_TOKEN_PATH.toFile());

        return (token == null) ? null : new Bot(token);
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
        for (final Command command : commands)
        {
            Command.registerGuildCommand(command);
            //Command.registerCommand(command);
        }
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

    /**
     * Saves a given resource ot a destination path.
     */
    public static void saveResource(@NonNull final String resourcePath, @NonNull final String destinationPath)
    {
        try (final InputStream inputStream = getResource(resourcePath))
        {
            if (inputStream == null)
            {
                throw new IllegalArgumentException(String.format("Unable to find resource %s", resourcePath));
            }

            final File destFile = new File(destinationPath);
            final File destDir = new File(destinationPath.substring(0, Math.max(destinationPath.lastIndexOf('/'), 0)));

            if (!destDir.exists())
            {
                destDir.mkdirs();
            }

            try (final OutputStream outputStream = new FileOutputStream(destFile))
            {
                byte[] buffer = new byte[1024];
                int length;

                while ((length = inputStream.read(buffer)) > 0)
                {
                    outputStream.write(buffer, 0, length);
                }
            }
        }
        catch (final IOException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * @return The input stream of the given file resource path.
     */
    private static InputStream getResource(@NonNull final String resourcePath)
    {
        try
        {
            final URL resource = KodeKitten.class.getClassLoader().getResource(resourcePath);

            if (resource == null)
            {
                return null;
            }

            final URLConnection connection = resource.openConnection();

            connection.setUseCaches(false);

            return connection.getInputStream();
        }
        catch (final IOException exception)
        {
            exception.printStackTrace();
        }

        return null;
    }

    public static void log(@NonNull final Level logLevel, @NonNull final String... messages)
    {
        if (messages.length == 0)
        {
            return;
        }

        for (final String message : messages)
        {
            logger.log(logLevel, message);
        }
    }

    public static void logInfo(@NonNull final String... messages)
    {
        log(Level.INFO, messages);
    }

    public static void logWarning(@NonNull final String... messages)
    {
        log(Level.WARNING, messages);
    }

    public static void logSevere(@NonNull final String... messages)
    {
        log(Level.SEVERE, messages);
    }

}