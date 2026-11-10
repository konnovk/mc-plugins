package org.konnovk;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Utils {
    public static void runTaskLater(Runnable runnable, int delaySeconds) {
        Bukkit.getScheduler().runTaskLater(
                JavaPlugin.getProvidingPlugin(MonoPlugin.class),
                runnable,
                secondsToTicks(delaySeconds)
        );
    }

    public static long secondsToTicks(int seconds) {
        return 20L * seconds;
    }
}
