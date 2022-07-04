package com.sylink.commands;

import com.sylink.util.account.Account;
import com.sylink.util.config.MessageConfig;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

/**
 * Help command that displays all commands.
 */
public final class CmdHelp
        extends Command
{

    public CmdHelp()
    {
        super(CommandType.universal(), "Displays all commands", null, null, "help", "?");
    }

    @Override
    public String onUserCommand(@NonNull final SlashCommandEvent event, @NonNull final Account account,
                                @NonNull final String label, @NonNull final String[] args)
    {
        if (event.getChannel() instanceof PrivateChannel)
        {
            return super.userOutput(event, "help_message", false);
        }
        else
        {
            event.getUser().openPrivateChannel().complete().sendMessage(MessageConfig.getInstance().getCommand(
                    "help_message")).queue();

            return super.userOutput(event, "check_dms");
        }
    }

    @Override
    public String onConsoleCommand(@NonNull final String label, @NonNull final String[] args)
    {
        return super.consoleOutput("help_message");
    }

}