package org.konnovk.metrics;

import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class MetricsPlugin extends JavaPlugin {
    private HTTPServer httpServer;
    private BukkitTask updateTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        MetricsConfig config;
        try {
            config = MetricsConfig.from(getConfig());
        } catch (IllegalArgumentException exception) {
            getLogger().severe("Metrics are disabled: " + exception.getMessage());
            return;
        }

        if (!config.enabled()) {
            getLogger().info("Metrics export is disabled in config.yml");
            return;
        }

        PrometheusRegistry registry = new PrometheusRegistry();
        MinecraftMetrics minecraftMetrics = null;
        try {
            if (config.jvmEnabled()) {
                JvmMetrics.builder().register(registry);
            }
            if (config.minecraftEnabled()) {
                minecraftMetrics = new MinecraftMetrics(getServer(), registry, config.perWorldMetrics());
            }

            httpServer = HTTPServer.builder()
                    .hostname(config.host())
                    .port(config.port())
                    .metricsHandlerPath(config.path())
                    .registry(registry)
                    .buildAndStart();
        } catch (Exception exception) {
            closeHttpServer();
            getLogger().severe("Unable to start Prometheus endpoint: " + exception.getMessage());
            return;
        }

        if (minecraftMetrics != null) {
            MinecraftMetrics metricsToUpdate = minecraftMetrics;
            updateTask = getServer().getScheduler().runTaskTimer(
                    this,
                    metricsToUpdate::update,
                    0L,
                    config.updateIntervalTicks()
            );
        }

        getLogger().info("Prometheus metrics are available at http://"
                + config.host() + ":" + config.port() + config.path());
    }

    @Override
    public void onDisable() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        closeHttpServer();
    }

    private void closeHttpServer() {
        if (httpServer != null) {
            httpServer.close();
            httpServer = null;
        }
    }
}
