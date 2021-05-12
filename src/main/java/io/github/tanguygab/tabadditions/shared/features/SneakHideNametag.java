package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.types.Loadable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class SneakHideNametag implements Loadable, Listener {

    private final Map<TabPlayer, Boolean> tag = new HashMap<>();

    public SneakHideNametag() {
        load();
    }

    @Override
    public void load() {
        Plugin plugin = (Plugin) TABAdditions.getInstance().getPlugin();
        plugin.getServer().getScheduler().runTask(plugin,()->plugin.getServer().getPluginManager().registerEvents(this,plugin));
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        TabPlayer p = TAB.getInstance().getPlayer(e.getPlayer().getUniqueId());
        boolean isSneaking = e.isSneaking();

        if (isSneaking) {
            tag.put(p, p.hasHiddenNametag());
            p.hideNametag();
        } else if (!tag.get(p))
            p.showNametag();
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public Object getFeatureType() {
        return TAFeature.SNEAK_HIDE_NAMETAG;
    }
}
