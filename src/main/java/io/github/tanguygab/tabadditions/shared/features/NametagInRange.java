package io.github.tanguygab.tabadditions.shared.features;

import lombok.Getter;
import me.neznamy.tab.api.nametag.NameTagManager;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.TAB;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NametagInRange extends TabFeature implements JoinListener, UnLoadable {

    @Getter private final String featureName = "Nametag In Range";
    private final TAB tab;
    private final NameTagManager ntm;

    public NametagInRange(int range, NameTagManager ntm) {
        tab = TAB.getInstance();
        this.ntm = ntm;
        tab.getCPUManager().startRepeatingMeasuredTask(500,"&a"+featureName+"&r","handling Nametag In Range",()->{
            int zone = (int) Math.pow(range, 2);
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                TabPlayer p = tab.getPlayer(player.getUniqueId());
                if (p == null) continue;
                for (Player player2 : Bukkit.getServer().getOnlinePlayers()) {
                    TabPlayer p2 = tab.getPlayer(player2.getUniqueId());
                    if (p2 == null) continue;

                    if (player != player2 && player.getWorld().equals(player2.getWorld()) && player2.getLocation().distanceSquared(player.getLocation()) < zone)
                        ntm.showNameTag(p2,p);
                    else if (tab.getPlayer(player2.getUniqueId()) != null) ntm.hideNameTag(p2,p);
                }
            }
        });
    }

    @Override
    public void onJoin(@NotNull TabPlayer p) {
        for (TabPlayer p2 : tab.getOnlinePlayers()) {
            ntm.hideNameTag(p,p2);
            ntm.hideNameTag(p2,p);
        }
    }

    @Override
    public void unload() {
        if (!tab.getFeatureManager().isFeatureEnabled("NameTags")) return;
        for (TabPlayer p : tab.getOnlinePlayers()) {
            for (TabPlayer p2 : tab.getOnlinePlayers()) {
                if (p != p2)
                    ntm.showNameTag(p,p2);
            }
        }
    }

}
