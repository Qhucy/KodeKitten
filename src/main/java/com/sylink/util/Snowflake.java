package com.sylink.util;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.sylink.KodeKitten;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Guild;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class that holds all Discord Snowflake Id data with easy retrievable functions.
 */
public final class Snowflake
{

    // The path to the resource in the class loader.
    private final static String RESOURCE_PATH = "snowflake.toml";
    // The path for the config file in the program directory.
    private final static String CONFIG_PATH = "snowflake.toml";
    private static Snowflake instance = null;

    public static Snowflake getInstance()
    {
        if (instance == null)
        {
            instance = new Snowflake();
        }

        return instance;
    }

    // Map that hold all snowflake ids associated with their key.
    private final Map<String, Long> snowflakes = new HashMap<>();
    // The main discord guild the bots runs on.
    @Getter(AccessLevel.PUBLIC)
    private Guild mainGuild = null;

    /**
     * Loads all data from the snowflake toml file into the snowflakes map.
     */
    public void loadFromConfig()
    {
        snowflakes.clear();

        createConfigIfNotExist();

        try (final FileConfig fileConfig = FileConfig.of(CONFIG_PATH))
        {
            fileConfig.load();

            loadConfigSection(fileConfig, null);
        }

        // Load the main guild into internal data.
        mainGuild = KodeKitten.getJdaBot().getGuildById(getGuild("id"));
    }

    /**
     * Generates a default snowflake toml config if it doesn't exist in the program's directory.
     */
    private void createConfigIfNotExist()
    {
        if (!(new File(CONFIG_PATH)).exists())
        {
            KodeKitten.saveResource(RESOURCE_PATH, CONFIG_PATH);
        }
    }

    /**
     * Recursively loads all sections of the config file.
     */
    private void loadConfigSection(@NonNull final Config config, String parentKey)
    {
        if (parentKey == null)
        {
            parentKey = "";
        }

        for (final var entry : config.entrySet())
        {
            // Check if the value is an instance of another configuration section.
            if (entry.getValue() instanceof Config)
            {
                loadConfigSection(entry.getValue(), parentKey + entry.getKey() + ".");
            }
            else
            {
                snowflakes.put(parentKey + entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * @return The snowflake id stored in data or 0 if not found.
     */
    public long get(@NonNull final String key)
    {
        return snowflakes.getOrDefault(key, 0L);
    }

    /**
     * @return The snowflake id of a guild piece of data.
     */
    public long getGuild(@NonNull final String key)
    {
        return get("guild." + key);
    }

    /**
     * @return The snowflake id of a role piece of data.
     */
    public long getRole(@NonNull final String key)
    {
        return get("roles." + key);
    }

    /**
     * @return The snowflake id of a text channel piece of data.
     */
    public long getTextChannel(@NonNull final String key)
    {
        return get("channels.text." + key);
    }

    /**
     * @return The snowflake id of a voice channel piece of data.
     */
    public long getVoiceChannel(@NonNull final String key)
    {
        return get("channels.voice." + key);
    }

}