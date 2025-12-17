package com.lightningtpa.managers;

import com.lightningtpa.LightningTPA;
import com.lightningtpa.utils.SoundUtils;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportManager {
    
    private final LightningTPA plugin;
    private final Map<UUID, TeleportTask> activeTeleports;
    
    public TeleportManager(LightningTPA plugin) {
        this.plugin = plugin;
        this.activeTeleports = new HashMap<>();
    }
    
    /**
     * Start teleport warmup for a player
     */
    public boolean startTeleport(Player player, Location targetLocation, TeleportType type) {
        UUID playerId = player.getUniqueId();
        
        // Cancel existing teleport if any
        cancelTeleport(player);
        
        TeleportTask task = new TeleportTask(player, targetLocation, type);
        activeTeleports.put(playerId, task);
        
        return task.start();
    }
    
    /**
     * Cancel teleport for a player
     */
    public void cancelTeleport(Player player) {
        UUID playerId = player.getUniqueId();
        TeleportTask task = activeTeleports.remove(playerId);
        
        if (task != null) {
            task.cancel();
            player.sendMessage(plugin.getConfigManager().getMessage("teleport-cancelled"));
            
            if (plugin.getConfigManager().isSoundsEnabled()) {
                Sound sound = plugin.getConfigManager().getSound("request-denied");
                if (sound != null) {
                    SoundUtils.playSound(player, sound);
                }
            }
        }
    }
    
    /**
     * Check if a player is currently teleporting
     */
    public boolean isTeleporting(Player player) {
        return activeTeleports.containsKey(player.getUniqueId());
    }
    
    /**
     * Teleport task class
     */
    private class TeleportTask extends BukkitRunnable {
        private final Player player;
        private final Location targetLocation;
        private final TeleportType type;
        private final int warmupSeconds;
        private int currentSecond;
        private final Location startLocation;
        
        public TeleportTask(Player player, Location targetLocation, TeleportType type) {
            this.player = player;
            this.targetLocation = targetLocation;
            this.type = type;
            this.warmupSeconds = plugin.getConfigManager().getWarmupSeconds();
            this.currentSecond = warmupSeconds;
            this.startLocation = player.getLocation().clone();
        }
        
        public boolean start() {
            // Initial message
            showWarmupMessage();
            
            // Schedule the countdown
            runTaskTimer(plugin, 0L, 20L);
            return true;
        }
        
        @Override
        public void run() {
            if (!player.isOnline()) {
                cancel();
                activeTeleports.remove(player.getUniqueId());
                return;
            }
            
            // Check if player moved
            if (hasPlayerMoved()) {
                cancelTeleport(player);
                cancel();
                activeTeleports.remove(player.getUniqueId());
                return;
            }
            
            currentSecond--;
            
            if (currentSecond <= 0) {
                // Teleport complete
                performTeleport();
                cancel();
                activeTeleports.remove(player.getUniqueId());
            } else {
                showWarmupMessage();
            }
        }
        
        private void showWarmupMessage() {
            if (plugin.getConfigManager().isSoundsEnabled()) {
                Sound sound = plugin.getConfigManager().getSound("teleport-warmup");
                if (sound != null && currentSecond > 0) {
                    SoundUtils.playSound(player, sound, 0.5f, 1.0f);
                }
            }
            
            // Action bar message
            java.util.Map<String, String> placeholders = new java.util.HashMap<>();
            placeholders.put("time", String.valueOf(currentSecond));
            String message = plugin.getConfigManager().getMessage("teleport-warmup", placeholders);
            player.sendActionBar(message);
        }
        
        private boolean hasPlayerMoved() {
            Location currentLocation = player.getLocation();
            
            // Check if player moved more than 0.5 blocks
            double distance = startLocation.distance(currentLocation);
            return distance > 0.5;
        }
        
        private void performTeleport() {
            if (!player.teleport(targetLocation)) {
                player.sendMessage(plugin.getConfigManager().getMessage("teleport-cancelled"));
                return;
            }
            
            // Success message
            player.sendMessage(plugin.getConfigManager().getMessage("request-accepted"));
            
            if (plugin.getConfigManager().isSoundsEnabled()) {
                Sound sound = plugin.getConfigManager().getSound("teleport-complete");
                if (sound != null) {
                    SoundUtils.playSound(player, sound, 1.0f, 1.0f);
                }
            }
        }
    }
    
    /**
     * Teleport types
     */
    public enum TeleportType {
        TPA,
        TPAHERE,
        HOME
    }
}