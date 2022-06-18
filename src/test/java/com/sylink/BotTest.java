package com.sylink;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class BotTest
{

    @Test
    void constructorTest()
    {
        Bot bot = new Bot("abcd");

        assertEquals("abcd", bot.getToken());
        assertNull(bot.getBot());

        Bot secondBot = new Bot();

        assertNull(secondBot.getToken());
        assertNull(secondBot.getBot());
    }

    @Test
    void getSelfUserNullWhenNotConnected()
    {
        Bot bot = new Bot();

        assertNull(bot.getSelfUser());
    }

    @Test
    void isConnectedFalseWhenDisconnected()
    {
        Bot bot = new Bot();

        assertFalse(bot.isConnected());
    }

    @Test
    void settingTheToken()
    {
        Bot bot = new Bot("abcd");

        assertEquals("abcd", bot.getToken());

        bot.setToken("1234");

        assertEquals("1234", bot.getToken());
    }

    @Test
    void connectWithNewTokenChangesExistingToken()
    {
        Bot bot = new Bot("abcd");

        assertEquals("abcd", bot.getToken());

        try
        {
            bot.connect("1234");
        }
        catch (Throwable ignored)
        {

        }

        assertEquals("1234", bot.getToken());
    }

    @Test
    void getTokenFromFileNullWhenInvalidFile()
    {
        assertNull(Bot.getTokenFromFile(new File("invalid.txt")));
    }

}