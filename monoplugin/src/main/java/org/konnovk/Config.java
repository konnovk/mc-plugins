package org.konnovk;

import org.bukkit.configuration.file.FileConfiguration;

public record Config(
        Tpa tpa,
        Home home,
        WorldSpawn worldSpawn,
        Auth auth
) {
    public record Auth(
            boolean enabled,
            int bcryptCost,
            int loginTimeoutSeconds
    ) {}
    public record Home(
            boolean enabled,
            int maxPerPlayer
    ) {}

    public record Tpa(
            boolean enabled
    ) {}

    public record WorldSpawn(
            boolean enabled
    ) {}

    public static Config fromFileConfiguration(FileConfiguration fileConfiguration) {
        return new Config(
                new Tpa(
                        fileConfiguration.getBoolean("tpa.enabled")
                ),
                new Home(
                        fileConfiguration.getBoolean("home.enabled"),
                        fileConfiguration.getInt("home.max-per-player")
                ),
                new WorldSpawn(
                        fileConfiguration.getBoolean("worldspawn.enabled")
                ),
                new Auth(
                        fileConfiguration.getBoolean("auth.enabled", true),
                        fileConfiguration.getInt("auth.bcrypt-cost", 10),
                        fileConfiguration.getInt("auth.login-timeout-seconds", 60)
                )
        );
    }
}
