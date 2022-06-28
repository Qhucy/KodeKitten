package com.sylink;

import com.sylink.util.Testing;
import net.dv8tion.jda.api.entities.Activity;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class BotTest
{

    // The test token.txt file used for unit testing.
    private static final Path TOKEN_PATH = Paths.get("src/test/java/com/sylink/token_test.txt");
    // The path to a blank text file used for unit testing.
    private static final Path BLANK_TOKEN_PATH = Paths.get("src/test/java/com/sylink/blank_token_test.txt");
    // An example invalid path used for unit testing.
    private static final Path INVALID_PATH = Paths.get("src/test/java/com/sylink/token_test_invalid.txt");

    @Test
    void retrieveTokenFromFile()
    {
        String token = Bot.getTokenFromFile(TOKEN_PATH.toFile());

        assertNotNull(token);
        assertEquals("abcd", token);
    }

    @Test
    void retrieveTokenFromBlankFile()
    {
        String token = Bot.getTokenFromFile(BLANK_TOKEN_PATH.toFile());

        assertNull(token);
    }

    @Test
    void retrieveTokenFromInvalidFile()
    {
        String token = Bot.getTokenFromFile(INVALID_PATH.toFile());

        assertNull(token);
    }

    @Test
    void retrieveTokenFromInput()
    {
        System.setIn(new ByteArrayInputStream("abcd".getBytes()));

        String token = Bot.getTokenFromInput();

        assertNotNull(token);
        assertEquals("abcd", token);
    }

    @Test
    void retrieveTokenFromInvalidInput()
    {
        System.setIn(new ByteArrayInputStream("   ".getBytes()));

        String token = Bot.getTokenFromInput();

        assertNull(token);
    }

    @Test
    void findTokenWithValidFile()
    {
        String token = Bot.findToken(TOKEN_PATH.toFile());

        assertNotNull(token);
        assertEquals("abcd", token);
    }

    @Test
    void findTokenWithInvalidFileFromInput()
    {
        System.setIn(new ByteArrayInputStream("abcd".getBytes()));

        String token = Bot.findToken(INVALID_PATH.toFile());

        assertNotNull(token);
        assertEquals("abcd", token);
    }

    @Test
    void findTokenWithInvalidFileFromInvalidInput()
    {
        System.setIn(new ByteArrayInputStream("    ".getBytes()));

        String token = Bot.findToken(INVALID_PATH.toFile());

        assertNull(token);
    }

    @Test
    void constructorTest()
    {
        Bot bot = new Bot("abcd");

        assertEquals("abcd", bot.getToken());
        assertNull(bot.getBot());
    }

    @Test
    void getSelfUserNullWhenNotConnected()
    {
        Bot bot = new Bot("abcd");

        assertNull(bot.getSelfUser());
    }

    @Test
    void isConnectedFalseWhenDisconnected()
    {
        Bot bot = new Bot("abcd");

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
    void connectingToDiscordFromNewToken()
    {
        String token = Bot.getTokenFromFile(Testing.TOKEN_PATH.toFile());

        assertNotNull(token);

        Bot bot = new Bot("1234");

        assertDoesNotThrow(() -> bot.connect(token));
        assertNotNull(bot.getBot());
        assertNotNull(bot.getBot().getSelfUser());
    }

    @Nested
    class ConnectionTesting
    {

        private static Bot bot;

        @BeforeAll
        static void setUpAll()
        {
            bot = Testing.getBot();

            assertNotNull(bot);
            bot.connect();
        }

        @Test
        void isConnectedTrueWhenConnected()
        {
            assertNotNull(bot.getBot());
            assertTrue(bot.isConnected());
        }

        @Test
        void canGetSelfUserWhenConnected()
        {
            assertNotNull(bot.getSelfUser());
        }

        @Test
        void setStatus()
        {
            bot.setStatus(Activity.ActivityType.LISTENING, "Test");

            Activity activity = bot.getBot().getPresence().getActivity();

            assertNotNull(activity);

            assertEquals("Test", activity.getName());
            assertEquals(Activity.ActivityType.LISTENING, activity.getType());
        }

        @Test
        void setStatusWithDefaultActivityType()
        {
            bot.setStatus("Test");

            Activity activity = bot.getBot().getPresence().getActivity();

            assertNotNull(activity);

            assertEquals("Test", activity.getName());
            assertEquals(Activity.ActivityType.DEFAULT, activity.getType());
        }

        @Test
        void clearStatus()
        {
            bot.setStatus(null);

            assertNull(bot.getBot().getPresence().getActivity());
        }

        @AfterAll
        static void afterAll()
        {
            bot.disconnect();
        }

    }

    @Test
    void disconnectingBot()
    {
        Bot bot = Testing.getBot();

        assertNotNull(bot);
        bot.connect();

        assertTrue(bot.isConnected());
        assertNotNull(bot.getBot());

        bot.disconnect();

        assertNull(bot.getBot());
        assertNull(bot.getSelfUser());
        assertFalse(bot.isConnected());
    }

}