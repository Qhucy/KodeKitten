package com.sylink.commands;

import com.sylink.account.Account;
import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

/**
 * Help command that displays all commands.
 */
public final class CmdHelp
        extends Command
{

    public CmdHelp()
    {
        super(CommandType.universal(), "Displays all commands", new String[] {"{label}"}, null, "help", "?");
    }

    @Override
    public void onCommand(@NonNull final SlashCommandEvent event, @NonNull final Account account,
                          @NonNull final String label, @NonNull final String[] args)
    {
        event.reply("List of commands:").setEphemeral(true).queue();
    }

    @Override
    public void onCommand(@NonNull final String label, @NonNull final String[] args)
    {
        System.out.println("List of commands:");
    }

}