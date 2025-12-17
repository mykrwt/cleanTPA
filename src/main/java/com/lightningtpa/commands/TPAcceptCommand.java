package com.lightningtpa.commands;

import com.lightningtpa.LightningTPA;
import com.lightningtpa.managers.RequestManager;
import com.lightningtpa.managers.TeleportManager;
import com.lightningtpa.utils.SoundUtils;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TPAcceptCommand implements CommandExecutor {
    
    private final LightningTPA plugin;
    
    public TPAcceptCommand(LightningTPA plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check PvP cooldown
        if (plugin.getCooldownManager().isInCooldown(player)) {
            int remaining = plugin.getCooldownManager().getRemainingCooldown(player);
            String message = plugin.getConfigManager().getMessage("cooldown-blocked", 
                java.util.Collections.singletonMap("time", String.valueOf(remaining)));
            player.sendActionBar(message);
            return true;
        }
        
        // Check if player has any incoming requests
        List<RequestManager.TPARequest> requests = plugin.getRequestManager().getRequestsForTarget(player);
        if (requests.isEmpty()) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-pending-request"));
            return true;
        }
        
        RequestManager.TPARequest request = requests.get(0); // Get the first request
        
        Player requestSender = request.getSender();
        
        // Check if sender is still online
        if (!requestSender.isOnline()) {
            plugin.getRequestManager().removeRequestBySender(requestSender);
            player.sendMessage(plugin.getConfigManager().getMessage("no-pending-request"));
            return true;
        }
        
        // Start teleport based on request type
        boolean teleportStarted = false;
        switch (request.getType()) {
            case TPA:
                // Teleport sender to target
                teleportStarted = plugin.getTeleportManager().startTeleport(requestSender, player.getLocation(), TeleportManager.TeleportType.TPA);
                break;
            case TPAHERE:
                // Teleport target to sender
                teleportStarted = plugin.getTeleportManager().startTeleport(player, requestSender.getLocation(), TeleportManager.TeleportType.TPAHERE);
                break;
        }
        
        if (teleportStarted) {
            // Remove the request
            plugin.getRequestManager().removeRequestBySender(requestSender);
            
            // Send messages
            requestSender.sendMessage(plugin.getConfigManager().getMessage("request-accepted"));
            player.sendMessage(plugin.getConfigManager().getMessage("request-accepted"));
            
            // Play sounds
            if (plugin.getConfigManager().isSoundsEnabled()) {
                Sound acceptedSound = plugin.getConfigManager().getSound("request-accepted");
                if (acceptedSound != null) {
                    SoundUtils.playSound(player, acceptedSound);
                    SoundUtils.playSound(requestSender, acceptedSound);
                }
            }
        }
        
        return true;
    }
}