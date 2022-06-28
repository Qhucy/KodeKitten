package com.sylink.util;

import org.junit.jupiter.api.*;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConfigManagerTest
{

    private static final String PATH = "src/test/java/com/sylink/util/config_test.toml";

    private static final String RESOURCE_PATH = "config.toml";
    private static final String PROJECT_PATH = "src/test/java/com/sylink/util/config_test_2.toml";

    private static ConfigManager configManager = null;

    @BeforeAll
    static void setUpAll()
    {
        configManager = ConfigManager.getInstance();
    }

    @Test
    @Order(1)
    void isntLoadedOnStartup()
    {
        assertFalse(configManager.isLoaded());
    }

    @Test
    @Order(2)
    void getRandomStatusMessageNullWhenNotLoaded()
    {
        assertNull(configManager.getRandomStatusMessage());
    }

    @Test
    @Order(3)
    void createNewConfigIfNotExist()
    {
        assertTrue(configManager.createConfigIfNotExist(RESOURCE_PATH, PROJECT_PATH));

        final File projectFile = new File(PROJECT_PATH);

        assertTrue(projectFile.exists());
        assertTrue(projectFile.length() > 0);

        assertTrue(projectFile.delete());
    }

    @Test
    @Order(4)
    void dontCreateNewConfigIfExist()
    {
        assertFalse(configManager.createConfigIfNotExist(PATH, PATH));
    }

    @Test
    @Order(5)
    void loadingFromConfigSetsLoaded()
    {
        configManager.load(PATH, PATH);

        assertTrue(configManager.isLoaded());
    }

    @Test
    @Order(6)
    void loadFromNonExistentConfigCreatesConfig()
    {
        configManager.load(RESOURCE_PATH, PROJECT_PATH);

        final File projectFile = new File(PROJECT_PATH);

        assertTrue(projectFile.exists());
        assertTrue(projectFile.length() > 0);

        assertTrue(projectFile.delete());
    }

    @Test
    @Order(7)
    void loadingStatusMessages()
    {
        configManager.load(PATH, PATH);

        List<String> statusMessages = configManager.getStatusMessages();

        assertEquals(5, statusMessages.size());

        assertEquals("1", statusMessages.get(0));
        assertEquals("2", statusMessages.get(1));
        assertEquals("3", statusMessages.get(2));
        assertEquals("4", statusMessages.get(3));
        assertEquals("5", statusMessages.get(4));
    }

    @Test
    @Order(8)
    void getRandomStatusMessage()
    {
        configManager.load(PATH, PATH);

        final String statusMessage = configManager.getRandomStatusMessage();

        assertNotNull(statusMessage);
        assertTrue(statusMessage.equals("1") || statusMessage.equals("2") || statusMessage.equals("3") || statusMessage.equals("4") || statusMessage.equals("5"));
    }

}