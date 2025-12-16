package me.cleantpa.manager;

import org.bukkit.entity.Player;
import java.util.*;

public class CooldownManager {

    private static final Map<UUID, Long> pvp = new HashMap<>();

    public static void tagPvP(Player p) {
        pvp.put(p.getUniqueId(), System.currentTimeMillis() + 20000);
    }

    public static boolean inPvP(Player p) {
        return pvp.getOrDefault(p.getUniqueId(), 0L) > System.currentTimeMillis();
    }

    public static int timeLeft(Player p) {
        return (int)((pvp.get(p.getUniqueId()) - System.currentTimeMillis()) / 1000);
    }
}
