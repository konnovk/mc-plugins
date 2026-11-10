package org.konnovk;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.java.JavaPlugin;
import org.konnovk.command.*;

@Slf4j
public class MonoPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        Config config = Config.fromFileConfiguration(getConfig());

        Gson gson = new Gson();

        new WorldSpawnCommand(config.worldSpawn().enabled()).enable(this::getCommand);
        new SetHomeCommand(config.home().enabled(), config.home().maxPerPlayer(), gson).enable(this::getCommand);
        new DelHomeCommand(config.home().enabled()).enable(this::getCommand);
        new HomeCommand(config.home().enabled(), gson).enable(this::getCommand);
        new HomesCommand(config.home().enabled(), gson).enable(this::getCommand);
        new TpaCommand(config.tpa().enabled()).enable(this::getCommand);
        new TpacceptCommand(config.tpa().enabled()).enable(this::getCommand);

        super.onEnable();
    }
}
