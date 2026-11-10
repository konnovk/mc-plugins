package org.konnovk.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.konnovk.Constants;
import org.konnovk.KCommand;
import org.konnovk.exc.DisabledCommandException;
import org.konnovk.exc.IncorrectArgumentsException;
import org.konnovk.exc.TpaRequestsNotFoundException;

import java.util.Optional;

import static org.konnovk.Constants.TPA_KEY_PREFIX;

@RequiredArgsConstructor
@Slf4j
public class TpacceptCommand implements KCommand {
    private final Boolean enabled;

    @Override
    public String getName() {
        return "tpaccept";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!enabled) {
            throw new DisabledCommandException();
        }

        if (args.length > 0 || ! (sender instanceof Player target)) {
            throw new IncorrectArgumentsException();
        }

        Optional<String> sourceName = target.getPersistentDataContainer().getKeys().stream()
                .map(NamespacedKey::getKey)
                .filter(key -> key.startsWith(TPA_KEY_PREFIX))
                .map(key -> key.split("/")[key.split("/").length - 1])
                .findFirst();
        if (sourceName.isEmpty()) {
            log.info("Tpa requests not found in persistence");
            throw new TpaRequestsNotFoundException();
        }
        Player source = Bukkit.getPlayer(sourceName.get());
        if (source == null || !source.isOnline()) {
            log.info("Player from tpa request not found");
            throw new TpaRequestsNotFoundException();
        }

        source.teleport(target);
        source.playSound(successSound());
        target.playSound(successSound());
        target.sendMessage(successMessage());

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
