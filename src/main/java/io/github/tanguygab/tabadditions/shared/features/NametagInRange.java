package io.github.tanguygab.tabadditions.shared.features;

import lombok.Getter;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.TAB;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class NametagInRange extends TabFeature implements JoinListener, UnLoadable {

    @Getter private final String featureName = "Nametag In Range";
    private final TAB tab;

    public NametagInRange(int range) {
        tab = TAB.getInstance();
        tab.getCPUManager().startRepeatingMeasuredTask(500,this,"handling Nametag In Range",()->{
            int zone = (int) Math.pow(range, 2);
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
    public void onJoin(TabPlayer p) {
        for (TabPlayer p2 : tab.getOnlinePlayers()) {
            tab.getTeamManager().hideNametag(p,p2);
            tab.getTeamManager().hideNametag(p2,p);
        }
    }

    @Override
    public void unload() {
        if (!tab.getFeatureManager().isFeatureEnabled("Nametag")) return;
        for (TabPlayer p : tab.getOnlinePlayers()) {
            for (TabPlayer p2 : tab.getOnlinePlayers()) {
                if (p != p2)
                    tab.getTeamManager().showNametag(p,p2);
            }
        }
    }

}
