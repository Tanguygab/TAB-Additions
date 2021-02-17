package io.github.tanguygab.tabadditions.spigot.Features;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class NametagInRange implements Loadable, JoinEventListener {

    private final TabFeature feature;

    public NametagInRange(TabFeature feature) {
        feature.setDisplayName("Nametag in Range");
        this.feature = feature;
        for (TabPlayer p : TAB.getInstance().getPlayers()) {
            for (TabPlayer p2 : TAB.getInstance().getPlayers()) {
                if (p != p2)
                    p.hideNametag(p2.getUniqueId());
            }
        }
        load();
    }

    @Override
    public void onJoin(TabPlayer p) {
        for (TabPlayer p2 : TAB.getInstance().getPlayers()) {
            p.hideNametag(p2.getUniqueId());
            p2.hideNametag(p.getUniqueId());
        }
    }

    public void load() {
        TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(500,"handling Nametag In Range", feature, UsageType.REPEATING_TASK,()->{
            int zone = (int) Math.pow(TABAdditions.getInstance().nametagInRange, 2);
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    TabPlayer p2 = TAB.getInstance().getPlayer(player.getUniqueId());

                    if (p != player && p.getWorld().equals(player.getWorld()) && player.getLocation().distanceSquared(p.getLocation()) < zone) {
                        p2.showNametag(p.getUniqueId());
                    }
                    else if (TAB.getInstance().getPlayer(player.getUniqueId()) != null) {
                        p2.hideNametag(p.getUniqueId());
                    }
                }
            }
        });
    }

    @Override
    public void unload() {
        for (TabPlayer p : TAB.getInstance().getPlayers()) {
            for (TabPlayer p2 : TAB.getInstance().getPlayers()) {
                if (p != p2)
                    p.showNametag(p2.getUniqueId());
            }
        }
    }

    @Override
    public TabFeature getFeatureType() {
        return feature;
    }
}
