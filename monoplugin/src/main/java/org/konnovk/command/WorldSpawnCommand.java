package org.konnovk.command;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.konnovk.Constants;
import org.konnovk.KCommand;
import org.konnovk.exc.DisabledCommandException;
import org.konnovk.exc.IncorrectArgumentsException;

@RequiredArgsConstructor
public class WorldSpawnCommand implements KCommand {

    private final boolean enabled;

    @Override
    public String getName() {
        return "worldspawn";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!enabled) {
            throw new DisabledCommandException();
        }

        if (args.length > 0 || ! (sender instanceof Player player)) {
            throw new IncorrectArgumentsException();
        }

        Location spawnLocation = player.getWorld().getSpawnLocation();

        player.teleport(spawnLocation);
        player.sendMessage(successMessage());
        player.playSound(successSound());

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
}
