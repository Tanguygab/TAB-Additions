package io.github.tanguygab.tabadditions.bungee;

import io.github.tanguygab.tabadditions.shared.SharedEvents;
import me.neznamy.tab.api.event.BungeeTABLoadEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeEvents implements Listener {

    @EventHandler
    public void onJoin(ServerSwitchEvent e) {
        SharedEvents.JoinEvent(e.getPlayer().getName());
    }

    @EventHandler
    public void onTABLoad(BungeeTABLoadEvent e) {
        TABAdditionsBungeeCord.getInstance().reload();
    }
}
