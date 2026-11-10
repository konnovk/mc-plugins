package org.konnovk.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import lombok.extern.slf4j.Slf4j;
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

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
public class AuthService {
    private final boolean enabled;
    private final int bcryptCost;
    private final int loginTimeoutSeconds;
    private final JavaPlugin plugin;
    private final NamespacedKey passwordHashKey;
    private final Set<UUID> authenticatedPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> pendingOperations = ConcurrentHashMap.newKeySet();
    private final Map<UUID, BukkitTask> kickTasks = new ConcurrentHashMap<>();

    public AuthService(boolean enabled, int bcryptCost, int loginTimeoutSeconds) {
        this.enabled = enabled;
        this.bcryptCost = bcryptCost;
        this.loginTimeoutSeconds = loginTimeoutSeconds;
        this.plugin = JavaPlugin.getProvidingPlugin(MonoPlugin.class);
        this.passwordHashKey = new NamespacedKey(plugin, "auth/password-hash");
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
                plugin,
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

    public boolean register(Player player, char[] password, Consumer<Boolean> callback) {
        UUID playerId = player.getUniqueId();
        if (!pendingOperations.add(playerId)) {
            Arrays.fill(password, '\0');
            return false;
        }

        Bukkit.getAsyncScheduler().runNow(plugin, task -> {
            String passwordHash = null;
            try {
                passwordHash = BCrypt.withDefaults().hashToString(bcryptCost, password);
            } catch (RuntimeException e) {
                log.error("Не удалось вычислить хеш пароля для игрока {}", playerId, e);
            } finally {
                Arrays.fill(password, '\0');
            }

            String result = passwordHash;
            runOnMainThread(player, () -> {
                if (result == null) {
                    callback.accept(false);
                    return;
                }

                player.getPersistentDataContainer().set(passwordHashKey, PersistentDataType.STRING, result);
                authenticate(player);
                callback.accept(true);
            });
        });
        return true;
    }

    public boolean login(Player player, char[] password, Consumer<Boolean> callback) {
        UUID playerId = player.getUniqueId();
        String passwordHash = player.getPersistentDataContainer().get(passwordHashKey, PersistentDataType.STRING);
        if (passwordHash == null) {
            Arrays.fill(password, '\0');
            callback.accept(false);
            return true;
        }

        if (!pendingOperations.add(playerId)) {
            Arrays.fill(password, '\0');
            return false;
        }

        Bukkit.getAsyncScheduler().runNow(plugin, task -> {
            boolean authenticated;
            try {
                authenticated = BCrypt.verifyer().verify(password, passwordHash).verified;
            } catch (RuntimeException e) {
                authenticated = false;
                log.error("Не удалось проверить пароль игрока {}", playerId, e);
            } finally {
                Arrays.fill(password, '\0');
            }

            boolean result = authenticated;
            runOnMainThread(player, () -> {
                if (result) {
                    authenticate(player);
                }
                callback.accept(result);
            });
        });
        return true;
    }

    private void runOnMainThread(Player player, Runnable action) {
        Bukkit.getGlobalRegionScheduler().run(plugin, task -> {
            try {
                if (player.isOnline() && Bukkit.getPlayer(player.getUniqueId()) == player) {
                    action.run();
                }
            } finally {
                pendingOperations.remove(player.getUniqueId());
            }
        });
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
