package com.sylink.commands;

import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Arrays;

/**
 * Class that handles slash command events and runs the respective command.
 */
public final class CommandHandler extends ListenerAdapter
{

    @Override
    public void onSlashCommand(@NonNull final SlashCommandEvent event)
    {
        if (event.getUser().isBot())
            return;

        final String[] splitCommand = event.getCommandString().substring(1).split(" ");

        final String label = splitCommand[0];
        final String[] args = Arrays.copyOfRange(splitCommand, 1, splitCommand.length);

        Command.runCommands(event, label, args);
    }

}