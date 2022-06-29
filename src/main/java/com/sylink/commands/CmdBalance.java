package com.sylink.commands;

import com.sylink.KodeKitten;
import com.sylink.account.Account;
import com.sylink.account.AccountManager;
import com.sylink.util.Snowflake;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

/**
 * Balance command that displays the balance of an account.
 */
public final class CmdBalance
        extends Command
{

    public CmdBalance()
    {
        super(CommandType.universal(), "Displays the balance of an account", "[user]", null, "bal", "money",
                "dollars");
    }

    @Override
    public void onUserCommand(@NonNull final SlashCommandEvent event, @NonNull final Account account,
                              @NonNull final String label, @NonNull final String[] args)
    {
        if (args.length == 0)
        {
            event.reply("Your balance is $" + account.getBalance()).setEphemeral(true).queue();
        }
        else
        {

        }
    }

    @Override
    public void onConsoleCommand(@NonNull final String label, @NonNull final String[] args)
    {
        if (args.length == 0)
        {
            super.sendUsage(label);
        }
        else
        {
            try
            {
                final long discordId = Long.parseLong(args[0]);
                final Account account = AccountManager.getInstance().getAccount(discordId);

                if (account == null)
                {
                    System.out.println("That account does not exist");
                }
                else
                {
                    System.out.printf("%d's balance is $%g\n", account.getDiscordId(), account.getBalance());
                }
            }
            catch (final NumberFormatException exception)
            {
                System.out.println("You must input a proper account id.");
            }
        }
    }

    @Override
    public void registerGuildCommand()
    {
        final Guild guild = Snowflake.MAIN.getGuild();
        final String name = super.getName();

        if (guild != null)
        {
            guild.upsertCommand(name, super.getDescription()).queue();
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

        KodeKitten.getJdaBot().upsertCommand(name, super.getDescription()).queue();
    }

}