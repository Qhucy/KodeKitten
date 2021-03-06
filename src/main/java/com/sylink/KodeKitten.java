package com.sylink;

import com.sylink.util.account.AccountManager;
import com.sylink.commands.*;
import com.sylink.util.config.MainConfig;
import com.sylink.util.SchedulerManager;
import com.sylink.util.Snowflake;
import com.sylink.util.config.MessageConfig;
import lombok.NonNull;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
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
     * (2) review all code for the entire program so far
     * Left to do (Command, CmdHelp, CmdDatabase, CmdBalance, CmdRole)
     * RIGHT NOW: Go over unit tests for commands, review code real quick for commands too
     * <p>
     * add command to talk in channels thru console
     * add role check command in console only
     * finish balance command
     * add coinflip command
     * ================
     * | UNIT TESTING |
     * ================
     * (3) figure out a way to test the output of commands (console, internal logic, etc)
     * (maybe will need a messages.toml file or similar to have same messages to test)
     * (4) Then redo tests for all commands
     * ============
     * | END GAME |
     * ============
     * Have code peer-reviewed
     * Have new programmer try to understand project to test comment quality
     */

    private static final Logger logger = Logger.getLogger(KodeKitten.class.getName());
    // The logging prefix for each logged console message.
    static final String LOGGING_FORMAT = "[%1$tF %1$tT] [%4$-7s] %5$s %n";

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
        if (!Bot.MAIN.connect() || !Bot.MAIN.isConnected())
        {
            System.exit(0);
            return;
        }

        // The bot is now connected to Discord.
        logInfo(Bot.MAIN.getSelfUser().getName() + "#" + Bot.MAIN.getSelfUser().getDiscriminator() + " connected to " + "Discord!");

        // Load all needed data.
        Snowflake.MAIN.loadFromConfig();
        Snowflake.MAIN.loadGuild(Bot.MAIN);
        MainConfig.getInstance().loadFromConfig();
        MessageConfig.getInstance().loadFromConfig();
        SchedulerManager.getInstance().startTimers();
        registerCommands();
        Bot.MAIN.getBot().addEventListener(new CommandHandler());

        // Read console commands while the bot is running.
        readConsoleCommands();

        // The program is now exiting.
        logInfo("Exiting the program");
        SchedulerManager.getInstance().stopTimers();
        Bot.MAIN.disconnect();
        AccountManager.getInstance().closeDatabaseConnection();
        System.exit(0);
    }

    /**
     * Sets up the format of the logger.
     */
    static void setupLogger()
    {
        System.setProperty("java.util.logging.SimpleFormatter.format", LOGGING_FORMAT);
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

            if (Command.runCommands(label, args) == null)
            {
                System.out.println(MessageConfig.getInstance().getInternal("unknown_command"));
            }
        }

        scanner.close();
    }

    /**
     * Saves a given resource ot a destination path.
     */
    public static void saveResource(@NonNull final String resourcePath, @NonNull final Path destinationPath)
    {
        try (final InputStream inputStream = getResource(resourcePath))
        {
            if (inputStream == null)
            {
                throw new IllegalArgumentException(String.format(MessageConfig.getInstance().getInternal(
                        "resource_not_found"), resourcePath));
            }

            final File destFile = destinationPath.toFile();
            final File destDir = new File(destinationPath.toString().substring(0,
                    Math.max(destinationPath.toString().lastIndexOf('/'), 0)));

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
    public static InputStream getResource(@NonNull final String resourcePath)
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