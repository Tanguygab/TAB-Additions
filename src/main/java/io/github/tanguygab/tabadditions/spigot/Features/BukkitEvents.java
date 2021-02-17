package io.github.tanguygab.tabadditions.spigot.Features;

import me.neznamy.tab.shared.TAB;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import io.github.tanguygab.tabadditions.shared.SharedEvents;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.event.BukkitTABLoadEvent;


public class BukkitEvents implements Listener {

    @EventHandler
    public void onTABLoad(BukkitTABLoadEvent e) {
        TABAdditions.getInstance().getPlatform().reload();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (TAB.getInstance().isDisabled()) return;
        SharedEvents.JoinEvent(e.getPlayer().getName());
    }



}
