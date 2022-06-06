package com.sylink;

import lombok.NonNull;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.SelfUser;

import javax.annotation.Nullable;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class entry point of the program that handles bot set, bot startup connection to Discord, and bot shutdown.
 */
public final class KodeKitten
{

    private static final Logger logger = Logger.getLogger(KodeKitten.class.getName());

    private static Bot bot = null;

    /**
     * Processes setup of the bot and connection to Discord servers as well as console inputs and shutdown.
     */
    public static void main(@NonNull final String[] args)
    {
        bot = new Bot(Bot.findToken());

        bot.connect();

        if (!bot.isConnected())
        {
            System.exit(0);
            return;
        }

        // The bot is now connected to Discord.
        System.out.println(getBotUser().getName() + "#" + getBotUser().getDiscriminator() + " connected to Discord!");

        // Read console commands while the bot is running.
        readConsoleCommands();

        // The program is now exiting.
        System.out.println("Exiting the program");

        bot.disconnect();
        System.exit(0);
    }

    public static JDA getBot()
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
     * Continually reads console commands for the bot until 'stop' or 'exit' is entered.
     */
    private static void readConsoleCommands()
    {
        final Scanner scanner = new Scanner(System.in);

        String input;

        do
        {
            input = scanner.nextLine();


        } while (!input.equalsIgnoreCase("stop") && input.equalsIgnoreCase("exit"));

        scanner.close();
    }

    public static void log(@NonNull final Level logLevel, @Nullable final String... messages)
    {
        if (messages == null)
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
        if (messages == null)
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
        if (messages == null)
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
        if (messages == null)
        {
            return;
        }

        for (final String message : messages)
        {
            logger.severe(message);
        }
    }

}