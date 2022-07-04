package com.sylink.commands;

import com.sylink.Bot;
import com.sylink.KodeKitten;
import com.sylink.util.account.Account;
import com.sylink.util.account.AccountManager;
import com.sylink.util.Snowflake;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

/**
 * Balance command that displays the balance of an account.
 */
public final class CmdBalance
        extends Command
{

    public CmdBalance()
    {
        super(CommandType.universal(), "Displays the balance of an account", "[user]", null, "bal", "money", "dollars");
    }

    @Override
    public String onUserCommand(@NonNull final SlashCommandEvent event, @NonNull final Account account,
                                @NonNull final String label, @NonNull final String[] args)
    {
        if (args.length == 0)
        {
            return super.userOutput(event, "Your balance is $%g", account.getBalance());
        }

        return null;
    }

    @Override
    public String onConsoleCommand(@NonNull final String label, @NonNull final String[] args)
    {
        if (args.length == 0)
        {
            return super.consoleOutput(getUsage(label));
        }

        try
        {
            final long discordId = Long.parseLong(args[0]);
            final Account account = AccountManager.getInstance().getAccount(discordId);

            if (account == null)
            {
                return super.consoleOutput("That account does not exist");
            }
            else
            {
                return super.consoleOutput("%d's balance is $%g", account.getDiscordId(), account.getBalance());
            }
        }
        catch (final NumberFormatException exception)
        {
            return super.consoleOutput("You must input a proper account id.");
        }
    }

    @Override
    public void registerGuildCommand()
    {
        final Guild guild = Snowflake.MAIN.getGuild();
        final String name = super.getName();

        if (guild != null)
        {
            guild.upsertCommand(name, super.getDescription()).addOption(OptionType.USER, "user", "Another user",
                    false).queue();
        }
        else
        {
            KodeKitten.logSevere(String.format("Unable to register command '%s' to the main guild", name));
        }
    }

    @Override
    public void registerCommand()
    {
        final String name = super.getName();

        Bot.MAIN.getBot().upsertCommand(name, super.getDescription()).addOption(OptionType.USER, "user", "Another " +
                "user", false).queue();
    }

}