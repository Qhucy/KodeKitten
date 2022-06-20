package com.sylink.util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SnowflakeTest
{

    private static final String PATH = "src/test/java/com/sylink/util/snowflake_test.toml";
    private static Snowflake snowflake = null;

    @BeforeAll
    static void setUpAll()
    {
        snowflake = Snowflake.getInstance();
        snowflake.loadFromConfig(PATH, PATH);
    }

    @Test
    void getReturnsValues()
    {
        assertEquals(1L, snowflake.get("guild.id"));
        assertEquals(2L, snowflake.get("roles.member"));
        assertEquals(4L, snowflake.get("channels.text.general"));
        assertEquals(6L, snowflake.get("channels.voice.lounge"));
    }

    @Test
    void getGuildReturnsValues()
    {
        assertEquals(1L, snowflake.getGuild("id"));
    }

    @Test
    void getRoleReturnsValues()
    {
        assertEquals(2L, snowflake.getRole("member"));
        assertEquals(3L, snowflake.getRole("admin"));
    }

    @Test
    void getTextChannelReturnsValues()
    {
        assertEquals(4L, snowflake.getTextChannel("general"));
        assertEquals(5L, snowflake.getTextChannel("bot_commands"));
    }

    @Test
    void getVoiceChannelReturnsValues()
    {
        assertEquals(6L, snowflake.getVoiceChannel("lounge"));
        assertEquals(7L, snowflake.getVoiceChannel("music"));
    }

}