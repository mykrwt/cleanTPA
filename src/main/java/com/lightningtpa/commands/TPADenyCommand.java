package com.lightningtpa.commands;

import com.lightningtpa.LightningTPA;
import com.lightningtpa.managers.RequestManager;
import com.lightningtpa.utils.SoundUtils;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TPADenyCommand implements CommandExecutor {
    
    private final LightningTPA plugin;
    
    public TPADenyCommand(LightningTPA plugin) {
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
        
        // Remove the request
        plugin.getRequestManager().removeRequestBySender(requestSender);
        
        // Send messages
        player.sendMessage(plugin.getConfigManager().getMessage("request-denied"));
        
        if (requestSender.isOnline()) {
            requestSender.sendMessage(plugin.getConfigManager().getMessage("request-denied"));
        }
        
        // Play sounds
        if (plugin.getConfigManager().isSoundsEnabled()) {
            Sound deniedSound = plugin.getConfigManager().getSound("request-denied");
            if (deniedSound != null) {
                SoundUtils.playSound(player, deniedSound);
                if (requestSender.isOnline()) {
                    SoundUtils.playSound(requestSender, deniedSound);
                }
            }
        }
        
        return true;
    }
}