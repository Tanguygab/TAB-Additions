package io.github.tanguygab.tabadditions.spigot.Features;

import io.github.tanguygab.tabadditions.shared.SharedTA;
import me.neznamy.tab.shared.TAB;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class NametagInRange {

    public int load() {

        return Bukkit.getScheduler().scheduleAsyncRepeatingTask((Plugin) SharedTA.plugin, () -> {
            int zone = (int) Math.pow(SharedTA.nametagInRange, 2);
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {

                    if (p != player && p.getWorld().equals(player.getWorld()) && player.getLocation().distanceSquared(p.getLocation()) < zone) {
                        TAB.getInstance().getPlayer(player.getUniqueId()).showNametag(p.getUniqueId());
                    }
                    else if (TAB.getInstance().getPlayer(player.getUniqueId()) != null) {
                        TAB.getInstance().getPlayer(player.getUniqueId()).hideNametag(p.getUniqueId());
                    }
                }
            }
        }, 0L, 10L);
    }
}
