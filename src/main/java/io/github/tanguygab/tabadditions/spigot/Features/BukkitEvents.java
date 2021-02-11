package io.github.tanguygab.tabadditions.spigot.Features;

import java.util.HashMap;
import java.util.Map;

import io.github.tanguygab.tabadditions.spigot.TABAdditionsSpigot;
import me.neznamy.tab.shared.TAB;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import io.github.tanguygab.tabadditions.shared.SharedEvents;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.BukkitTABLoadEvent;


public class BukkitEvents implements Listener {

    private final Map<TabPlayer, Boolean> tag = new HashMap<>();

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        if (TABAdditions.getInstance().sneakhideEnabled) {
            boolean sneak = e.isSneaking();
            TabPlayer p = TABAPI.getPlayer(e.getPlayer().getUniqueId());

            if (sneak) {
                tag.put(p, p.hasHiddenNametag());
                p.hideNametag();
            } else if (!tag.get(p))
                p.showNametag();
        }
    }

    @EventHandler
    public void onTABLoad(BukkitTABLoadEvent e) {
        TABAdditionsSpigot.getPlugin(TABAdditionsSpigot.class).reload();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        SharedEvents.JoinEvent(e.getPlayer().getName());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (TABAdditions.getInstance().chatEnabled) {
            SharedEvents.ChatEvent(TAB.getInstance().getPlayer(e.getPlayer().getUniqueId()), e.getMessage());
            e.setCancelled(true);
        }
    }

}
