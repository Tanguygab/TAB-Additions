package io.github.tanguygab.tabadditions.spigot;

import me.neznamy.tab.api.TABAPI;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;
import me.neznamy.tab.shared.packets.PacketPlayOutTitle;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import me.neznamy.tab.api.event.BukkitTABLoadEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class BukkitEvents implements Listener {

    private final TABAdditionsSpigot plugin;
    private final FileConfiguration config;
    private final FileConfiguration titleConfig;
    private final FileConfiguration actionbarConfig;
    private final FileConfiguration chatConfig;
    private Map<TabPlayer, Boolean> tag = new HashMap<>();


    public BukkitEvents(TABAdditionsSpigot plugin, FileConfiguration config, FileConfiguration titleConfig, FileConfiguration actionbarConfig, FileConfiguration chatConfig) {
        this.config = config;
        this.titleConfig = titleConfig;
        this.actionbarConfig = actionbarConfig;
        this.chatConfig = chatConfig;
        this.plugin = plugin;
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {

        boolean sneak = e.isSneaking();
        TabPlayer p = TABAPI.getPlayer(e.getPlayer().getUniqueId());
        if (sneak) {
            tag.put(p,p.hasHiddenNametag());
            if (config.getBoolean("features.sneak-hide-nametags"))
                p.hideNametag();
        }
        else
            if (!tag.get(p))
                p.showNametag();

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        TabPlayer pTAB = TABAPI.getPlayer(p.getUniqueId());
        plugin.loadProps(pTAB);
        if (config.getBoolean("features.actionbars")) {
            String actionbar = actionbarConfig.getString("bars." + pTAB.getProperty("actionbar").get(), "");
            actionbar = Shared.platform.replaceAllPlaceholders(actionbar, pTAB);
            pTAB.sendCustomPacket(new PacketPlayOutChat(actionbar, PacketPlayOutChat.ChatMessageType.GAME_INFO));
        }
        if (config.getBoolean("features.titles")) {
            ConfigurationSection tSection = titleConfig.getConfigurationSection("titles." + pTAB.getProperty("title").get());
            if (tSection != null) {
                String title = Shared.platform.replaceAllPlaceholders(tSection.getString("title", ""), pTAB);
                String subtitle = Shared.platform.replaceAllPlaceholders(tSection.getString("subtitle", ""), pTAB);
                int fadeIn = tSection.getInt("fadein", 5);
                int stay = tSection.getInt("stay", 20);
                int fadeOut = tSection.getInt("fadeout", 5);
                pTAB.sendCustomPacket(PacketPlayOutTitle.TITLE(title));
                pTAB.sendCustomPacket(PacketPlayOutTitle.SUBTITLE(subtitle));
                pTAB.sendCustomPacket(PacketPlayOutTitle.TIMES(fadeIn,stay,fadeOut));
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (config.getBoolean("features.chat")) {
            TabPlayer p = TABAPI.getPlayer(e.getPlayer().getUniqueId());
            String msg = e.getMessage();
            ConfigurationSection formats = chatConfig.getConfigurationSection("chat-formats");
            assert formats != null;
            String format = formats.getString("_OTHER_.text");

            format = Shared.platform.replaceAllPlaceholders(format, p).replaceAll("%msg%", msg);

            p.sendCustomPacket(new PacketPlayOutChat(format, PacketPlayOutChat.ChatMessageType.CHAT));
            e.setCancelled(true);
        }

    }
    @EventHandler
    public void onTABLoad(BukkitTABLoadEvent e) {
        TABAdditionsSpigot.getPlugin(TABAdditionsSpigot.class).reload();
    }
}
