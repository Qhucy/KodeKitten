package com.sylink.util;

import com.sylink.Bot;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class used to aid in unit testing for the KodeKitten Testing Bot and Discord Guild.
 */
public final class Testing
{

    /**
     * =============
     * | IMPORTANT |
     * =============
     * The path to the text file that contains the Discord Bot Token for the testing bot.
     * This is used for unit testing and must be correct to run tests.
     */
    public static final Path TOKEN_PATH = Paths.get("../test_token.txt");
    /**
     * The path to the test snowflake config that contains the Discord Snowflake Ids
     * for the unit test bot to use in the testing discord server.
     */
    public static final String SNOWFLAKE_PATH = "../test_snowflake.toml";

    // The Discord Snowflake Id of the test bot.
    public final static long BOT_ID = 890542435921440778L;

    // The Discord Snowflake Id of the testing guild.
    public final static long GUILD_ID = 989935294738485288L;

    // The Discord Snowflake Id of the bot role in the testing guild.
    public final static long ROLE_BOT_ID = 989935294738485291L;
    // The Discord Snowflake Id of the kode kitten role in the testing guild.
    public final static long ROLE_KK_ID = 989936363648458754L;

    /**
     * @return The unit testing bot that is separate from the main discord bot.
     */
    public static Bot getBot()
    {
        final String token = Bot.getTokenFromFile(TOKEN_PATH.toFile());

        return (token == null) ? null : new Bot(token);
    }

}