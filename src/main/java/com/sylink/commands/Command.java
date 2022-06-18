package com.sylink.commands;

import com.sylink.KodeKitten;
import com.sylink.account.Account;
import com.sylink.account.AccountManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Abstract parent class for all commands that manages all commands.
 */
public abstract class Command
{

    // List that contains all registered and runnable commands.
    private static final List<Command> commands = new ArrayList<>();

    /**
     * Interprets a slash command to its stored commands and runs the command if the user is able to.
     */
    public static void runCommands(@NonNull final SlashCommandEvent event, @NonNull final String label,
                                   @NonNull final String[] args)
    {
        final Account account = AccountManager.getInstance().getAccount(event.getUser().getIdLong());

        if (account == null)
        {
            event.reply("Unable to load your account data").queue();
            return;
        }

        for (final Command command : commands)
        {
            if (!command.containsLabel(label) || !command.containsCommandType(CommandType.USER) || (command.getPermission() != null && !account.hasPermission(command.getPermission())))
            {
                continue;
            }

            if (event.isFromGuild() && !command.containsCommandType(CommandType.GUILD))
            {
                event.reply("You can only use this command in a DM with me").queue();
                continue;
            }

            if (!event.isFromGuild() && !command.containsCommandType(CommandType.USER))
            {
                event.reply("You can only use this command inside of a discord server").queue();
                continue;
            }

            command.onUserCommand(event, account, label, args);
            return;
        }
    }

    /**
     * Interprets a console command and runs the command.
     */
    public static boolean runCommands(@NonNull final String label, @NonNull final String[] args)
    {
        for (final Command command : commands)
        {
            if (!command.containsLabel(label))
            {
                continue;
            }

            if (!command.containsCommandType(CommandType.CONSOLE))
            {
                System.out.println("This command cannot be executed by console");
                continue;
            }

            command.onConsoleCommand(label, args);
            return true;
        }

        return false;
    }

    /**
     * Registers a given command to Discord and adds it to internal data.
     * Effective in approximately an hour.
     */
    public static void registerCommand(@NonNull final Command command)
    {
        // Only register the command to discord if it has a user implementation.
        if (command.containsCommandType(CommandType.USER))
        {
            KodeKitten.getBot().registerCommand(command.getName(), command.getDescription());
        }

        commands.add(command);
    }

    /**
     * Registers a given command to a Guild and adds it to internal data.
     * Effective almost immediately.
     */
    public static void registerGuildCommand(@NonNull final Command command)
    {
        // Only register the command to discord if it has a user implementation.
        if (command.containsCommandType(CommandType.USER))
        {
            KodeKitten.getBot().registerCommand(command.getName(), command.getDescription());
        }

        commands.add(command);
    }

    // List of command labels used to access the command.
    private final List<String> labels;
    @Getter(AccessLevel.PUBLIC)
    private final String description;
    // Usage information for the command.
    private final String[] usage;
    // The properties of the command.
    private final List<CommandType> commandTypes;
    // The permission required to access the command.
    @Getter(AccessLevel.PUBLIC)
    private final String permission;

    public Command(@NonNull final List<CommandType> commandTypes, @NonNull final String description,
                   @NonNull final String[] usage, final String permission, @NonNull final String... labels)
    {
        this.commandTypes = commandTypes;
        this.description = description;
        this.usage = usage;
        this.permission = permission;
        this.labels = Arrays.asList(labels);
    }

    /**
     * @return The first command label of the command.
     */
    public final String getName()
    {
        return labels.get(0);
    }

    public final boolean containsLabel(@NonNull final String label)
    {
        for (final String commandLabel : labels)
        {
            if (commandLabel.equalsIgnoreCase(label))
            {
                return true;
            }
        }

        return false;
    }

    public final boolean containsCommandType(@NonNull final CommandType commandType)
    {
        return commandTypes.contains(commandType);
    }

    /**
     * Sends the usage syntax message to the slash command event for the command.
     */
    public final void sendUsage(@NonNull final SlashCommandEvent event, @NonNull final String label)
    {
        if (usage == null || usage.length == 0)
        {
            event.reply("There is no defined usage for this command.").queue();
            return;
        }

        if (usage.length == 1)
        {
            event.reply(String.format("Invalid syntax: </%s>", usage[0].replace("{label}", label))).queue();
        }
        else
        {
            final StringBuilder stringBuilder = new StringBuilder();

            for (String usageArgument : usage)
            {
                usageArgument = usageArgument.replace("{label}", label);

                if (!stringBuilder.isEmpty())
                {
                    stringBuilder.append("\n  ");
                }

                stringBuilder.append(usageArgument);
            }

            event.reply(String.format("**Showing help for /%s**:\n  %s", label, stringBuilder)).queue();
        }
    }

    /**
     * Sends the usage syntax message to the console for the command.
     */
    public final void sendUsage(@NonNull final String label)
    {
        if (usage == null || usage.length == 0)
        {
            System.out.println("There is no defined usage for this command.");
            return;
        }

        if (usage.length == 1)
        {
            System.out.printf("Invalid syntax: </%s>\n", usage[0].replace("{label}", label));
        }
        else
        {
            final StringBuilder stringBuilder = new StringBuilder();

            for (String usageArgument : usage)
            {
                usageArgument = usageArgument.replace("{label}", label);

                if (!stringBuilder.isEmpty())
                {
                    stringBuilder.append("\n  ");
                }

                stringBuilder.append(usageArgument);
            }

            System.out.printf("**Showing help for /%s**:\n  %s", label, stringBuilder);
        }
    }

    /**
     * Execution method of the command after passing all initial checks for user commands.
     * If there is no override, the command doesn't exist.
     */
    public void onUserCommand(@NonNull final SlashCommandEvent event, @NonNull final Account account,
                              @NonNull final String label, @NonNull final String[] args)
    {

    }

    /**
     * Execution method of the command after passing all initial checks for console commands.
     */
    public void onConsoleCommand(@NonNull final String label, @NonNull final String[] args)
    {
        // The default implementation if there is no implementation.
        System.out.printf("'/%s' does not have a console implementation.\n", label);
    }

}