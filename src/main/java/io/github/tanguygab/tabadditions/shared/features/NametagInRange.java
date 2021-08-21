package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.api.TabFeature;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class NametagInRange extends TabFeature {

    private final TabAPI tab;

    public NametagInRange() {
        super("&aNametag in Range&r");
        tab = TabAPI.getInstance();
        for (TabPlayer p : tab.getOnlinePlayers()) {
            for (TabPlayer p2 : tab.getOnlinePlayers()) {
                if (p != p2)
                    tab.getTeamManager().hideNametag(p,p2);
            }
        }
        load();
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
        TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(500,"handling Nametag In Range", this, "repeating task",()->{
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
