package com.sylink;

import com.sylink.util.Snowflake;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.SelfUser;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Holds data for a JDA object and its connection token.
 */
public class Bot
{

    // The path to the file that contains the Discord bot token.
    private static final Path TOKEN_PATH = Paths.get("token.txt");

    /**
     * Tries to find and return a token from first the token path file and then from user input.
     */
    public static String findToken()
    {
        String token = getTokenFromFile(TOKEN_PATH.toFile());

        if (token == null)
        {
            System.out.println("Unable to find 'token.txt' from project directory.");

            return getTokenFromInput();
        }
        else
        {
            return token;
        }
    }

    /**
     * Returns the token from the given file.
     */
    public static String getTokenFromFile(@NonNull final File file)
    {
        try (final FileReader fileReader = new FileReader(file); final BufferedReader bufferedReader =
                new BufferedReader(fileReader))
        {
            final String token = bufferedReader.readLine();

            return (token == null || token.isBlank()) ? null : token;
        }
        catch (@NonNull final IOException ignored)
        {
            return null;
        }
    }

    /**
     * Returns the token from user input.
     */
    public static String getTokenFromInput()
    {
        final Scanner scanner = new Scanner(System.in);

        System.out.print("Enter Discord Bot Token: ");

        final String token = scanner.nextLine().trim();

        scanner.close();

        return (token.isEmpty()) ? null : token;
    }

    @Getter(AccessLevel.PUBLIC)
    private JDA bot = null;
    @Getter(AccessLevel.PUBLIC)
    private String token = null;

    public Bot(@NonNull final String token)
    {
        this.token = token;
    }

    public Bot()
    {

    }

    /**
     * Sets the token to a new token and disconnects any prior bot connections.
     */
    public void setToken(@NonNull final String token)
    {
        // Disconnect an existing connection when changing bot.
        if (bot != null)
        {
            bot.shutdown();
            bot = null;
        }

        this.token = token;
    }

    public SelfUser getSelfUser()
    {
        if (!isConnected())
        {
            sendConnectionError();
            return null;
        }
        else
        {
            return bot.getSelfUser();
        }
    }

    /**
     * Connects the Discord bot to the server.
     */
    public void connect()
    {
        try
        {
            bot = JDABuilder.createDefault(token).build();
            // Wait for the JDA object to be fully connected and ready.
            bot.awaitReady();
        }
        catch (@NonNull final LoginException | InterruptedException exception)
        {
            KodeKitten.logSevere("Unable to login to Discord servers, shutting down!");
            exception.printStackTrace();
        }
    }

    /**
     * Changes the token of the Discord bot and connects it to the server.
     */
    public void connect(@NonNull final String token)
    {
        setToken(token);
        connect();
    }

    /**
     * Disconnects the bot from the Discord servers and nullifies it.
     */
    public void disconnect()
    {
        if (bot != null)
        {
            bot.shutdown();
            bot = null;
        }
    }

    /**
     * Returns true if the bot is connected to Discord.
     */
    public boolean isConnected()
    {
        return bot != null;
    }

    /**
     * Sends a connection error message to console if trying to use the bot when it is disconnected.
     */
    private void sendConnectionError()
    {
        KodeKitten.logWarning("You have to connect the bot to Discord to use it!");

        // Print the stack trace.
        for (final StackTraceElement element : Thread.currentThread().getStackTrace())
        {
            System.out.println(element.toString());
        }
    }

    /**
     * Sets the status message of the bot.
     * If the status message is null it clears the status message.
     * If the activity type is null the default is a default status.
     */
    public void setStatus(@Nullable Activity.ActivityType activityType, @Nullable final String statusMessage)
    {
        if (!isConnected())
        {
            sendConnectionError();
            return;
        }

        if (statusMessage == null)
        {
            bot.getPresence().setActivity(null);
            return;
        }

        if (activityType == null)
        {
            activityType = Activity.ActivityType.DEFAULT;
        }

        bot.getPresence().setActivity(Activity.of(activityType, statusMessage));
        KodeKitten.logInfo("Changed status message to '" + statusMessage + "'");
    }

    public void setStatus(@Nullable final String statusMessage)
    {
        setStatus(null, statusMessage);
    }

}