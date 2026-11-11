package org.konnovk;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.konnovk.exc.CommandException;

import java.util.Objects;
import java.util.function.Function;

public interface KCommand extends CommandExecutor {
    String getName();

    default void enable(Function<String, PluginCommand> block) {
        Objects.requireNonNull(block.apply(getName())).setExecutor((sender, command, label, args) -> {
            try {
                return onCommand(sender, command, label, args);
            } catch (CommandException exception) {
                sender.sendMessage(Component.text(exception.getMessage()).color(NamedTextColor.RED));
                return true;
            }
        });
    };
}
