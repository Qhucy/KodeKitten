package com.sylink.util;

import com.sylink.Bot;
import org.junit.jupiter.api.*;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SnowflakeTest
{

    private static final String PATH_SNOWFLAKE_TEST = "src/test/java/com/sylink/util/snowflake_test.toml";
    private static final String PATH_SNOWFLAKE_TEST_2 = "src/test/java/com/sylink/util/snowflake_test_2.toml";
    private static Snowflake snowflake = null;

    @BeforeAll
    static void setUpAll()
    {
        snowflake = Snowflake.TEST;
    }

    @Test
    @Order(1)
    void isntLoadedOnStartup()
    {
        assertFalse(snowflake.isLoaded());
    }

    @Test
    @Order(2)
    void createNewConfigIfNotExist()
    {
        assertTrue(snowflake.createConfigIfNotExist(Snowflake.TEST.getResourcePath(), "temp_snowflake.toml"));

        final File projectFile = new File("temp_snowflake.toml");

        assertTrue(projectFile.exists());
        assertTrue(projectFile.delete());
    }

    @Test
    @Order(3)
    void dontCreateNewConfigIfExists()
    {
        assertFalse(snowflake.createConfigIfNotExist(Snowflake.TEST.getResourcePath(), PATH_SNOWFLAKE_TEST));
    }

    @Test
    @Order(4)
    void loadingConfigSetsLoaded()
    {
        assertFalse(snowflake.isLoaded());

        snowflake.loadFromConfig(Snowflake.TEST.getResourcePath(), PATH_SNOWFLAKE_TEST);

        assertTrue(snowflake.isLoaded());
    }

    @Test
    void loadingConfigCreatesNewIfNotExist()
    {
        snowflake.loadFromConfig(Snowflake.TEST.getResourcePath(), "temp_snowflake.toml");

        final File projectFile = new File("temp_snowflake.toml");

        assertTrue(projectFile.exists());
        assertTrue(projectFile.delete());
    }

    @Test
    void loadingConfigCorrectValues()
    {
        snowflake.loadFromConfig(Snowflake.TEST.getResourcePath(), PATH_SNOWFLAKE_TEST);

        assertEquals(1L, snowflake.get("guild.id"));
        assertEquals(2L, snowflake.get("roles.member"));
        assertEquals(4L, snowflake.get("channels.text.general"));
        assertEquals(6L, snowflake.get("channels.voice.lounge"));
    }

    @Test
    void loadingConfigClearsPastSnowflakes()
    {
        snowflake.loadFromConfig(Snowflake.TEST.getResourcePath(), PATH_SNOWFLAKE_TEST);

        assertEquals(1L, snowflake.get("guild.id"));
        assertEquals(2L, snowflake.get("roles.member"));
        assertEquals(4L, snowflake.get("channels.text.general"));
        assertEquals(6L, snowflake.get("channels.voice.lounge"));

        snowflake.loadFromConfig(Snowflake.TEST.getResourcePath(), PATH_SNOWFLAKE_TEST_2);

        assertEquals(10L, snowflake.get("guild.id"));
        assertEquals(20L, snowflake.get("roles.member"));
        assertEquals(40L, snowflake.get("channels.text.general"));
        assertEquals(60L, snowflake.get("channels.voice.lounge"));
    }

    @Test
    void loadingAndGettingGuild()
    {
        snowflake.loadFromConfig();

        assertTrue(snowflake.getGuild("id") != 0L);

        final Bot bot = Testing.getBot();

        assertNotNull(bot);

        bot.connect();

        assertTrue(bot.isConnected());

        snowflake.loadGuild(bot);

        assertNotNull(snowflake.getGuild());

        bot.disconnect();
    }

    @Test
    void getGuildReturnsValues()
    {
        snowflake.loadFromConfig(Snowflake.TEST.getResourcePath(), PATH_SNOWFLAKE_TEST);

        assertEquals(1L, snowflake.getGuild("id"));
    }

    @Test
    void getRoleReturnsValues()
    {
        snowflake.loadFromConfig(Snowflake.TEST.getResourcePath(), PATH_SNOWFLAKE_TEST);

        assertEquals(2L, snowflake.getRole("member"));
        assertEquals(3L, snowflake.getRole("admin"));
    }

    @Test
    void getTextChannelReturnsValues()
    {
        snowflake.loadFromConfig(Snowflake.TEST.getResourcePath(), PATH_SNOWFLAKE_TEST);

        assertEquals(4L, snowflake.getTextChannel("general"));
        assertEquals(5L, snowflake.getTextChannel("bot_commands"));
    }

    @Test
    void getVoiceChannelReturnsValues()
    {
        snowflake.loadFromConfig(Snowflake.TEST.getResourcePath(), PATH_SNOWFLAKE_TEST);

        assertEquals(6L, snowflake.getVoiceChannel("lounge"));
        assertEquals(7L, snowflake.getVoiceChannel("music"));
    }

}