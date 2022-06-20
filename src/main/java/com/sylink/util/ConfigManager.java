package com.sylink.util;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.sylink.KodeKitten;
import lombok.NonNull;

import java.io.File;
import java.util.List;
import java.util.Random;

/**
 * Singleton class that manages configuration data.
 */
public final class ConfigManager
{

    // The path to the resource in the class loader.
    private final static String RESOURCE_PATH = "config.toml";
    // The path for the config file in the program directory.
    private final static String CONFIG_PATH = "config.toml";

    private static ConfigManager instance = null;

    public static ConfigManager getInstance()
    {
        if (instance == null)
        {
            instance = new ConfigManager();
        }

        return instance;
    }

    private List<String> statusMessages;

    /**
     * Loads all configuration data from the config file.
     */
    public void load(@NonNull final String resourcePath, @NonNull final String configPath)
    {
        createConfigIfNotExist(resourcePath, configPath);

        try (final FileConfig fileConfig = FileConfig.of(configPath))
        {
            fileConfig.load();

            statusMessages = fileConfig.get("status_messages");
        }
    }

    public void load()
    {
        load(RESOURCE_PATH, CONFIG_PATH);
    }

    /**
     * Generates a default toml config if it doesn't exist in the program's directory.
     */
    private void createConfigIfNotExist(@NonNull final String resourcePath, @NonNull final String configPath)
    {
        if (!(new File(configPath)).exists())
        {
            KodeKitten.saveResource(resourcePath, configPath);
        }
    }

    private void createConfigIfNotExist()
    {
        createConfigIfNotExist(RESOURCE_PATH, CONFIG_PATH);
    }

    public List<String> getStatusMessages()
    {
        return statusMessages;
    }

    /**
     * Returns a random status messages.
     */
    public String getRandomStatusMessage()
    {
        if (statusMessages == null)
        {
            return null;
        }
        else
        {
            final Random random = new Random();

            return statusMessages.get(random.nextInt(statusMessages.size()));
        }
    }

}