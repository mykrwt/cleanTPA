package com.lightningtpa.managers;

import com.lightningtpa.LightningTPA;
import com.lightningtpa.utils.SoundUtils;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    
    private final LightningTPA plugin;
    private File configFile;
    private FileConfiguration config;
    private Map<String, Sound> sounds;
    
    public ConfigManager(LightningTPA plugin) {
        this.plugin = plugin;
        this.sounds = new HashMap<>();
        loadConfig();
    }
    
    public void loadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
        
        // Load sounds configuration
        loadSounds();
        
        // Set default values
        setDefaultValues();
    }
    
    private void loadSounds() {
        sounds.clear();
        
        if (config.contains("sounds.request-sent")) {
            Sound sound = SoundUtils.getSoundFromString(config.getString("sounds.request-sent"));
            if (sound != null) sounds.put("request-sent", sound);
        }
        
        if (config.contains("sounds.request-received")) {
            Sound sound = SoundUtils.getSoundFromString(config.getString("sounds.request-received"));
            if (sound != null) sounds.put("request-received", sound);
        }
        
        if (config.contains("sounds.request-accepted")) {
            Sound sound = SoundUtils.getSoundFromString(config.getString("sounds.request-accepted"));
            if (sound != null) sounds.put("request-accepted", sound);
        }
        
        if (config.contains("sounds.request-denied")) {
            Sound sound = SoundUtils.getSoundFromString(config.getString("sounds.request-denied"));
            if (sound != null) sounds.put("request-denied", sound);
        }
        
        if (config.contains("sounds.teleport-warmup")) {
            Sound sound = SoundUtils.getSoundFromString(config.getString("sounds.teleport-warmup"));
            if (sound != null) sounds.put("teleport-warmup", sound);
        }
        
        if (config.contains("sounds.teleport-complete")) {
            Sound sound = SoundUtils.getSoundFromString(config.getString("sounds.teleport-complete"));
            if (sound != null) sounds.put("teleport-complete", sound);
        }
    }
    
    private void setDefaultValues() {
        if (!config.contains("warmup-seconds")) config.set("warmup-seconds", 5);
        if (!config.contains("pvp-cooldown-seconds")) config.set("pvp-cooldown-seconds", 20);
        if (!config.contains("request-expiry-seconds")) config.set("request-expiry-seconds", 60);
        if (!config.contains("sounds-enabled")) config.set("sounds-enabled", true);
        if (!config.contains("log-combat")) config.set("log-combat", false);
        
        saveConfig();
    }
    
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save config file: " + e.getMessage());
        }
    }
    
    public String getMessage(String path) {
        String message = config.getString("messages." + path, "");
        return message.replace('&', '§');
    }
    
    public String getMessage(String path, Map<String, String> placeholders) {
        String message = getMessage(path);
        
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        
        return message;
    }
    
    public int getWarmupSeconds() {
        return config.getInt("warmup-seconds", 5);
    }
    
    public int getPvpCooldownSeconds() {
        return config.getInt("pvp-cooldown-seconds", 20);
    }
    
    public int getRequestExpirySeconds() {
        return config.getInt("request-expiry-seconds", 60);
    }
    
    public boolean isSoundsEnabled() {
        return config.getBoolean("sounds-enabled", true);
    }
    
    public boolean isCombatLogging() {
        return config.getBoolean("log-combat", false);
    }
    
    public Sound getSound(String key) {
        return sounds.get(key);
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
}