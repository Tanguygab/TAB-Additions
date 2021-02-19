package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TablistNamesRadius implements Loadable, JoinEventListener {

    private final TabFeature feature;
    private final Plugin plugin;
    private int task = -1;
    private boolean enabled = true;

    public TablistNamesRadius(TabFeature feature) {
        feature.setDisplayName("Tablist Names Radius");
        this.feature = feature;
        plugin = (Plugin) TABAdditions.getInstance().getPlugin();
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            for (Player p2 : Bukkit.getServer().getOnlinePlayers()) {
                if (p != p2)
                    p.hidePlayer(plugin,p2);
            }
        }
        load();
    }

    @Override
    public void load() {
        task = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin,() -> {
            int zone = (int) Math.pow(TABAdditions.getInstance().tablistNamesRadius, 2);
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    if (p != player && p.getWorld().equals(player.getWorld()) && player.getLocation().distanceSquared(p.getLocation()) < zone) {
                        p.showPlayer(plugin,player);
                    }
                    else if (p != player) {
                        p.hidePlayer(plugin,player);
                    }
                }
            }
            if (!enabled) {
                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                    for (Player p2 : Bukkit.getServer().getOnlinePlayers()) {
                        if (p != p2)
                            p.showPlayer(plugin, p2);
                    }
                }
            }
        },0,10);

    }

    @Override
    public void unload() {
        enabled = false;
        Bukkit.getServer().getScheduler().cancelTask(task);
    }

    @Override
    public void onJoin(TabPlayer tabPlayer) {
        Bukkit.getServer().getScheduler().runTask(plugin,()->{
            Player p = (Player) tabPlayer.getPlayer();
            for (Player p2 : Bukkit.getServer().getOnlinePlayers()) {
                p.hidePlayer(plugin,p2);
                p2.hidePlayer(plugin,p);
            }
        });
    }

    @Override
    public TabFeature getFeatureType() {
        return feature;
    }
}
