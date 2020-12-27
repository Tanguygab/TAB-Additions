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

    private final Map<Player, List<Entity>> list = new HashMap<>();

    public int load() {

        return Bukkit.getScheduler().scheduleSyncRepeatingTask((Plugin) SharedTA.plugin, () -> {
            int range = SharedTA.nametagInRange;
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                ArrayList<Entity> entities = (ArrayList<Entity>) p.getNearbyEntities(range, range, range);
                entities.removeIf(entity -> !(entity instanceof Player));
                for (Entity otherp : entities) {
                    Shared.getPlayer(otherp.getUniqueId()).showNametag(p.getUniqueId());
                    list.put(p,entities);
                }
                if (list.containsKey(p))
                    list.get(p).removeIf(entity -> {
                        if (!entities.contains(entity)) {
                            if (Shared.getPlayer(entity.getUniqueId()) != null)
                                Shared.getPlayer(entity.getUniqueId()).hideNametag(p.getUniqueId());
                            return true;
                        }
                        return false;
                    });
            }
        }, 0L, 10L);
    }
}
