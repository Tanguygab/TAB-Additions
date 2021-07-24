package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.team.ScoreboardTeamManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.HashMap;
import java.util.Map;

public class SitHideNametag extends TabFeature implements Listener {

    private final Map<TabPlayer, Boolean> tag = new HashMap<>();

    public SitHideNametag() {
        super("&aSit Hide Nametag&r");
        load();
    }

    @Override
    public void load() {
        Plugin plugin = (Plugin) TABAdditions.getInstance().getPlugin();
        plugin.getServer().getScheduler().runTask(plugin,()->plugin.getServer().getPluginManager().registerEvents(this,plugin));
    }

    @EventHandler
    public void onMount(EntityMountEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        TabPlayer p = TabAPI.getInstance().getPlayer(e.getEntity().getUniqueId());
        if (p == null) return;
        ScoreboardTeamManager tm = TabAPI.getInstance().getScoreboardTeamManager();
        tag.put(p, tm.hasHiddenNametag(p));
        tm.hideNametag(p);
    }

    @EventHandler
    public void onDismount(EntityDismountEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        TabPlayer p = TabAPI.getInstance().getPlayer(e.getEntity().getUniqueId());
        if (p == null) return;
        if (tag.containsKey(p) && !tag.get(p))
            TabAPI.getInstance().getScoreboardTeamManager().showNametag(p);
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(this);
    }
}