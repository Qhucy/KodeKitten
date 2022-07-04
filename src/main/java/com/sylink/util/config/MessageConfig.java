package com.sylink.util.config;

import lombok.NonNull;

import javax.annotation.Nullable;

/**
 * Singleton class that manages all output message data from the config.
 */
public final class MessageConfig
        extends ConfigManager
{

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