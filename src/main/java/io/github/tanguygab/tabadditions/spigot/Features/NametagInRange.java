package io.github.tanguygab.tabadditions.spigot.Features;

import io.github.tanguygab.tabadditions.shared.SharedTA;
import me.neznamy.tab.shared.Shared;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NametagInRange {

    public int load() {

        return Bukkit.getScheduler().scheduleAsyncRepeatingTask((Plugin) SharedTA.plugin, () -> {
            int zone = (int) Math.pow(SharedTA.nametagInRange, 2);
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {

                    if (p != player && p.getWorld().equals(player.getWorld()) && player.getLocation().distanceSquared(p.getLocation()) < zone) {
                        Shared.getPlayer(player.getUniqueId()).showNametag(p.getUniqueId());
                    }
                    else if (Shared.getPlayer(player.getUniqueId()) != null) {
                        Shared.getPlayer(player.getUniqueId()).hideNametag(p.getUniqueId());
                    }
                }
            }
        }, 0L, 10L);
    }
}
