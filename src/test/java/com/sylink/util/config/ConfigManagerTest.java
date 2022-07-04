package com.sylink.util.config;

import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConfigManagerTest
{

    private static final String PATH = "src/test/java/com/sylink/util/config/default_config.toml";

    private static final String RESOURCE_PATH = "config.toml";
    private static final Path PROJECT_PATH = Paths.get("src/test/java/com/sylink/util/config/default_config_2.toml");

    private static ConfigManager configManager = null;

    @BeforeAll
    static void setUpAll()
    {
        configManager = new ConfigManager();
    }

    @Test
    @Order(1)
    void isntLoadedOnStartup()
    {
        assertFalse(configManager.isLoaded());
    }

    @Test
    @Order(2)
    void loadingFromConfigSetsLoaded()
    {
        configManager.loadFromConfig(PATH, Paths.get(PATH));

        assertTrue(configManager.isLoaded());
    }

    @Test
    void createNewConfigIfNotExist()
    {
        assertTrue(configManager.createConfigIfNotExist(RESOURCE_PATH, PROJECT_PATH));

        final File projectFile = PROJECT_PATH.toFile();

        assertTrue(projectFile.exists());
        assertTrue(projectFile.length() > 0);

        assertTrue(projectFile.delete());
    }

    @Test
    void dontCreateNewConfigIfExist()
    {
        assertFalse(configManager.createConfigIfNotExist(PATH, Paths.get(PATH)));
    }

    @Test
    void loadFromNonExistentConfigCreatesConfig()
    {
        configManager.loadFromConfig(RESOURCE_PATH, PROJECT_PATH);

        final File projectFile = PROJECT_PATH.toFile();

        assertTrue(projectFile.exists());
        assertTrue(projectFile.length() > 0);

        assertTrue(projectFile.delete());
    }

    @Test
    void loadingConfig()
    {
        configManager.loadFromConfig(PATH, Paths.get(PATH));

        assertEquals(1, configManager.get("first"));
        assertEquals(2, configManager.get("second.third"));
        assertEquals(3, configManager.get("second.fourth.fifth"));
    }

    @Test
    void gettingValue()
    {
        configManager.loadFromConfig(PATH, Paths.get(PATH));

        final Object value = configManager.get("second.third");

        assertNotNull(value);
        assertEquals(2, value);
    }

    @Test
    void gettingDefaultValue()
    {
        configManager.loadFromConfig(PATH, Paths.get(PATH));

        final Object value = configManager.get("does.not.exist", 100);

        assertNotNull(value);
        assertEquals(100, value);
    }

}