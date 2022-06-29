package com.sylink.util;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.sylink.KodeKitten;
import lombok.AccessLevel;
import lombok.Getter;
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
    private final static String PROJECT_PATH = "config.toml";

    private static ConfigManager instance = null;

    public static ConfigManager getInstance()
    {
        if (instance == null)
        {
            instance = new ConfigManager();
        }

        return instance;
    }

    @Getter(AccessLevel.PUBLIC)
    // Flags whether configuration data has been loaded yet.
    private boolean loaded = false;

    // List of status messages in config key status_messages.
    @Getter(AccessLevel.PUBLIC)
    private List<String> statusMessages = null;

    /**
     * Generates a default toml config if it doesn't exist in the program's directory.
     *
     * @return If a new config was created.
     */
    public boolean createConfigIfNotExist(@NonNull final String resourcePath, @NonNull final String projectPath)
    {
        if (!(new File(projectPath)).exists())
        {
            KodeKitten.saveResource(resourcePath, projectPath);
            return true;
        }

        return false;
    }

    /**
     * Generates a default toml config if it doesn't exist in the program's directory using default paths.
     *
     * @return If a new config was created.
     */
    public boolean createConfigIfNotExist()
    {
        return createConfigIfNotExist(RESOURCE_PATH, PROJECT_PATH);
    }

    /**
     * Loads all configuration data from the config file.
     */
    public void load(@NonNull final String resourcePath, @NonNull final String projectPath)
    {
        createConfigIfNotExist(resourcePath, projectPath);

        try (final FileConfig fileConfig = FileConfig.of(projectPath))
        {
            fileConfig.load();

            statusMessages = fileConfig.get("status_messages");

            this.loaded = true;
        }
    }

    /**
     * Loads all configuration data from the default config file.
     */
    public void load()
    {
        load(RESOURCE_PATH, PROJECT_PATH);
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