package org.konnovk;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;

import java.util.Objects;
import java.util.function.Function;

public interface KCommand extends CommandExecutor {
    String getName();

    default void enable(Function<String, PluginCommand> block) {
        Objects.requireNonNull(block.apply(getName())).setExecutor(this);
    };
}
