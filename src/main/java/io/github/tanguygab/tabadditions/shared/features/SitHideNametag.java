package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.team.TeamManager;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.features.types.UnLoadable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.HashMap;
import java.util.Map;

public class SitHideNametag extends TabFeature implements Listener, UnLoadable {

    private final TabAPI tab;
    private final Map<TabPlayer, Boolean> tag = new HashMap<>();

    public SitHideNametag() {
        tab = TabAPI.getInstance();
        Plugin plugin = (Plugin) TABAdditions.getInstance().getPlugin();
        plugin.getServer().getScheduler().runTask(plugin,()->plugin.getServer().getPluginManager().registerEvents(this,plugin));
    }

    @Override
    public String getFeatureName() {
        return "Site Hide Nametag";
    }

    @EventHandler
    public void onMount(EntityMountEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        TabPlayer p = tab.getPlayer(e.getEntity().getUniqueId());
        if (p == null) return;
        TeamManager tm = tab.getTeamManager();
        tag.put(p, tm.hasHiddenNametag(p));
        tm.hideNametag(p);
    }

    @EventHandler
    public void onDismount(EntityDismountEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        TabPlayer p = tab.getPlayer(e.getEntity().getUniqueId());
        if (p == null) return;
        if (tag.containsKey(p) && !tag.get(p))
            tab.getTeamManager().showNametag(p);
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(this);
    }
}