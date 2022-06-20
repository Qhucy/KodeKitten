package com.sylink.util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfigManagerTest
{

    private static final String PATH = "src/test/java/com/sylink/util/config_test.toml";
    private static ConfigManager configManager = null;

    @BeforeAll
    static void setUpAll()
    {
        configManager = ConfigManager.getInstance();
        configManager.load(PATH, PATH);
    }

    @Test
    void loadingStatusMessages()
    {
        List<String> statusMessages = configManager.getStatusMessages();

        assertEquals(5, statusMessages.size());

        assertEquals("1", statusMessages.get(0));
        assertEquals("2", statusMessages.get(1));
        assertEquals("3", statusMessages.get(2));
        assertEquals("4", statusMessages.get(3));
        assertEquals("5", statusMessages.get(4));
    }

}