package org.konnovk.command;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.konnovk.Constants;
import org.konnovk.KCommand;
import org.konnovk.MonoPlugin;
import org.konnovk.exc.DisabledCommandException;
import org.konnovk.exc.HomeNotFoundException;
import org.konnovk.exc.IncorrectArgumentsException;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.konnovk.Constants.HOME_KEY_PREFIX;

@RequiredArgsConstructor
public class HomeCommand implements KCommand {
    private final boolean enabled;
    private final Gson gson;

    @Override
    public String getName() {
        return "home";
    }

    @SuppressWarnings("unchecked")
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

        Optional<String> serializedHomeLocationKey = player.getPersistentDataContainer().getKeys().stream()
                .map(NamespacedKey::getKey)
                .filter(key -> key.startsWith(HOME_KEY_PREFIX))
                .map(key -> key.split("/")[key.split("/").length - 1])
                .filter(key -> Objects.equals(key, homeName))
                .findFirst();
        if (serializedHomeLocationKey.isEmpty()) {
            throw new HomeNotFoundException(homeName);
        }

        String serializedHomeLocation = player.getPersistentDataContainer().get(
                buildHomeKey(homeName),
                PersistentDataType.STRING
        );

        Location homeLocation = Location.deserialize(
                Objects.requireNonNull(gson.fromJson(serializedHomeLocation, Map.class))
        );

        player.teleport(homeLocation);
        player.playSound(successSound());
        player.sendMessage(successMessage());

        return true;
    }

    private Component successMessage() {
        return Component
                .text("Успешно!")
                .color(NamedTextColor.GREEN);
    }

    private Sound successSound() {
        return Sound.sound(
                Key.key(Constants.PLAYER_TELEPORT_SOUND_KEY),
                Sound.Source.PLAYER,
                1.0F,
                1.0F
        );
    }

    private NamespacedKey buildHomeKey(String homeName) {
        return new NamespacedKey(
                JavaPlugin.getProvidingPlugin(MonoPlugin.class),
                HOME_KEY_PREFIX + "/" + homeName
        );
    }
}
