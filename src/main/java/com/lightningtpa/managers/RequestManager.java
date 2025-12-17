package com.lightningtpa.managers;

import com.lightningtpa.LightningTPA;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RequestManager {
    
    private final LightningTPA plugin;
    private final Map<UUID, TPARequest> pendingRequests;
    
    public RequestManager(LightningTPA plugin) {
        this.plugin = plugin;
        this.pendingRequests = new HashMap<>();
    }
    
    /**
     * Create a new TPA request
     */
    public boolean createRequest(Player sender, Player target, RequestType type) {
        UUID senderId = sender.getUniqueId();
        
        // Check if sender already has a pending request
        if (pendingRequests.containsKey(senderId)) {
            return false;
        }
        
        TPARequest request = new TPARequest(sender, target, type);
        pendingRequests.put(senderId, request);
        
        // Schedule request expiry
        scheduleExpiry(senderId);
        
        return true;
    }
    
    /**
     * Get a pending request for a player
     */
    public TPARequest getRequest(Player player) {
        return pendingRequests.get(player.getUniqueId());
    }
    
    /**
     * Get a pending request by sender
     */
    public TPARequest getRequestBySender(Player sender) {
        return pendingRequests.get(sender.getUniqueId());
    }
    
    /**
     * Remove a request
     */
    public void removeRequest(Player player) {
        pendingRequests.remove(player.getUniqueId());
    }
    
    /**
     * Remove a request by sender
     */
    public void removeRequestBySender(Player sender) {
        removeRequest(sender);
    }
    
    /**
     * Get all requests where a player is the target
     */
    public java.util.List<TPARequest> getRequestsForTarget(Player target) {
        java.util.List<TPARequest> targetRequests = new java.util.ArrayList<>();
        for (TPARequest request : pendingRequests.values()) {
            if (request.getTarget().equals(target)) {
                targetRequests.add(request);
            }
        }
        return targetRequests;
    }
    
    /**
     * Check if a player has a pending request
     */
    public boolean hasRequest(Player player) {
        return pendingRequests.containsKey(player.getUniqueId());
    }
    
    /**
     * Check if a player is the target of any request
     */
    public boolean isTargetOfRequest(Player player) {
        for (TPARequest request : pendingRequests.values()) {
            if (request.getTarget().equals(player)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Schedule request expiry after configured time
     */
    private void scheduleExpiry(UUID senderId) {
        int expirySeconds = plugin.getConfigManager().getRequestExpirySeconds();
        
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            TPARequest request = pendingRequests.get(senderId);
            if (request != null) {
                Player sender = request.getSender();
                pendingRequests.remove(senderId);
                
                // Send expiry message if sender is still online
                if (sender != null && sender.isOnline()) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("request-expired"));
                }
            }
        }, expirySeconds * 20L);
    }
    
    /**
     * TPA Request object
     */
    public static class TPARequest {
        private final Player sender;
        private final Player target;
        private final RequestType type;
        private final long timestamp;
        
        public TPARequest(Player sender, Player target, RequestType type) {
            this.sender = sender;
            this.target = target;
            this.type = type;
            this.timestamp = System.currentTimeMillis();
        }
        
        public Player getSender() {
            return sender;
        }
        
        public Player getTarget() {
            return target;
        }
        
        public RequestType getType() {
            return type;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
    
    /**
     * TPA Request types
     */
    public enum RequestType {
        TPA,
        TPAHERE
    }
}