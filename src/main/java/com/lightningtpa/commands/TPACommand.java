package com.lightningtpa.commands;

import com.lightningtpa.LightningTPA;
import com.lightningtpa.managers.CooldownManager;
import com.lightningtpa.managers.RequestManager;
import com.lightningtpa.utils.SoundUtils;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TPACommand implements CommandExecutor {
    
    private final LightningTPA plugin;
    
    public TPACommand(LightningTPA plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length != 1) {
            player.sendMessage("§cUsage: /tpa <player>");
            return true;
        }
        
        // Check PvP cooldown
        if (plugin.getCooldownManager().isInCooldown(player)) {
            int remaining = plugin.getCooldownManager().getRemainingCooldown(player);
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("time", String.valueOf(remaining));
            player.sendMessage(plugin.getConfigManager().getMessage("cooldown-blocked", placeholders));
            return true;
        }
        
        // Find target player
        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
            return true;
        }
        
        if (target.equals(player)) {
            player.sendMessage(plugin.getConfigManager().getMessage("cannot-tpa-yourself"));
            return true;
        }
        
        // Check if target is in PvP cooldown
        if (plugin.getCooldownManager().isInCooldown(target)) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("target", target.getName());
            player.sendMessage(plugin.getConfigManager().getMessage("target-in-combat", placeholders));
            return true;
        }
        
        // Check if sender already has a pending request
        if (plugin.getRequestManager().hasRequest(player)) {
            player.sendMessage(plugin.getConfigManager().getMessage("already-sent-request"));
            return true;
        }
        
        // Create TPA request
        boolean success = plugin.getRequestManager().createRequest(player, target, RequestManager.RequestType.TPA);
        
        if (!success) {
            player.sendMessage(plugin.getConfigManager().getMessage("already-sent-request"));
            return true;
        }
        
        // Send messages and play sounds
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("target", target.getName());
        player.sendMessage(plugin.getConfigManager().getMessage("request-sent", placeholders));
        
        placeholders.clear();
        placeholders.put("sender", player.getName());
        target.sendMessage(plugin.getConfigManager().getMessage("request-received", placeholders));
        
        if (plugin.getConfigManager().isSoundsEnabled()) {
            Sound senderSound = plugin.getConfigManager().getSound("request-sent");
            Sound targetSound = plugin.getConfigManager().getSound("request-received");
            
            if (senderSound != null) {
                SoundUtils.playSound(player, senderSound);
            }
            
            if (targetSound != null) {
                SoundUtils.playSound(target, targetSound);
            }
        }
        
        return true;
    }
}