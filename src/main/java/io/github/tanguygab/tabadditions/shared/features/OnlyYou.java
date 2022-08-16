package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.TabFeature;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class OnlyYou extends TabFeature {

    private final Plugin plugin;

    public OnlyYou() {
        super("Only You","&aOnly You&r");
        plugin = (Plugin) TABAdditions.getInstance().getPlugin();
        load();
    }

    @Override
    public void load() {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            for (Player p2 : Bukkit.getServer().getOnlinePlayers()) {
                if (p != p2)
                    hide(p, p2);
            }
        }
    }

    @Override
    public void unload() {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            for (Player p2 : Bukkit.getServer().getOnlinePlayers()) {
                if (p != p2)
                    show(p, p2);
            }
        }
    }

    @Override
    public void onJoin(TabPlayer tabPlayer) {
        Player p = Bukkit.getServer().getPlayer(tabPlayer.getUniqueId());
        Bukkit.getServer().getScheduler().runTask(plugin, ()->{
                for (Player p2 : Bukkit.getServer().getOnlinePlayers())
                    if (p != p2) {
                        hide(p, p2);
                        hide(p2,p);
                    }
        });
    }

    private void show(Player p, Player target) {
        try {
            p.showPlayer(plugin, target);
        } catch (Exception e) {
            p.showPlayer(target);
        }
    }
    private void hide(Player p, Player target) {
        try {
            p.hidePlayer(plugin, target);
        } catch (Exception e) {
            p.hidePlayer(target);
        }
    }

}
