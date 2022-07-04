package com.sylink.util.config;

import lombok.NonNull;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Singleton class that manages all output message data from the config.
 */
public final class MessageConfig
        extends ConfigManager
{

    // The path to the resource in the class loader.
    private final static String RESOURCE_PATH = "messages.toml";
    // The path for the config file in the program directory.
    private final static Path PROJECT_PATH = Paths.get("messages.toml");

    private static MessageConfig instance = null;

    public static MessageConfig getInstance()
    {
        if (instance == null)
        {
            instance = new MessageConfig();
        }

        return instance;
    }

    /**
     * Loads all configuration data from the default config file.
     */
    public void loadFromConfig()
    {
        loadFromConfig(RESOURCE_PATH, PROJECT_PATH);
    }

    /**
     * @return The output message stored at the specified key in the config.
     */
    public String get(@NonNull final String key, @Nullable final String defaultValue)
    {
        return (String) super.configMap.getOrDefault(key, defaultValue);
    }

    /**
     * @return The output message stored at the specified key in the config.
     */
    @Override
    public String get(@NonNull final String key)
    {
        return get(key, "null");
    }

    /**
     * @return The output message at the specified internal message key.
     */
    public String getInternal(@NonNull final String key)
    {
        return get("internal." + key);
    }

    /**
     * @return The output message at the specified command message key.
     */
    public String getCommand(@NonNull final String key)
    {
        return get("command." + key);
    }

    /**
     * @return The output message at the specified event message key.
     */
    public String getEvent(@NonNull final String key)
    {
        return get("event." + key);
    }

    /**
     * @return The output message at the specified other message key.
     */
    public String getOther(@NonNull final String key)
    {
        return get("other." + key);
    }

}