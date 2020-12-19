package io.github.tanguygab.tabadditions.spigot;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import io.github.tanguygab.tabadditions.shared.SharedEvents;
import io.github.tanguygab.tabadditions.shared.SharedTA;
import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.BukkitTABLoadEvent;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;


public class BukkitEvents implements Listener {

    private final Map<TabPlayer, Boolean> tag = new HashMap<>();

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        if (SharedTA.sneakhideEnabled) {
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
    public void onJoin(PlayerJoinEvent e) {
        SharedEvents.JoinEvent(e.getPlayer().getName());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (SharedTA.chatEnabled) {
            TabPlayer p = TABAPI.getPlayer(e.getPlayer().getUniqueId());
            String msg = e.getMessage();
            Map<String,Object> formats = SharedTA.chatConfig.getConfigurationSection("chat-formats");
            Map<String,Object> fSection = (Map<String, Object>) formats.get("_OTHER_");
            String format = fSection.get("text").toString();

            format = Shared.platform.replaceAllPlaceholders(format,p).replaceAll("%msg%", msg);
            for (TabPlayer pl : Shared.getPlayers())
                pl.sendCustomPacket(new PacketPlayOutChat(format, PacketPlayOutChat.ChatMessageType.CHAT));

            e.setCancelled(true);
        }

    }
    @EventHandler
    public void onTABLoad(BukkitTABLoadEvent e) {
        TABAdditionsSpigot.getPlugin(TABAdditionsSpigot.class).reload();
    }
}
