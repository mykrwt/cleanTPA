package com.lightningtpa.utils;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class SoundUtils {
    
    public static void playSound(Player player, Sound sound, float volume, float pitch) {
        if (player != null && sound != null) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }
    
    public static void playSound(Player player, Sound sound) {
        playSound(player, sound, 1.0f, 1.0f);
    }
    
    public static Sound getSoundFromString(String soundName) {
        if (soundName == null || soundName.trim().isEmpty()) {
            return null;
        }
        
        try {
            return Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}