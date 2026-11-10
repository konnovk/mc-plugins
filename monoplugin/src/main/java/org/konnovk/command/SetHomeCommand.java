package org.konnovk.command;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.konnovk.KCommand;
import org.konnovk.MonoPlugin;
import org.konnovk.exc.DisabledCommandException;
import org.konnovk.exc.HomesLimitExceededException;
import org.konnovk.exc.IncorrectArgumentsException;

import java.util.Objects;

import static org.konnovk.Constants.HOME_KEY_PREFIX;

@RequiredArgsConstructor
public class SetHomeCommand implements KCommand {
    private final boolean enabled;
    private final long limit;
    private final Gson gson;

    @Override
    public String getName() {
        return "sethome";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!enabled) {
            throw new DisabledCommandException();
        }

        if (args.length > 1 || ! (sender instanceof Player player)) {
            throw new IncorrectArgumentsException();
        }

        String homeName;
        if (args.length == 1) {
            homeName = args[0];
        } else {
            homeName = "default";
        }

        long homesCount = player.getPersistentDataContainer().getKeys().stream()
                .map(NamespacedKey::getKey)
                .filter(key -> key.startsWith(HOME_KEY_PREFIX))
                .map(key -> key.split("/")[key.split("/").length - 1])
                .filter(key -> !Objects.equals(key, homeName))
                .count();

        if (homesCount >= limit) {
            throw new HomesLimitExceededException(limit);
        }

        String serializedHomeLocation = gson.toJson(player.getLocation().serialize());

        player.getPersistentDataContainer().set(
                buildHomeKey(homeName),
                PersistentDataType.STRING,
                serializedHomeLocation
        );

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
