package com.sylink.commands;

import com.sylink.account.Account;
import com.sylink.account.AccountManager;
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
        //System.out.println(helpMessage);

        final Account account = AccountManager.getInstance().getAccount(259508942420443136L);

        if (account == null)
        {
            System.out.println("null");
            return;
        }

        if (args.length == 0)
        {
            System.out.println(account.getDiscordId() + " | " + account.getBalance() + " | " + account.getLastActivityTime());
            return;
        }

        if (args[0].equalsIgnoreCase("1"))
        {
            account.addBalance(1.0);

            System.out.println(account.getBalance());
        }
        else if (args[0].equalsIgnoreCase("2"))
        {
            AccountManager.getInstance().flushFromMemory(account, false);
        }
        else if (args[0].equalsIgnoreCase("3"))
        {
            System.out.println(AccountManager.getInstance().existsInMemory(account.getDiscordId()));
        }
        else if (args[0].equalsIgnoreCase("4"))
        {
            System.out.println(AccountManager.getInstance().existsInDatabase(account.getDiscordId()));
        }
    }

}