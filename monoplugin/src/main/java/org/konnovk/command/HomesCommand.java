package org.konnovk.command;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.konnovk.KCommand;
import org.konnovk.exc.DisabledCommandException;
import org.konnovk.exc.IncorrectArgumentsException;

import java.util.Map;
import java.util.Objects;

import static org.konnovk.Constants.HOME_KEY_PREFIX;

@RequiredArgsConstructor
public class HomesCommand implements KCommand {

    private final Boolean enabled;
    private final Gson gson;

    @Override
    public String getName() {
        return "homes";
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!enabled) {
            throw new DisabledCommandException();
        }

        if (args.length != 0 || ! (sender instanceof Player player)) {
            throw new IncorrectArgumentsException();
        }

        StringBuilder sb = new StringBuilder();

        player.getPersistentDataContainer().getKeys().stream()
                .filter(it -> it.getKey().startsWith(HOME_KEY_PREFIX))
                .forEach(key -> {
                    try {
                        String serializedHomeLocation = player.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                        Location homeLocation = Location.deserialize(Objects.requireNonNull(gson.fromJson(serializedHomeLocation, Map.class)));
                        String homeName = key.getKey().split("/")[key.getKey().split("/").length - 1];

                        sb.append("[ %s ] - (%d,%d,%d)%n".formatted(homeName, homeLocation.getBlockX(), homeLocation.getBlockY(), homeLocation.getBlockZ()));
                    } catch (Exception ignored) {}
                });

        player.sendMessage(sb.toString());

        return true;
    }
}
