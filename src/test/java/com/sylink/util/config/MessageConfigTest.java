package com.sylink.util.config;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class MessageConfigTest
{

    private static final String PATH = "src/test/java/com/sylink/util/config/messages_test.toml";
    private static MessageConfig messageConfig = null;

    @BeforeAll
    static void setUpAll()
    {
        messageConfig = MessageConfig.getInstance();

        messageConfig.loadFromConfig(PATH, Paths.get(PATH));
    }

    @Test
    void loadingMessages()
    {
        assertEquals("internal", messageConfig.get("internal.internal"));
        assertEquals("command", messageConfig.get("command.command"));
        assertEquals("event", messageConfig.get("event.event"));
        assertEquals("other", messageConfig.get("other.other"));
    }

    @Test
    void getMessage()
    {
        assertEquals("internal", messageConfig.get("internal.internal"));
    }

    @Test
    void getDefaultMessageNull()
    {
        assertEquals("null", messageConfig.get("invalid.token"));
    }

    @Test
    void getInternalMessage()
    {
        assertEquals("internal", messageConfig.getInternal("internal"));
    }

    @Test
    void getCommandMessage()
    {
        assertEquals("command", messageConfig.getCommand("command"));
    }

    @Test
    void getEventMessage()
    {
        assertEquals("event", messageConfig.getEvent("event"));
    }

    @Test
    void getOtherMessage()
    {
        assertEquals("other", messageConfig.getOther("other"));
    }

}