package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.feature.Loadable;
import me.neznamy.tab.api.feature.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.feature.UnLoadable;
import me.neznamy.tab.api.team.TeamManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class SneakHideNametag extends TabFeature implements Listener, Loadable, UnLoadable {

    private final Map<TabPlayer, Boolean> tag = new HashMap<>();

    public SneakHideNametag() {
        load();
    }

    @Override
    public String getFeatureName() {
        return "Sneak Hide Nametag";
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
