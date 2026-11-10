package org.konnovk;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.java.JavaPlugin;
import org.konnovk.listener.AuthListener;
import org.konnovk.service.AuthService;
import org.konnovk.command.*;

@Slf4j
public class MonoPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        Config config = Config.fromFileConfiguration(getConfig());

        Gson gson = new Gson();
        AuthService authService = new AuthService(
                config.auth().enabled(),
                config.auth().bcryptCost(),
                config.auth().loginTimeoutSeconds()
        );

        registerCommands(config, gson, authService);
        registerEvents(authService);

        super.onEnable();
    }

    private void registerEvents(AuthService authService) {
        getServer().getPluginManager().registerEvents(new AuthListener(authService), this);
    }

    private void registerCommands(Config config, Gson gson, AuthService authService) {
        new RegisterCommand(config.auth().enabled(), authService).enable(this::getCommand);
        new LoginCommand(config.auth().enabled(), authService).enable(this::getCommand);
        new WorldSpawnCommand(config.worldSpawn().enabled()).enable(this::getCommand);
        new SetHomeCommand(config.home().enabled(), config.home().maxPerPlayer(), gson).enable(this::getCommand);
        new DelHomeCommand(config.home().enabled()).enable(this::getCommand);
        new HomeCommand(config.home().enabled(), gson).enable(this::getCommand);
        new HomesCommand(config.home().enabled(), gson).enable(this::getCommand);
        new TpaCommand(config.tpa().enabled()).enable(this::getCommand);
        new TpacceptCommand(config.tpa().enabled()).enable(this::getCommand);
    }
}
