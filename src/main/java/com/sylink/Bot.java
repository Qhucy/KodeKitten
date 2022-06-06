package com.sylink;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.SelfUser;

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
        try (final FileReader fileReader = new FileReader(file);
             final BufferedReader bufferedReader = new BufferedReader(fileReader))
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
        return (bot == null) ? null : bot.getSelfUser();
    }

    /**
     * Connects the Discord bot to the server.
     */
    public void connect()
    {
        try
        {
            bot = JDABuilder.createDefault(token).build();
        } catch (@NonNull final LoginException loginException)
        {
            KodeKitten.logSevere("Unable to login to Discord servers, shutting down!");

            loginException.printStackTrace();
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

}