package io.github.tanguygab.tabadditions.shared.features;

import lombok.Getter;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.features.types.UnLoadable;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class TabnameInRange extends TabFeature implements UnLoadable, JoinListener {

    @Getter private final String featureName = "Tabname In Range";
    private final Plugin plugin;
    private final int task;
    private boolean enabled = true;

    public TabnameInRange(Object plugin, int range) {
        this.plugin = (Plugin) plugin;
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            for (Player p2 : Bukkit.getServer().getOnlinePlayers()) {
                if (p != p2)
                    hide(p,p2);
            }
        }

        task = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin,() -> {
            int zone = (int) Math.pow(range, 2);
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    TabPlayer tp = TAB.getInstance().getPlayer(p.getUniqueId());
                    if (p != player
                            && p.getWorld().equals(player.getWorld())
                            && player.getLocation().distanceSquared(p.getLocation()) < zone
                            && (tp == null || tp.isVanished())
                    ) show(p,player);
                    else if (p != player) hide(p,player);
                }
            }
            if (enabled) return;
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                for (Player p2 : Bukkit.getServer().getOnlinePlayers()) {
                    if (p != p2)
                        show(p, p2);
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
    public void onJoin(@NotNull TabPlayer p) {
        Bukkit.getServer().getScheduler().runTask(plugin,()->{
            Player player = (Player) p.getPlayer();
            for (Player all : Bukkit.getServer().getOnlinePlayers()) {
                hide(player,all);
                hide(all,player);
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void show(Player p, Player target) {
        try {p.showPlayer(plugin, target);}
        catch (NoSuchMethodError e) {p.showPlayer(target);}
    }
    @SuppressWarnings("deprecation")
    private void hide(Player p, Player target) {
        try {p.hidePlayer(plugin, target);}
        catch (NoSuchMethodError e) {p.hidePlayer(target);}
    }
}
