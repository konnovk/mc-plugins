package org.konnovk.metrics;

import org.bukkit.configuration.file.FileConfiguration;

record MetricsConfig(
        boolean enabled,
        String host,
        int port,
        String path,
        boolean jvmEnabled,
        boolean minecraftEnabled,
        long updateIntervalTicks,
        boolean perWorldMetrics
) {
    static MetricsConfig from(FileConfiguration config) {
        MetricsConfig result = new MetricsConfig(
                config.getBoolean("metrics.enabled", true),
                config.getString("metrics.host", "127.0.0.1"),
                config.getInt("metrics.port", 9464),
                config.getString("metrics.path", "/metrics"),
                config.getBoolean("metrics.jvm.enabled", true),
                config.getBoolean("metrics.minecraft.enabled", true),
                config.getLong("metrics.minecraft.update-interval-ticks", 20L),
                config.getBoolean("metrics.minecraft.per-world-metrics", true)
        );
        result.validate();
        return result;
    }

    private void validate() {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("metrics.host must not be blank");
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("metrics.port must be between 1 and 65535");
        }
        if (path == null || !path.startsWith("/") || path.equals("/")
                || path.contains("?") || path.contains("#")) {
            throw new IllegalArgumentException("metrics.path must be an absolute HTTP path, for example /metrics");
        }
        if (updateIntervalTicks < 1) {
            throw new IllegalArgumentException("metrics.minecraft.update-interval-ticks must be positive");
        }
    }
}
