package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.types.Loadable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.HashMap;
import java.util.Map;

public class SitHideNametag implements Loadable, Listener {

    private final Map<TabPlayer, Boolean> tag = new HashMap<>();

    public SitHideNametag() {
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
        TabPlayer p = TAB.getInstance().getPlayer(e.getEntity().getUniqueId());
        tag.put(p, p.hasHiddenNametag());
        p.hideNametag();
    }

    @EventHandler
    public void onDismount(EntityDismountEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        TabPlayer p = TAB.getInstance().getPlayer(e.getEntity().getUniqueId());
        if (!tag.get(p))
            p.showNametag();
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public Object getFeatureType() {
        return TAFeature.SIT_HIDE_NAMETAG;
    }
}