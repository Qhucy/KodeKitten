package com.sylink.commands;

import com.sylink.Bot;
import com.sylink.KodeKitten;
import com.sylink.util.account.Account;
import com.sylink.util.account.AccountManager;
import com.sylink.util.Snowflake;
import com.sylink.util.config.MessageConfig;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Abstract parent class for all commands that manages all commands.
 */
public abstract class Command
{

    // List that contains all registered and runnable commands.
    private static final Set<Command> commands = new HashSet<>();

    /**
     * Interprets a slash command to its stored commands and runs the command if the user is able to.
     *
     * @return The output response of the command.
     */
    public static String runCommands(@NonNull final SlashCommandEvent event, @NonNull final String label,
                                     @NonNull final String[] args)
    {
        final Account account = AccountManager.getInstance().getAccount(event.getUser().getIdLong());

        if (account == null)
        {
            event.reply(MessageConfig.getInstance().getCommand("cant_load_account")).queue();
            return null;
        }

        for (final Command command : commands)
        {
            if (!command.containsLabel(label) || !command.containsCommandType(CommandType.USER) || (command.getPermission() != null && !account.hasPermission(command.getPermission())))
            {
                continue;
            }

            if (event.isFromGuild() && !command.containsCommandType(CommandType.GUILD))
            {
                return command.userOutput(event, "dm_only_command");
            }

            if (!event.isFromGuild() && !command.containsCommandType(CommandType.USER))
            {
                return command.userOutput(event, "guild_only_command");
            }

            return command.onUserCommand(event, account, label, args);
        }

        return null;
    }

    /**
     * Interprets a console command and runs the command.
     *
     * @return The output response of the command.
     */
    public static String runCommands(@NonNull final String label, @NonNull final String[] args)
    {
        for (final Command command : commands)
        {
            if (!command.containsLabel(label))
            {
                continue;
            }

            if (!command.containsCommandType(CommandType.CONSOLE))
            {
                return command.consoleOutput("no_console_command");
            }

            return command.onConsoleCommand(label, args);
        }

        return null;
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
            command.registerCommand();
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
            command.registerGuildCommand();
        }

        commands.add(command);
    }

    // List of command labels used to access the command.
    private final List<String> labels;
    @Getter(AccessLevel.PUBLIC)
    private final String description;
    // Usage information for the command.
    private final String usage;
    // The properties of the command.
    private final List<CommandType> commandTypes;
    // The permission required to access the command.
    @Getter(AccessLevel.PUBLIC)
    private final String permission;

    public Command(@NonNull final List<CommandType> commandTypes, @NonNull final String description,
                   @Nullable final String usage, @Nullable final String permission, @NonNull final String... labels)
    {
        this.commandTypes = commandTypes;
        this.description = description;
        this.usage = (usage == null) ? "" : usage;
        this.permission = permission;
        this.labels = Arrays.asList(labels);
    }

    public Command(@NonNull final CommandType commandType, @NonNull final String description,
                   @Nullable final String usage, @Nullable final String permission, @NonNull final String... labels)
    {
        this.commandTypes = new ArrayList<>();
        this.commandTypes.add(commandType);

        this.description = description;
        this.usage = (usage == null) ? "" : usage;
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
     * @return The usage string for the command given the command label used.
     */
    public final String getUsage(@NonNull final String label)
    {
        return String.format("/%s %s", label, usage);
    }

    /**
     * Sends the usage syntax message to the slash command event for the command.
     */
    public final void sendUsage(@NonNull final SlashCommandEvent event, @NonNull final String label)
    {
        if (usage == null)
        {
            event.reply(MessageConfig.getInstance().getCommand("no_usage")).queue();
            return;
        }

        event.reply(getUsage(label)).queue();
    }

    /**
     * Sends the usage syntax message to the console for the command.
     */
    public final void sendUsage(@NonNull final String label)
    {
        if (usage == null)
        {
            System.out.println(MessageConfig.getInstance().getCommand("no_usage"));
            return;
        }

        System.out.println(getUsage(label));
    }

    /**
     * Execution method of the command after passing all initial checks for user commands.
     * If there is no override, the command doesn't exist.
     *
     * @return The output response of the command.
     */
    public String onUserCommand(@NonNull final SlashCommandEvent event, @NonNull final Account account,
                                @NonNull final String label, @NonNull final String[] args)
    {
        return userOutput(event, "no_user_command");
    }

    /**
     * Sends an output command message to the user and returns its contents as a string.
     *
     * @param messageKey    The key to the output message in the message config.
     * @param ephemeral     Whether the message is visible to others or not.
     * @param formatObjects The list of format objects if needed.
     *
     * @return The content of the message before formatting.
     */
    public String userOutput(@NonNull final SlashCommandEvent event, @NonNull final String messageKey,
                             final boolean ephemeral, @Nullable final Object... formatObjects)
    {
        final String message = MessageConfig.getInstance().getCommand(messageKey);

        if (formatObjects == null || formatObjects.length == 0)
        {
            event.reply(message).setEphemeral(ephemeral).queue();
        }
        else
        {
            event.reply(String.format(message, formatObjects)).setEphemeral(ephemeral).queue();
        }

        return message;
    }

    /**
     * Sends an output command message to the user and returns its contents as a string.
     * Ephemeral is true by default.
     *
     * @param messageKey    The key to the output message in the message config.
     * @param formatObjects The list of format objects if needed.
     *
     * @return The content of the message before formatting.
     */
    public String userOutput(@NonNull final SlashCommandEvent event, @NonNull final String messageKey,
                             @Nullable final Object... formatObjects)
    {
        return userOutput(event, messageKey, true, formatObjects);
    }

    /**
     * Execution method of the command after passing all initial checks for console commands.
     *
     * @return The output response of the command.
     */
    public String onConsoleCommand(@NonNull final String label, @NonNull final String[] args)
    {
        return consoleOutput("'/%s' does not have a console implementation.");
    }

    /**
     * Sends an output command message to the console and returns its contents as a string.
     *
     * @param messageKey    The key to the output message in the message config.
     * @param formatObjects The list of format objects if needed.
     *
     * @return The content of the message before formatting.
     */
    public String consoleOutput(@NonNull final String messageKey, @Nullable final Object... formatObjects)
    {
        final String message = MessageConfig.getInstance().getCommand(messageKey);

        if (formatObjects == null || formatObjects.length == 0)
        {
            System.out.println(message);
        }
        else
        {
            System.out.printf(message + "%n", formatObjects);
        }

        return message;
    }

    /**
     * Registers a new command with the bot through the main guild's slash commands.
     * Takes effect almost instantly.
     */
    public void registerGuildCommand()
    {
        // Default implementation is a basic command with a name and description.
        final Guild guild = Snowflake.MAIN.getGuild();
        final String name = getName();

        if (guild != null)
        {
            guild.upsertCommand(name, description).queue();
        }
        else
        {
            KodeKitten.logSevere(String.format(MessageConfig.getInstance().getCommand("no_register"), name));
        }
    }

    /**
     * Registers a new command with the bot through Discord's slash commands.
     * Can take upwards of an hour to fully register.
     */
    public void registerCommand()
    {
        final String name = getName();

        Bot.MAIN.getBot().upsertCommand(name, description).queue();
    }

}