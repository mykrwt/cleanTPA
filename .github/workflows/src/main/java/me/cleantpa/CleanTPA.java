package me.cleantpa;

import me.cleantpa.listener.PvPListener;
import org.bukkit.plugin.java.JavaPlugin;

public class CleanTPA extends JavaPlugin {

    public static final String PREFIX = "§8[§6TPA§8] ";

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new PvPListener(), this);
        getLogger().info("CleanTPA enabled");
    }
}
