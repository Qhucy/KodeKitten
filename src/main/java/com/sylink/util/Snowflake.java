package com.sylink.util;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.sylink.Bot;
import com.sylink.KodeKitten;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Enums for the main and testing bot that holds all Discord Snowflake Id data with easy retrievable functions.
 */
public enum Snowflake
{

    MAIN("snowflake.toml", Paths.get("snowflake.toml")),
    TEST("snowflake.toml", Paths.get("../test_snowflake.toml"));

    // The path to the default snowflake config in code.
    @Getter(AccessLevel.PUBLIC)
    private final String resourcePath;
    // The path to the snowflake config in the project.
    @Getter(AccessLevel.PUBLIC)
    private final Path projectPath;

    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PROTECTED)
    // Flags whether snowflake data has been loaded yet.
    private boolean loaded = false;

    // Map that hold all snowflake ids associated with their key.
    private final Map<String, Long> snowflakes = new HashMap<>();

    // The main guild the bot runs on.
    @Getter(AccessLevel.PUBLIC)
    private Guild guild = null;

    Snowflake(@NonNull final String resourcePath, @NonNull final Path projectPath)
    {
        this.resourcePath = resourcePath;
        this.projectPath = projectPath;
    }

    /**
     * Generates a default snowflake toml config if it doesn't exist in the program's directory.
     *
     * @return True if a new config was created.
     */
    boolean createConfigIfNotExist(@NonNull final String resourcePath, @NonNull final Path projectPath)
    {
        if (!projectPath.toFile().exists())
        {
            KodeKitten.saveResource(resourcePath, projectPath);
            return true;
        }

        return false;
    }

    /**
     * Generates a default snowflake toml config if it doesn't exist in the program's directory.
     *
     * @return True if a new config was created.
     */
    boolean createConfigIfNotExist()
    {
        return createConfigIfNotExist(resourcePath, projectPath);
    }

    /**
     * Loads all data from the snowflake toml file into the snowflakes map.
     */
    public void loadFromConfig(@NonNull final String resourcePath, @NonNull final Path projectPath)
    {
        snowflakes.clear();

        createConfigIfNotExist(resourcePath, projectPath);

        try (final FileConfig fileConfig = FileConfig.of(projectPath))
        {
            fileConfig.load();

            loadConfigSection(fileConfig, null);

            this.loaded = true;
        }
    }

    /**
     * Loads all data from the snowflake toml file into the snowflakes map.
     */
    public void loadFromConfig()
    {
        loadFromConfig(resourcePath, projectPath);
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
            // Check if the value is an instance of another configuration section.
            if (entry.getValue() instanceof Config)
            {
                loadConfigSection(entry.getValue(), parentKey + entry.getKey() + ".");
            }
            else
            {
                snowflakes.put(parentKey + entry.getKey(), entry.getLong());
            }
        }
    }

    /**
     * Loads the main guild object into memory.
     */
    public void loadGuild(@NonNull final Bot bot)
    {
        guild = bot.getBot().getGuildById(getGuild("id"));
    }

    /**
     * @return The snowflake id stored in data or 0 if not found.
     */
    public long get(@NonNull final String key)
    {
        return snowflakes.getOrDefault(key, 0L);
    }

    /**
     * @return The snowflake id of the bot account in the config.
     */
    public long getBotId()
    {
        return get("bot_id");
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