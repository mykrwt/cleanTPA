package com.lightningtpa;

import com.lightningtpa.commands.*;
import com.lightningtpa.listener.PvPListener;
import com.lightningtpa.managers.*;
import org.bukkit.plugin.java.JavaPlugin;

public class LightningTPA extends JavaPlugin {
    
    private static LightningTPA instance;
    
    private CooldownManager cooldownManager;
    private RequestManager requestManager;
    private HomeManager homeManager;
    private TeleportManager teleportManager;
    private ConfigManager configManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Load configuration
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        
        // Initialize managers
        cooldownManager = new CooldownManager(this);
        requestManager = new RequestManager(this);
        homeManager = new HomeManager(this);
        teleportManager = new TeleportManager(this);
        
        // Register listener (single global listener for PvP)
        getServer().getPluginManager().registerEvents(new PvPListener(this), this);
        
        // Register commands
        registerCommands();
        
        getLogger().info("LightningTPA has been enabled successfully!");
    }
    
    @Override
    public void onDisable() {
        // Save homes data
        if (homeManager != null) {
            homeManager.saveHomes();
        }
        
        getLogger().info("LightningTPA has been disabled.");
    }
    
    private void registerCommands() {
        getCommand("tpa").setExecutor(new TPACommand(this));
        getCommand("tpahere").setExecutor(new TPAHereCommand(this));
        getCommand("tpaccept").setExecutor(new TPAcceptCommand(this));
        getCommand("tpdeny").setExecutor(new TPADenyCommand(this));
        getCommand("home").setExecutor(new HomeCommand(this));
    }
    
    public static LightningTPA getInstance() {
        return instance;
    }
    
    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }
    
    public RequestManager getRequestManager() {
        return requestManager;
    }
    
    public HomeManager getHomeManager() {
        return homeManager;
    }
    
    public TeleportManager getTeleportManager() {
        return teleportManager;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
}