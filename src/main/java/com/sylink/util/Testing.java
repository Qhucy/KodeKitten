package com.sylink.util;

import com.sylink.Bot;

/**
 * Utility class used to aid in unit testing for the KodeKitten Testing Bot and Discord Guild.
 */
public final class Testing
{

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
        final String token = Bot.getTokenFromFile(Bot.TEST_TOKEN_PATH.toFile());

        return (token == null) ? null : new Bot(token);
    }

}