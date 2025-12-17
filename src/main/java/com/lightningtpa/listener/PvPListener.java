package com.lightningtpa.listener;

import com.lightningtpa.LightningTPA;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PvPListener implements Listener {
    
    private final LightningTPA plugin;
    
    public PvPListener(LightningTPA plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerDamagePlayer(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity damaged = event.getEntity();
        
        // Check if both entities are players
        if (!(damager instanceof Player) || !(damaged instanceof Player)) {
            return;
        }
        
        Player damagedPlayer = (Player) damaged;
        Player damagerPlayer = (Player) damager;
        
        // Set PvP cooldown for both players
        plugin.getCooldownManager().setCooldown(damagedPlayer);
        plugin.getCooldownManager().setCooldown(damagerPlayer);
        
        if (plugin.getConfigManager().isCombatLogging()) {
            plugin.getLogger().info(damagerPlayer.getName() + " damaged " + damagedPlayer.getName() + 
                " - PvP cooldown activated");
        }
    }
}