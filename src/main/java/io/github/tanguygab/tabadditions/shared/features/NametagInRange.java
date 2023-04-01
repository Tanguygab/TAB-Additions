package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.feature.*;
import me.neznamy.tab.shared.TAB;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.Future;

public class NametagInRange extends TabFeature implements JoinListener, Loadable, UnLoadable {

    private final TabAPI tab;
    private Future<?> task;

    public NametagInRange() {
        tab = TabAPI.getInstance();
        load();
    }

    @Override
    public String getFeatureName() {
        return "Nametag In Range";
    }

    @Override
    public void onJoin(TabPlayer p) {
        for (TabPlayer p2 : tab.getOnlinePlayers()) {
            tab.getTeamManager().hideNametag(p,p2);
            tab.getTeamManager().hideNametag(p2,p);
        }
    }

    @Override
    public void load() {
        TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(500,this,"handling Nametag In Range",()->{
            int zone = (int) Math.pow(TABAdditions.getInstance().nametagInRange, 2);
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                TabPlayer p = tab.getPlayer(player.getUniqueId());
                for (Player player2 : Bukkit.getServer().getOnlinePlayers()) {
                    TabPlayer p2 = tab.getPlayer(player2.getUniqueId());

                    if (player != player2 && player.getWorld().equals(player2.getWorld()) && player2.getLocation().distanceSquared(player.getLocation()) < zone) {
                        tab.getTeamManager().showNametag(p2,p);
                    }
                    else if (tab.getPlayer(player2.getUniqueId()) != null) {
                        tab.getTeamManager().hideNametag(p2,p);
                    }
                }
            }
        });
    }

    @Override
    public void unload() {
        for (TabPlayer p : tab.getOnlinePlayers()) {
            for (TabPlayer p2 : tab.getOnlinePlayers()) {
                if (p != p2)
                    tab.getTeamManager().showNametag(p,p2);
            }
        }
    }

}
