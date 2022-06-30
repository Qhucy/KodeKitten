package com.sylink;

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
    void getSelfUserNullWhenNotConnected()
    {
        assertNull(Bot.TEST.getSelfUser());
    }

    @Test
    void isConnectedFalseWhenDisconnected()
    {
        assertFalse(Bot.TEST.isConnected());
    }

    @Test
    void settingTheToken()
    {
        Bot.TEST.setToken("1234");

        assertEquals("1234", Bot.TEST.getToken());
    }

    @Test
    void connectWithNewTokenChangesExistingToken()
    {
        try
        {
            Bot.TEST.connect("1234");
        }
        catch (Throwable ignored)
        {

        }

        assertEquals("1234", Bot.TEST.getToken());
    }

    @Test
    void connectingToDiscordFromNewToken()
    {
        Bot.TEST.setToken("abcde");

        assertDoesNotThrow(() -> Bot.TEST.connect(Bot.TEST.getTokenFromFile()));
        assertNotNull(Bot.TEST.getBot());
        assertNotNull(Bot.TEST.getSelfUser());
    }

    @Nested
    class ConnectionTesting
    {

        @BeforeAll
        static void setUpAll()
        {
            Bot.TEST.setTokenFromFile();

            assertTrue(Bot.TEST.connect());
        }

        @Test
        void isConnectedTrueWhenConnected()
        {
            assertNotNull(Bot.TEST.getBot());
            assertTrue(Bot.TEST.isConnected());
        }

        @Test
        void canGetSelfUserWhenConnected()
        {
            assertNotNull(Bot.TEST.getSelfUser());
        }

        @Test
        void setStatus()
        {
            Bot.TEST.setStatus(Activity.ActivityType.LISTENING, "Test");

            Activity activity = Bot.TEST.getBot().getPresence().getActivity();

            assertNotNull(activity);

            assertEquals("Test", activity.getName());
            assertEquals(Activity.ActivityType.LISTENING, activity.getType());
        }

        @Test
        void setStatusWithDefaultActivityType()
        {
            Bot.TEST.setStatus("Test");

            Activity activity = Bot.TEST.getBot().getPresence().getActivity();

            assertNotNull(activity);

            assertEquals("Test", activity.getName());
            assertEquals(Activity.ActivityType.DEFAULT, activity.getType());
        }

        @Test
        void clearStatus()
        {
            Bot.TEST.setStatus(null);

            assertNull(Bot.TEST.getBot().getPresence().getActivity());
        }

        @AfterAll
        static void afterAll()
        {
            Bot.TEST.disconnect();
        }

    }

    @Test
    void disconnectingBot()
    {
        Bot.TEST.setTokenFromFile();

        assertTrue(Bot.TEST.connect());
        assertTrue(Bot.TEST.isConnected());
        assertNotNull(Bot.TEST.getBot());

        Bot.TEST.disconnect();

        assertNull(Bot.TEST.getBot());
        assertNull(Bot.TEST.getSelfUser());
        assertFalse(Bot.TEST.isConnected());
    }

}