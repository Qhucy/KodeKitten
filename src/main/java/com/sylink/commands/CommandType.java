package com.sylink.commands;

import java.util.Arrays;
import java.util.List;

/**
 * Enum constants for the types of commands that exist.
 */
public enum CommandType
{

    /**
     * Command can be accessed in guild channels.
     */
    GUILD,
    /**
     * Command can be accessed in DM channels.
     */
    DM,

    /**
     * Command can be accessed by users.
     */
    USER,
    /**
     * Command can be accessed by console.
     */
    CONSOLE;

    /**
     * @return Every CommandType in a list.
     */
    public static List<CommandType> universal()
    {
        return Arrays.asList(values());
    }

    /**
     * @return Every CommandType for user functionality only in a list.
     */
    public static List<CommandType> userUniversal()
    {
        return Arrays.asList(USER, GUILD, DM);
    }

    /**
     * @return The USER and GUILD command type
     */
    public static List<CommandType> guild()
    {
        return Arrays.asList(GUILD, USER);
    }

}