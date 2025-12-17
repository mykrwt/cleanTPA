package com.lightningtpa.managers;

import com.lightningtpa.LightningTPA;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HomeManager {
    
    private final LightningTPA plugin;
    private final Map<String, Map<String, HomeLocation>> homes;
    private File homesFile;
    private FileConfiguration homesConfig;
    
    public HomeManager(LightningTPA plugin) {
        this.plugin = plugin;
        this.homes = new HashMap<>();
        loadHomes();
    }
    
    /**
     * Load homes from YAML file
     */
    public void loadHomes() {
        if (homesFile == null) {
            homesFile = new File(plugin.getDataFolder(), "homes.yml");
        }
        
        if (!homesFile.exists()) {
            saveHomes(); // Create empty file
            return;
        }
        
        homesConfig = YamlConfiguration.loadConfiguration(homesFile);
        homes.clear();
        
        ConfigurationSection playerSection = homesConfig.getConfigurationSection("homes");
        if (playerSection != null) {
            for (String playerName : playerSection.getKeys(false)) {
                Map<String, HomeLocation> playerHomes = new HashMap<>();
                ConfigurationSection homesSection = playerSection.getConfigurationSection(playerName);
                
                if (homesSection != null) {
                    for (String homeName : homesSection.getKeys(false)) {
                        ConfigurationSection homeSection = homesSection.getConfigurationSection(homeName);
                        if (homeSection != null) {
                            HomeLocation home = new HomeLocation(
                                homeSection.getString("world"),
                                homeSection.getDouble("x"),
                                homeSection.getDouble("y"),
                                homeSection.getDouble("z"),
                                homeSection.getDouble("yaw"),
                                homeSection.getDouble("pitch")
                            );
                            playerHomes.put(homeName, home);
                        }
                    }
                }
                
                homes.put(playerName.toLowerCase(), playerHomes);
            }
        }
    }
    
    /**
     * Save homes to YAML file
     */
    public void saveHomes() {
        if (homesFile == null) {
            homesFile = new File(plugin.getDataFolder(), "homes.yml");
        }
        
        if (homesConfig == null) {
            homesConfig = new YamlConfiguration();
        }
        
        // Clear existing data
        homesConfig.set("homes", null);
        
        // Save all homes
        for (Map.Entry<String, Map<String, HomeLocation>> playerEntry : homes.entrySet()) {
            String playerName = playerEntry.getKey();
            Map<String, HomeLocation> playerHomes = playerEntry.getValue();
            
            for (Map.Entry<String, HomeLocation> homeEntry : playerHomes.entrySet()) {
                String homeName = homeEntry.getKey();
                HomeLocation home = homeEntry.getValue();
                
                String path = "homes." + playerName + "." + homeName;
                homesConfig.set(path + ".world", home.getWorld());
                homesConfig.set(path + ".x", home.getX());
                homesConfig.set(path + ".y", home.getY());
                homesConfig.set(path + ".z", home.getZ());
                homesConfig.set(path + ".yaw", home.getYaw());
                homesConfig.set(path + ".pitch", home.getPitch());
            }
        }
        
        try {
            homesConfig.save(homesFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save homes file: " + e.getMessage());
        }
    }
    
    /**
     * Set a home for a player
     */
    public boolean setHome(Player player, String homeName, Location location) {
        homeName = homeName.toLowerCase();
        String playerName = player.getName().toLowerCase();
        
        if (!isValidHomeName(homeName)) {
            return false;
        }
        
        Map<String, HomeLocation> playerHomes = homes.computeIfAbsent(playerName, k -> new HashMap<>());
        
        if (playerHomes.containsKey(homeName)) {
            return false; // Home already exists
        }
        
        HomeLocation home = new HomeLocation(
            location.getWorld().getName(),
            location.getX(),
            location.getY(),
            location.getZ(),
            location.getYaw(),
            location.getPitch()
        );
        
        playerHomes.put(homeName, home);
        saveHomes(); // Save immediately
        
        return true;
    }
    
    /**
     * Delete a home for a player
     */
    public boolean deleteHome(Player player, String homeName) {
        homeName = homeName.toLowerCase();
        String playerName = player.getName().toLowerCase();
        
        Map<String, HomeLocation> playerHomes = homes.get(playerName);
        if (playerHomes == null || !playerHomes.containsKey(homeName)) {
            return false;
        }
        
        playerHomes.remove(homeName);
        saveHomes(); // Save immediately
        
        return true;
    }
    
    /**
     * Get a home location for a player
     */
    public HomeLocation getHome(Player player, String homeName) {
        homeName = homeName.toLowerCase();
        String playerName = player.getName().toLowerCase();
        
        Map<String, HomeLocation> playerHomes = homes.get(playerName);
        if (playerHomes == null) {
            return null;
        }
        
        return playerHomes.get(homeName);
    }
    
    /**
     * Get all homes for a player
     */
    public Map<String, HomeLocation> getPlayerHomes(Player player) {
        return homes.getOrDefault(player.getName().toLowerCase(), new HashMap<>());
    }
    
    /**
     * Check if a home exists for a player
     */
    public boolean hasHome(Player player, String homeName) {
        return getHome(player, homeName) != null;
    }
    
    /**
     * Check if home name is valid (letters, numbers, underscores only)
     */
    public boolean isValidHomeName(String homeName) {
        return homeName != null && 
               homeName.matches("^[a-zA-Z0-9_]+$") && 
               homeName.length() <= 16 &&
               !homeName.isEmpty();
    }
    
    /**
     * Home location data class
     */
    public static class HomeLocation {
        private final String world;
        private final double x, y, z;
        private final double yaw, pitch;
        
        public HomeLocation(String world, double x, double y, double z, double yaw, double pitch) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
        }
        
        public String getWorld() { return world; }
        public double getX() { return x; }
        public double getY() { return y; }
        public double getZ() { return z; }
        public double getYaw() { return yaw; }
        public double getPitch() { return pitch; }
    }
}