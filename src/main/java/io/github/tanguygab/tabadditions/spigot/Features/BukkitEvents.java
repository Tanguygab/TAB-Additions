package io.github.tanguygab.tabadditions.spigot.Features;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.event.BukkitTABLoadEvent;


public class BukkitEvents implements Listener {

    @EventHandler
    public void onTABLoad(BukkitTABLoadEvent e) {
        TABAdditions.getInstance().getPlatform().reload();
    }

}
