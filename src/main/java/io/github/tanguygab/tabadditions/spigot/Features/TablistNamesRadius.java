package io.github.tanguygab.tabadditions.spigot.Features;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TablistNamesRadius {

    public int load() {

        return Bukkit.getScheduler().scheduleSyncRepeatingTask((Plugin) TABAdditions.getInstance().getPlugin(), () -> {
            int zone = (int) Math.pow(TABAdditions.getInstance().tablistNamesRadius, 2);
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {

                    if (p != player && p.getWorld().equals(player.getWorld()) && player.getLocation().distanceSquared(p.getLocation()) < zone) {
                        p.showPlayer((Plugin) TABAdditions.getInstance().getPlugin(),player);
                    }
                    else if (p != player) {
                        p.hidePlayer((Plugin) TABAdditions.getInstance().getPlugin(),player);
                    }
                }
            }
        }, 0L, 10L);
    }
}
