package com.sylink.util.config;

import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MainConfigTest
{

    private static final String PATH = "src/test/java/com/sylink/util/config_test.toml";

    private static final String RESOURCE_PATH = "config.toml";
    private static final Path PROJECT_PATH = Paths.get("src/test/java/com/sylink/util/config_test_2.toml");

    private static MainConfig mainConfig = null;

    @BeforeAll
    static void setUpAll()
    {
        mainConfig = MainConfig.getInstance();
    }

    @Test
    @Order(1)
    void isntLoadedOnStartup()
    {
        assertFalse(mainConfig.isLoaded());
    }

    @Test
    @Order(2)
    void getRandomStatusMessageNullWhenNotLoaded()
    {
        assertNull(mainConfig.getRandomStatusMessage());
    }

    @Test
    @Order(3)
    void createNewConfigIfNotExist()
    {
        assertTrue(mainConfig.createConfigIfNotExist(RESOURCE_PATH, PROJECT_PATH));

        final File projectFile = PROJECT_PATH.toFile();

        assertTrue(projectFile.exists());
        assertTrue(projectFile.length() > 0);

        assertTrue(projectFile.delete());
    }

    @Test
    @Order(4)
    void dontCreateNewConfigIfExist()
    {
        assertFalse(mainConfig.createConfigIfNotExist(PATH, Paths.get(PATH)));
    }

    @Test
    @Order(5)
    void loadingFromConfigSetsLoaded()
    {
        mainConfig.loadFromConfig(PATH, Paths.get(PATH));

        assertTrue(mainConfig.isLoaded());
    }

    @Test
    void loadFromNonExistentConfigCreatesConfig()
    {
        mainConfig.loadFromConfig(RESOURCE_PATH, PROJECT_PATH);

        final File projectFile = PROJECT_PATH.toFile();

        assertTrue(projectFile.exists());
        assertTrue(projectFile.length() > 0);

        assertTrue(projectFile.delete());
    }

    @Test
    void loadingStatusMessages()
    {
        mainConfig.loadFromConfig(PATH, Paths.get(PATH));

        List<String> statusMessages = mainConfig.getStatusMessages();

        assertEquals(5, statusMessages.size());

        assertEquals("1", statusMessages.get(0));
        assertEquals("2", statusMessages.get(1));
        assertEquals("3", statusMessages.get(2));
        assertEquals("4", statusMessages.get(3));
        assertEquals("5", statusMessages.get(4));
    }

    @Test
    void getRandomStatusMessage()
    {
        mainConfig.loadFromConfig(PATH, Paths.get(PATH));

        final String statusMessage = mainConfig.getRandomStatusMessage();

        assertNotNull(statusMessage);
        assertTrue(statusMessage.equals("1") || statusMessage.equals("2") || statusMessage.equals("3") || statusMessage.equals("4") || statusMessage.equals("5"));
    }

}