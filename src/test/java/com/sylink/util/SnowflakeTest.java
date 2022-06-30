package com.sylink.util;

import com.sylink.Bot;
import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SnowflakeTest
{

    private static final Path PATH_SNOWFLAKE_TEST = Paths.get("src/test/java/com/sylink/util/snowflake_test.toml");
    private static final Path PATH_SNOWFLAKE_TEST_2 = Paths.get("src/test/java/com/sylink/util/snowflake_test_2.toml");

    @Test
    @Order(1)
    void isntLoadedOnStartup()
    {
        Snowflake.TEST.setLoaded(false);

        assertFalse(Snowflake.TEST.isLoaded());
    }

    @Test
    @Order(2)
    void createNewConfigIfNotExist()
    {
        assertTrue(Snowflake.TEST.createConfigIfNotExist(Snowflake.TEST.getResourcePath(),
                Paths.get("temp_snowflake" + ".toml")));

        final File projectFile = new File("temp_snowflake.toml");

        assertTrue(projectFile.exists());
        assertTrue(projectFile.delete());
    }

    @Test
    @Order(3)
    void dontCreateNewConfigIfExists()
    {
        assertFalse(Snowflake.TEST.createConfigIfNotExist(Snowflake.TEST.getResourcePath(), PATH_SNOWFLAKE_TEST));
    }

    @Test
    @Order(4)
    void loadingConfigSetsLoaded()
    {
        assertFalse(Snowflake.TEST.isLoaded());

        Snowflake.TEST.loadFromConfig(Snowflake.TEST.getResourcePath(), PATH_SNOWFLAKE_TEST);

        assertTrue(Snowflake.TEST.isLoaded());
    }

    @Test
    void loadingConfigCreatesNewIfNotExist()
    {
        Snowflake.TEST.loadFromConfig(Snowflake.TEST.getResourcePath(), Paths.get("temp_snowflake.toml"));

        final File projectFile = new File("temp_snowflake.toml");

        assertTrue(projectFile.exists());
        assertTrue(projectFile.delete());
    }

    @Test
    void loadingConfigCorrectValues()
    {
        Snowflake.TEST.loadFromConfig(Snowflake.TEST.getResourcePath(), PATH_SNOWFLAKE_TEST);

        assertEquals(1L, Snowflake.TEST.get("guild.id"));
        assertEquals(2L, Snowflake.TEST.get("roles.member"));
        assertEquals(4L, Snowflake.TEST.get("channels.text.general"));
        assertEquals(6L, Snowflake.TEST.get("channels.voice.lounge"));
    }

    @Test
    void loadingConfigClearsPastSnowflakes()
    {
        Snowflake.TEST.loadFromConfig(Snowflake.TEST.getResourcePath(), PATH_SNOWFLAKE_TEST);

        assertEquals(1L, Snowflake.TEST.get("guild.id"));
        assertEquals(2L, Snowflake.TEST.get("roles.member"));
        assertEquals(4L, Snowflake.TEST.get("channels.text.general"));
        assertEquals(6L, Snowflake.TEST.get("channels.voice.lounge"));

        Snowflake.TEST.loadFromConfig(Snowflake.TEST.getResourcePath(), PATH_SNOWFLAKE_TEST_2);

        assertEquals(10L, Snowflake.TEST.get("guild.id"));
        assertEquals(20L, Snowflake.TEST.get("roles.member"));
        assertEquals(40L, Snowflake.TEST.get("channels.text.general"));
        assertEquals(60L, Snowflake.TEST.get("channels.voice.lounge"));
    }

    @Test
    void loadingAndGettingGuild()
    {
        Snowflake.TEST.loadFromConfig();

        assertTrue(Snowflake.TEST.getGuild("id") != 0L);

        assertTrue(Bot.TEST.connect());
        assertTrue(Bot.TEST.isConnected());

        Snowflake.TEST.loadGuild(Bot.TEST);

        assertNotNull(Snowflake.TEST.getGuild());

        Bot.TEST.disconnect();
    }

    @Test
    void getGuildReturnsValues()
    {
        Snowflake.TEST.loadFromConfig(Snowflake.TEST.getResourcePath(), PATH_SNOWFLAKE_TEST);

        assertEquals(1L, Snowflake.TEST.getGuild("id"));
    }

    @Test
    void getRoleReturnsValues()
    {
        Snowflake.TEST.loadFromConfig(Snowflake.TEST.getResourcePath(), PATH_SNOWFLAKE_TEST);

        assertEquals(2L, Snowflake.TEST.getRole("member"));
        assertEquals(3L, Snowflake.TEST.getRole("admin"));
    }

    @Test
    void getTextChannelReturnsValues()
    {
        Snowflake.TEST.loadFromConfig(Snowflake.TEST.getResourcePath(), PATH_SNOWFLAKE_TEST);

        assertEquals(4L, Snowflake.TEST.getTextChannel("general"));
        assertEquals(5L, Snowflake.TEST.getTextChannel("bot_commands"));
    }

    @Test
    void getVoiceChannelReturnsValues()
    {
        Snowflake.TEST.loadFromConfig(Snowflake.TEST.getResourcePath(), PATH_SNOWFLAKE_TEST);

        assertEquals(6L, Snowflake.TEST.getVoiceChannel("lounge"));
        assertEquals(7L, Snowflake.TEST.getVoiceChannel("music"));
    }

}