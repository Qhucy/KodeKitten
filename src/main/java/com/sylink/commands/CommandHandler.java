package com.sylink.commands;

import com.sylink.account.Account;
import com.sylink.account.AccountManager;
import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

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

        System.out.println(1);

        Account account = AccountManager.getAccount(event.getUser().getIdLong());

        if (account == null)
        {
            System.out.println("account is null");
        }
        else
        {
            System.out.printf("%d | %f\n", account.getDiscordId(), account.getBalance());
        }
    }

}