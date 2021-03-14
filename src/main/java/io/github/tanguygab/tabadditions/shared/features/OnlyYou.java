package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class OnlyYou implements JoinEventListener, Loadable {

    private final TabFeature feature;
    private final Plugin plugin;

    public OnlyYou(TabFeature feature) {
        feature.setDisplayName("&aOnly You");
        this.feature = feature;
        plugin = (Plugin) TABAdditions.getInstance().getPlugin();
        load();
    }

    @Override
    public void load() {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            for (Player p2 : Bukkit.getServer().getOnlinePlayers()) {
                if (p != p2)
                    p.hidePlayer(plugin, p2);
            }
        }
    }

    @Override
    public void unload() {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            for (Player p2 : Bukkit.getServer().getOnlinePlayers()) {
                if (p != p2)
                    p.showPlayer(plugin, p2);
            }
        }
    }

    @Override
    public void onJoin(TabPlayer tabPlayer) {
        Player p = Bukkit.getServer().getPlayer(tabPlayer.getUniqueId());
        for (Player p2 : Bukkit.getServer().getOnlinePlayers())
            if (p != p2)
                p.hidePlayer(plugin, p2);
    }

    @Override
    public TabFeature getFeatureType() {
        return feature;
    }
}
