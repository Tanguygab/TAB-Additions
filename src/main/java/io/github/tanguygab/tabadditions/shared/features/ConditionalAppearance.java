package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.shared.ConfigType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ConditionalAppearance extends TabFeature implements Refreshable, UnLoadable, JoinListener {

    private final TAB tab;
    private final Plugin plugin;
    private final boolean def;

    public ConditionalAppearance() {
        tab = TAB.getInstance();
        plugin = (Plugin) TABAdditions.getInstance().getPlugin();
        def = TABAdditions.getInstance().getConfig(ConfigType.MAIN).getBoolean("appearance-nametags.show-by-default",true);
        for (TabPlayer p : tab.getOnlinePlayers()) {
            p.loadPropertyFromConfig(this,"appearance-condition");
            refresh(p,true);
        }
    }

    @Override
    public String getFeatureName() {
        return "Conditional Appearance";
    }
    @Override
    public String getRefreshDisplayName() {
        return "&aConditional Appearance&r";
    }

    @Override
    public void onJoin(TabPlayer p) {
        p.loadPropertyFromConfig(this,"appearance-condition");
        for (Player all : Bukkit.getOnlinePlayers()) {
            refresh(p(p),all);
            refresh(all,p(p));
        }
    }

    @Override
    public void refresh(TabPlayer p, boolean force) {
        for (Player all : Bukkit.getOnlinePlayers()) {
            refresh(p(p),all);
        }
    }

    private void refresh(Player p, Player all) {
        if (p == all) return;
        if (getCondition(p)) sync(()->show(p,all));
        else sync(()->hide(p,all));
    }

    private void sync(Runnable run) {
        plugin.getServer().getScheduler().runTask(plugin,run);
    }

    private Player p(TabPlayer p) {
        return (Player) p.getPlayer();
    }

    public boolean getCondition(Player player) {
        TabPlayer p = tab.getPlayer(player.getUniqueId());
        if (p == null) return def;
        Property prop = p.getProperty("appearance-condition");
        if (prop == null) return def;
        String cond = prop.getCurrentRawValue();
        if (cond.equals("")) return def;
        return Condition.getCondition(cond).isMet(p);
    }

    @Override
    public void unload() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            for (Player all : Bukkit.getOnlinePlayers()) {
                if (p != all) show(p,all);
            }
        }
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
