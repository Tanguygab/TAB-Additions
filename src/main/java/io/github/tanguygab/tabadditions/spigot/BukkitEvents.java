package io.github.tanguygab.tabadditions.spigot;

import me.neznamy.tab.api.TABAPI;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;


public class BukkitEvents implements Listener {

    private final FileConfiguration config;
    private final FileConfiguration titleConfig;
    private final FileConfiguration actionbarConfig;
    private boolean tag = false;

    public BukkitEvents(FileConfiguration config, FileConfiguration titleConfig, FileConfiguration actionbarConfig) {
        this.config = config;
        this.titleConfig = titleConfig;
        this.actionbarConfig = actionbarConfig;
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {

        boolean sneak = e.isSneaking();
        Player p = e.getPlayer();
        if (sneak) {
            tag = TABAPI.hasHiddenNametag(p.getUniqueId());
            if (config.getBoolean("features.sneak-hide-nametags"))
                TABAPI.hideNametag(p.getUniqueId());
        }
        else
            if (!tag)
                TABAPI.showNametag(p.getUniqueId());

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        TabPlayer pTAB = TABAPI.getPlayer(p.getUniqueId());

        pTAB.loadPropertyFromConfig("actionbar");
        String actionbar = actionbarConfig.getString("bars." + pTAB.getProperty("actionbar").get(), "");
        actionbar = Shared.platform.replaceAllPlaceholders(actionbar, pTAB);
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(actionbar).create());

        pTAB.loadPropertyFromConfig("title");
        ConfigurationSection tSection = titleConfig.getConfigurationSection("titles." + pTAB.getProperty("title").get());
        if (tSection != null) {
            String title = Shared.platform.replaceAllPlaceholders(tSection.getString("title", ""), pTAB);
            String subtitle = Shared.platform.replaceAllPlaceholders(tSection.getString("subtitle", ""), pTAB);
            int fadeIn = tSection.getInt("fadein", 5);
            int stay = tSection.getInt("stay", 20);
            int fadeOut = tSection.getInt("fadeout", 5);
            p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        }

    }
}
