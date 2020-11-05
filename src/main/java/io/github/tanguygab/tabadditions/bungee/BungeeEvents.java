package io.github.tanguygab.tabadditions.bungee;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

public class BungeeEvents implements Listener {

    private final Configuration config;
    private final Configuration titleConfig;
    private final Configuration actionbarConfig;

    public BungeeEvents(Configuration config, Configuration titleConfig, Configuration actionbarConfig) {
        this.config = config;
        this.actionbarConfig = actionbarConfig;
        this.titleConfig = titleConfig;
    }

    @EventHandler
    public void onJoin(ServerSwitchEvent e) {
        ProxiedPlayer p = e.getPlayer();
        ServerInfo server = p.getServer().getInfo();

    }
}
