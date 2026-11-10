package org.konnovk.command;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.konnovk.KCommand;
import org.konnovk.MonoPlugin;
import org.konnovk.exc.DisabledCommandException;
import org.konnovk.exc.HomeNotFoundException;
import org.konnovk.exc.IncorrectArgumentsException;

import static org.konnovk.Constants.HOME_KEY_PREFIX;

@RequiredArgsConstructor
public class DelHomeCommand implements KCommand {
    private final Boolean enabled;

    @Override
    public String getName() {
        return "delhome";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!enabled) {
            throw new DisabledCommandException();
        }

        if (args.length != 1 || ! (sender instanceof Player player)) {
            throw new IncorrectArgumentsException();
        }

        String homeName = args[0];

        boolean isHomeExists = player.getPersistentDataContainer().getKeys().stream()
                .filter(it -> it.getKey().startsWith(HOME_KEY_PREFIX))
                .anyMatch(it ->
                        it.getKey().split("/")[it.getKey().split("/").length - 1].equals(homeName)
                );

        if (!isHomeExists) {
            throw new HomeNotFoundException(homeName);
        }

        player.getPersistentDataContainer().remove(buildHomeKey(homeName));
        player.sendMessage(successMessage());

        return true;
    }

    private Component successMessage() {
        return Component
                .text("Успешно!")
                .color(NamedTextColor.GREEN);
    }

    private NamespacedKey buildHomeKey(String homeName) {
        return new NamespacedKey(
                JavaPlugin.getProvidingPlugin(MonoPlugin.class),
                HOME_KEY_PREFIX + "/" + homeName
        );
    }
}
