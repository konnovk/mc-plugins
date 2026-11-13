package org.konnovk.metrics;

import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.bukkit.Server;
import org.bukkit.World;

final class MinecraftMetrics {
    private final Server server;
    private final boolean perWorldMetrics;
    private final Gauge onlinePlayers;
    private final Gauge maxPlayers;
    private final Gauge tps;
    private final Gauge averageTickDuration;
    private final Gauge loadedWorlds;
    private final Gauge loadedPlugins;
    private final Gauge loadedEntities;
    private final Gauge loadedChunks;

    MinecraftMetrics(Server server, PrometheusRegistry registry, boolean perWorldMetrics) {
        this.server = server;
        this.perWorldMetrics = perWorldMetrics;

        this.onlinePlayers = Gauge.builder()
                .name("minecraft_players_online")
                .help("Number of players currently online")
                .register(registry);
        this.maxPlayers = Gauge.builder()
                .name("minecraft_players_max")
                .help("Maximum number of players allowed on the server")
                .register(registry);
        this.tps = Gauge.builder()
                .name("minecraft_tps")
                .help("Server ticks per second")
                .labelNames("period")
                .register(registry);
        this.averageTickDuration = Gauge.builder()
                .name("minecraft_tick_duration_seconds")
                .help("Average server tick duration in seconds")
                .register(registry);
        this.loadedWorlds = Gauge.builder()
                .name("minecraft_worlds_loaded")
                .help("Number of loaded worlds")
                .register(registry);
        this.loadedPlugins = Gauge.builder()
                .name("minecraft_plugins_loaded")
                .help("Number of loaded plugins")
                .register(registry);
        this.loadedEntities = worldGauge(
                "minecraft_entities_loaded",
                "Number of loaded entities",
                registry
        );
        this.loadedChunks = worldGauge(
                "minecraft_chunks_loaded",
                "Number of loaded chunks",
                registry
        );

        Gauge serverInfo = Gauge.builder()
                .name("minecraft_server_info")
                .help("Minecraft server version information")
                .labelNames("minecraft_version", "server_version")
                .register(registry);
        serverInfo.labelValues(server.getBukkitVersion(), server.getVersion()).set(1);
    }

    void update() {
        onlinePlayers.set(server.getOnlinePlayers().size());
        maxPlayers.set(server.getMaxPlayers());

        double[] currentTps = server.getTPS();
        tps.labelValues("1m").set(currentTps[0]);
        tps.labelValues("5m").set(currentTps[1]);
        tps.labelValues("15m").set(currentTps[2]);
        averageTickDuration.set(server.getAverageTickTime() / 1000.0);

        loadedWorlds.set(server.getWorlds().size());
        loadedPlugins.set(server.getPluginManager().getPlugins().length);
        updateWorldMetrics();
    }

    private Gauge worldGauge(String name, String help, PrometheusRegistry registry) {
        var builder = Gauge.builder().name(name).help(help);
        if (perWorldMetrics) {
            builder.labelNames("world");
        }
        return builder.register(registry);
    }

    private void updateWorldMetrics() {
        if (perWorldMetrics) {
            loadedEntities.clear();
            loadedChunks.clear();
            for (World world : server.getWorlds()) {
                loadedEntities.labelValues(world.getName()).set(world.getEntities().size());
                loadedChunks.labelValues(world.getName()).set(world.getLoadedChunks().length);
            }
            return;
        }

        int entityCount = 0;
        int chunkCount = 0;
        for (World world : server.getWorlds()) {
            entityCount += world.getEntities().size();
            chunkCount += world.getLoadedChunks().length;
        }
        loadedEntities.set(entityCount);
        loadedChunks.set(chunkCount);
    }
}
