package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.team.TeamManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class SneakHideNametag extends TabFeature implements Listener {

    private final Map<TabPlayer, Boolean> tag = new HashMap<>();

    public SneakHideNametag() {
        super("&aSneak Hide Nametag&r");
        load();
    }

    @Override
    public void load() {
        Plugin plugin = (Plugin) TABAdditions.getInstance().getPlugin();
        plugin.getServer().getScheduler().runTask(plugin,()->plugin.getServer().getPluginManager().registerEvents(this,plugin));
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        TabPlayer p = TabAPI.getInstance().getPlayer(e.getPlayer().getUniqueId());
        boolean isSneaking = e.isSneaking();
        TeamManager tm = TabAPI.getInstance().getTeamManager();

        if (isSneaking) {
            tag.put(p, tm.hasHiddenNametag(p));
            tm.hideNametag(p);
        } else if (!tag.get(p))
            tm.showNametag(p);
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(this);
    }
}
