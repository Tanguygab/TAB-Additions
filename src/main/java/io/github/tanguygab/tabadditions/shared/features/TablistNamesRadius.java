package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.features.types.UnLoadable;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TablistNamesRadius extends TabFeature implements UnLoadable, JoinListener {

    private final Plugin plugin;
    private int task = -1;
    private boolean enabled = true;

    public TablistNamesRadius() {
        plugin = (Plugin) TABAdditions.getInstance().getPlugin();
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            for (Player p2 : Bukkit.getServer().getOnlinePlayers()) {
                if (p != p2)
                    hide(p,p2);
            }
        }

        task = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin,() -> {
            int zone = (int) Math.pow(TABAdditions.getInstance().tablistNamesRadius, 2);
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    if (p != player
                            && p.getWorld().equals(player.getWorld())
                            && player.getLocation().distanceSquared(p.getLocation()) < zone
                            && !TAB.getInstance().getPlayer(p.getUniqueId()).isVanished()
                    ) show(p,player);
                    else if (p != player) hide(p,player);
                }
            }
            if (!enabled) {
                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                    for (Player p2 : Bukkit.getServer().getOnlinePlayers()) {
                        if (p != p2)
                            show(p, p2);
                    }
                }
            }
        },0,10);
    }

    @Override
    public String getFeatureName() {
        return "Tablist Names Radius";
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
                hide(p,p2);
                hide(p2,p);
            }
        });
    }

    private void show(Player p, Player target) {
        try {p.showPlayer(plugin, target);}
        catch (NoSuchMethodError e) {p.showPlayer(target);}
    }
    private void hide(Player p, Player target) {
        try {p.hidePlayer(plugin, target);}
        catch (NoSuchMethodError e) {p.hidePlayer(target);}
    }
}
