package com.sylink.commands;

import com.sylink.account.Account;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

/**
 * Help command that displays all commands.
 */
public final class CmdHelp
        extends Command
{

    private final static String helpMessage = """
            **Available Commands:**
            **/balance [user]**: View the balance of an account.
            """;

    public CmdHelp()
    {
        super(CommandType.universal(), "Displays all commands", null, null, "help", "?");
    }

    @Override
    public void onUserCommand(@NonNull final SlashCommandEvent event, @NonNull final Account account,
                              @NonNull final String label, @NonNull final String[] args)
    {
        if (event.getChannel() instanceof PrivateChannel)
        {
            event.reply(helpMessage).queue();
        }
        else
        {
            event.reply("Check your DMs for command help!").setEphemeral(true).queue();
            event.getUser().openPrivateChannel().complete().sendMessage(helpMessage).queue();
        }
    }

    @Override
    public void onConsoleCommand(@NonNull final String label, @NonNull final String[] args)
    {
        System.out.println(helpMessage);
    }

}