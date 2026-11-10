package org.konnovk.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.konnovk.MonoPlugin;
import org.konnovk.Utils;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {
    private final boolean enabled;
    private final int bcryptCost;
    private final int loginTimeoutSeconds;
    private final NamespacedKey passwordHashKey;
    private final Set<UUID> authenticatedPlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, BukkitTask> kickTasks = new ConcurrentHashMap<>();

    public AuthService(boolean enabled, int bcryptCost, int loginTimeoutSeconds) {
        this.enabled = enabled;
        this.bcryptCost = bcryptCost;
        this.loginTimeoutSeconds = loginTimeoutSeconds;
        this.passwordHashKey = new NamespacedKey(
                JavaPlugin.getProvidingPlugin(MonoPlugin.class),
                "auth/password-hash"
        );
    }

    public void onJoin(Player player) {
        authenticatedPlayers.remove(player.getUniqueId());

        if (!enabled) {
            return;
        }

        if (isRegistered(player)) {
            player.sendMessage(Component.text("Введите /login <пароль>, чтобы войти.").color(NamedTextColor.YELLOW));
        } else {
            player.sendMessage(Component.text("Введите /register <пароль>, чтобы зарегистрироваться.").color(NamedTextColor.YELLOW));
        }

        BukkitTask task = Bukkit.getScheduler().runTaskLater(
                JavaPlugin.getProvidingPlugin(MonoPlugin.class),
                () -> kickIfNotAuthenticated(player.getUniqueId()),
                Utils.secondsToTicks(loginTimeoutSeconds)
        );
        kickTasks.put(player.getUniqueId(), task);
    }

    public void onQuit(Player player) {
        authenticatedPlayers.remove(player.getUniqueId());
        cancelKickTask(player.getUniqueId());
    }

    public boolean isAuthenticated(Player player) {
        return !enabled || authenticatedPlayers.contains(player.getUniqueId());
    }

    public boolean isRegistered(Player player) {
        return player.getPersistentDataContainer().has(passwordHashKey, PersistentDataType.STRING);
    }

    public void register(Player player, char[] password) {
        String passwordHash = BCrypt.withDefaults().hashToString(bcryptCost, password);
        player.getPersistentDataContainer().set(passwordHashKey, PersistentDataType.STRING, passwordHash);
        authenticate(player);
    }

    public boolean login(Player player, char[] password) {
        String passwordHash = player.getPersistentDataContainer().get(passwordHashKey, PersistentDataType.STRING);
        if (passwordHash == null || !BCrypt.verifyer().verify(password, passwordHash).verified) {
            return false;
        }

        authenticate(player);
        return true;
    }

    private void authenticate(Player player) {
        authenticatedPlayers.add(player.getUniqueId());
        cancelKickTask(player.getUniqueId());
    }

    private void kickIfNotAuthenticated(UUID playerId) {
        kickTasks.remove(playerId);

        Player player = Bukkit.getPlayer(playerId);
        if (player != null && !isAuthenticated(player)) {
            player.kick(Component.text("Время на авторизацию истекло.").color(NamedTextColor.RED));
        }
    }

    private void cancelKickTask(UUID playerId) {
        BukkitTask task = kickTasks.remove(playerId);
        if (task != null) {
            task.cancel();
        }
    }
}
