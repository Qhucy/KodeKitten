package com.sylink.util.config;

import com.electronwill.nightconfig.core.file.FileConfig;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

/**
 * Singleton class that manages configuration data.
 */
public class MainConfig
        extends ConfigManager
{

    // The path to the resource in the class loader.
    private final static String RESOURCE_PATH = "config.toml";
    // The path for the config file in the program directory.
    private final static Path PROJECT_PATH = Paths.get("config.toml");

    private static MainConfig instance = null;

    public static MainConfig getInstance()
    {
        if (instance == null)
        {
            instance = new MainConfig();
        }

        return instance;
    }

    // List of status messages in config key status_messages.
    @Getter(AccessLevel.PUBLIC)
    private List<String> statusMessages = null;

    /**
     * Loads all configuration data from the config file.
     */
    @Override
    public void loadFromConfig(@NonNull final String resourcePath, @NonNull final Path projectPath)
    {
        super.configMap.clear();

        createConfigIfNotExist(resourcePath, projectPath);

        try (final FileConfig fileConfig = FileConfig.of(projectPath))
        {
            fileConfig.load();

            statusMessages = fileConfig.get("status_messages");

            super.loaded = true;
        }
    }

    /**
     * Loads all configuration data from the default config file.
     */
    public void loadFromConfig()
    {
        loadFromConfig(RESOURCE_PATH, PROJECT_PATH);
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