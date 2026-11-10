package org.konnovk.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
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
import org.konnovk.exc.IncorrectArgumentsException;

import java.time.Instant;
import java.util.UUID;

import static org.konnovk.Constants.TPA_KEY_PREFIX;
import static org.konnovk.Utils.runTaskLater;

@RequiredArgsConstructor
@Slf4j
public class TpaCommand implements KCommand {

    private final Boolean enabled;

    @Override
    public String getName() {
        return "tpa";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!enabled) {
            throw new DisabledCommandException();
        }

        if (args.length != 1 || ! (sender instanceof Player source)) {
            throw new IncorrectArgumentsException();
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            log.info("Player {} not found", args[0]);
            throw new IncorrectArgumentsException();
        }

        UUID tpaId = UUID.randomUUID();

        target.getPersistentDataContainer().set(
                buildTpaRequestKey(tpaId, source.getName()),
                PersistentDataType.LONG,
                buildTpaRequestValue()
        );
        runTaskLater(() -> target.getPersistentDataContainer()
                .remove(buildTpaRequestKey(tpaId, source.getName())
                ), 30);

        target.sendMessage(
                notification(source.getName())
        );

        return true;
    }

    public @NotNull Component notification(String source) {
        return Component
                .text("Вам отправлен запрос на телепортацию от игрока '" + source + "'. Введите /tpaccept, чтобы принять запрос.")
                .color(NamedTextColor.GREEN);
    }

    private NamespacedKey buildTpaRequestKey(UUID tpaId, String sourceName) {
        return new NamespacedKey(
                JavaPlugin.getProvidingPlugin(MonoPlugin.class),
                TPA_KEY_PREFIX + "/" + tpaId.toString() + "/" + sourceName
        );
    }

    private Long buildTpaRequestValue() {
        return Instant.now().toEpochMilli();
    }
}
