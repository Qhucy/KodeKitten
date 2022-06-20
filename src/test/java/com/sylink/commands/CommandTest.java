package com.sylink.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandTest
{

    static class CmdTest
            extends Command
    {

        CmdTest()
        {
            super(CommandType.universal(), "test", "test", "test", "test");
        }

    }

    static class CmdHelp
            extends Command
    {

        CmdHelp()
        {
            super(CommandType.CONSOLE, "help", null, null, "help", "?");
        }

    }

    @Test
    void constructorWithAllValuesFilled()
    {
        Command test = new CmdTest();

        assertEquals(test.getName(), "test");

        assertTrue(test.containsLabel("test"));
        assertFalse(test.containsLabel("t"));

        assertTrue(test.containsCommandType(CommandType.USER));
        assertTrue(test.containsCommandType(CommandType.CONSOLE));
        assertTrue(test.containsCommandType(CommandType.GUILD));
        assertTrue(test.containsCommandType(CommandType.DM));

        assertTrue(test.getUsage("test").contains("/test test"));
    }

    @Test
    void constructorWithMissingValues()
    {
        Command help = new CmdHelp();

        assertEquals(help.getName(), "help");

        assertTrue(help.containsLabel("help"));
        assertTrue(help.containsLabel("?"));
        assertFalse(help.containsLabel("test"));

        assertTrue(help.containsCommandType(CommandType.CONSOLE));
        assertFalse(help.containsCommandType(CommandType.USER));

        assertTrue(help.getUsage("help").contains("/help"));
    }

    @Test
    void runsCommandIfCorrectLabel()
    {
        Command.registerCommand(new CmdHelp());

        assertTrue(Command.runCommands("help", new String[]{}));
        assertTrue(Command.runCommands("?", new String[]{}));
        assertFalse(Command.runCommands("test", new String[]{}));
    }

}