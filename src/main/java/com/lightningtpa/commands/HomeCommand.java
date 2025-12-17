package com.lightningtpa.commands;

import com.lightningtpa.LightningTPA;
import com.lightningtpa.managers.HomeManager;
import com.lightningtpa.managers.TeleportManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class HomeCommand implements CommandExecutor {
    
    private final LightningTPA plugin;
    
    public HomeCommand(LightningTPA plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            player.sendMessage("§cUsage: /home <name> | /home set <name> | /home delete <name>");
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "set":
                return handleSetHome(player, args);
            case "delete":
                return handleDeleteHome(player, args);
            default:
                return handleTeleportHome(player, args[0]);
        }
    }
    
    private boolean handleSetHome(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage("§cUsage: /home set <name>");
            return true;
        }
        
        // Check PvP cooldown
        if (plugin.getCooldownManager().isInCooldown(player)) {
            int remaining = plugin.getCooldownManager().getRemainingCooldown(player);
            String message = plugin.getConfigManager().getMessage("cooldown-blocked", 
                java.util.Collections.singletonMap("time", String.valueOf(remaining)));
            player.sendActionBar(message);
            return true;
        }
        
        String homeName = args[1];
        
        // Validate home name
        if (!plugin.getHomeManager().isValidHomeName(homeName)) {
            player.sendMessage(plugin.getConfigManager().getMessage("home-name-invalid"));
            return true;
        }
        
        // Check if home already exists
        if (plugin.getHomeManager().hasHome(player, homeName)) {
            player.sendMessage(plugin.getConfigManager().getMessage("home-already-exists"));
            return true;
        }
        
        Location location = player.getLocation();
        boolean success = plugin.getHomeManager().setHome(player, homeName, location);
        
        if (success) {
            Map<String, String> placeholders = java.util.Collections.singletonMap("home", homeName);
            player.sendMessage(plugin.getConfigManager().getMessage("home-set", placeholders));
        }
        
        return true;
    }
    
    private boolean handleDeleteHome(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage("§cUsage: /home delete <name>");
            return true;
        }
        
        String homeName = args[1];
        
        boolean success = plugin.getHomeManager().deleteHome(player, homeName);
        
        if (success) {
            Map<String, String> placeholders = java.util.Collections.singletonMap("home", homeName);
            player.sendMessage(plugin.getConfigManager().getMessage("home-deleted", placeholders));
        } else {
            Map<String, String> placeholders = java.util.Collections.singletonMap("home", homeName);
            player.sendMessage(plugin.getConfigManager().getMessage("home-not-found", placeholders));
        }
        
        return true;
    }
    
    private boolean handleTeleportHome(Player player, String homeName) {
        // Check PvP cooldown
        if (plugin.getCooldownManager().isInCooldown(player)) {
            int remaining = plugin.getCooldownManager().getRemainingCooldown(player);
            String message = plugin.getConfigManager().getMessage("cooldown-blocked", 
                java.util.Collections.singletonMap("time", String.valueOf(remaining)));
            player.sendActionBar(message);
            return true;
        }
        
        // Check if player is already teleporting
        if (plugin.getTeleportManager().isTeleporting(player)) {
            player.sendMessage("§cYou are already teleporting!");
            return true;
        }
        
        HomeManager.HomeLocation home = plugin.getHomeManager().getHome(player, homeName);
        
        if (home == null) {
            Map<String, String> placeholders = java.util.Collections.singletonMap("home", homeName);
            player.sendMessage(plugin.getConfigManager().getMessage("home-not-found", placeholders));
            return true;
        }
        
        // Get world
        World world = plugin.getServer().getWorld(home.getWorld());
        if (world == null) {
            player.sendMessage("§cHome world not found!");
            return true;
        }
        
        // Create location
        Location homeLocation = new Location(world, home.getX(), home.getY(), home.getZ(), 
                                           (float) home.getYaw(), (float) home.getPitch());
        
        // Start teleport
        boolean success = plugin.getTeleportManager().startTeleport(player, homeLocation, 
                                                                   TeleportManager.TeleportType.HOME);
        
        if (success) {
            // Teleport started successfully
            return true;
        } else {
            player.sendMessage("§cFailed to start teleport!");
            return true;
        }
    }
}