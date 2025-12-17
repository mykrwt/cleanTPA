package com.lightningtpa.managers;

import com.lightningtpa.LightningTPA;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {
    
    private final LightningTPA plugin;
    private final Map<UUID, Long> cooldownEndTimes;
    
    public CooldownManager(LightningTPA plugin) {
        this.plugin = plugin;
        this.cooldownEndTimes = new HashMap<>();
    }
    
    /**
     * Check if a player is currently in PvP cooldown
     */
    public boolean isInCooldown(Player player) {
        if (!cooldownEndTimes.containsKey(player.getUniqueId())) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        long cooldownEndTime = cooldownEndTimes.get(player.getUniqueId());
        
        if (currentTime >= cooldownEndTime) {
            // Cooldown has expired, remove it
            cooldownEndTimes.remove(player.getUniqueId());
            return false;
        }
        
        return true;
    }
    
    /**
     * Set a player in PvP cooldown
     */
    public void setCooldown(Player player) {
        long cooldownEndTime = System.currentTimeMillis() + (plugin.getConfigManager().getPvpCooldownSeconds() * 1000);
        cooldownEndTimes.put(player.getUniqueId(), cooldownEndTime);
        
        if (plugin.getConfigManager().isCombatLogging()) {
            plugin.getLogger().info(player.getName() + " is now in PvP cooldown for " + 
                plugin.getConfigManager().getPvpCooldownSeconds() + " seconds");
        }
    }
    
    /**
     * Remove cooldown from a player
     */
    public void removeCooldown(Player player) {
        cooldownEndTimes.remove(player.getUniqueId());
    }
    
    /**
     * Get remaining cooldown time in seconds
     */
    public int getRemainingCooldown(Player player) {
        if (!isInCooldown(player)) {
            return 0;
        }
        
        long currentTime = System.currentTimeMillis();
        long cooldownEndTime = cooldownEndTimes.get(player.getUniqueId());
        
        return (int) Math.ceil((cooldownEndTime - currentTime) / 1000.0);
    }
    
    /**
     * Get cooldown time remaining as a formatted string
     */
    public String getCooldownMessage(Player player) {
        int remaining = getRemainingCooldown(player);
        return String.valueOf(remaining);
    }
}