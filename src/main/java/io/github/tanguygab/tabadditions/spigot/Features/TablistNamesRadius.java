package io.github.tanguygab.tabadditions.spigot.Features;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TablistNamesRadius {

    public int load() {
        final boolean[] first = {true};
        Plugin plugin = (Plugin) TABAdditions.getInstance().getPlugin();
        return Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            int zone = (int) Math.pow(TABAdditions.getInstance().tablistNamesRadius, 2);
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    if (first[0]) p.hidePlayer(plugin,player);
                    if (p != player && p.getWorld().equals(player.getWorld()) && player.getLocation().distanceSquared(p.getLocation()) < zone) {
                        p.showPlayer(plugin,player);
                    }
                    else if (p != player) {
                        p.hidePlayer(plugin,player);
                    }
                }
            }
            first[0] = false;
        }, 0L, 10L);
    }
}
