package com.sylink.util.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.sylink.KodeKitten;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Parent class that handles loading from toml config files.
 */
public class ConfigManager
{

    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PROTECTED)
    // Flags whether config data has been loaded yet.
    boolean loaded = false;

    // Map that holds all config keys associated with their value.
    final Map<String, Object> configMap = new ConcurrentHashMap<>();

    /**
     * Generates a default config file if it doesn't exist in the program's directory.
     *
     * @return True if a new config was created.
     */
    public boolean createConfigIfNotExist(@NonNull final String resourcePath, @NonNull final Path projectPath)
    {
        if (!projectPath.toFile().exists())
        {
            KodeKitten.saveResource(resourcePath, projectPath);
            return true;
        }

        return false;
    }

    /**
     * Loads all data from the config file into the config map.
     */
    public void loadFromConfig(@NonNull final String resourcePath, @NonNull final Path projectPath)
    {
        configMap.clear();

        createConfigIfNotExist(resourcePath, projectPath);

        try (final FileConfig fileConfig = FileConfig.of(projectPath))
        {
            fileConfig.load();

            loadConfigSection(fileConfig);

            this.loaded = true;
        }
    }

    /**
     * Recursively loads all sections of the config file.
     */
    private void loadConfigSection(@NonNull final Config config, @Nullable String parentKey)
    {
        if (parentKey == null)
        {
            parentKey = "";
        }

        for (final var entry : config.entrySet())
        {
            if (entry.getValue() instanceof Config)
            {
                loadConfigSection(entry.getValue(), parentKey + entry.getKey() + ".");
            }
            else
            {
                configMap.put(parentKey + entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Recursively loads all sections of the config file.
     */
    private void loadConfigSection(@NonNull final Config config)
    {
        loadConfigSection(config, "");
    }

    /**
     * @return The config value stored at the specified config key.
     */
    public Object get(@NonNull final String key, @Nullable final Object defaultValue)
    {
        return configMap.getOrDefault(key, defaultValue);
    }

    /**
     * @return The config value stored at the specified config key.
     */
    public Object get(@NonNull final String key)
    {
        return get(key, null);
    }

}