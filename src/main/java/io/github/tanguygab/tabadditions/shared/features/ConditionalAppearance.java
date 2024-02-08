package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.shared.features.advancedconditions.AdvancedConditions;
import lombok.Getter;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class ConditionalAppearance extends TabFeature implements Refreshable, UnLoadable, JoinListener {

    @Getter private final String featureName = "Conditional Appearance";
    @Getter private final String refreshDisplayName = "&aConditional Appearance&r";
    private final TAB tab;
    private final Plugin plugin;
    private final boolean def;
    private final boolean pwp;

    public ConditionalAppearance(Object plugin, boolean def) {
        tab = TAB.getInstance();
        this.plugin = (Plugin) plugin;
        this.def = def;
        pwp = tab.getFeatureManager().isFeatureEnabled("PerWorldPlayerList");
        if (pwp) addUsedPlaceholder("%world%");
        for (TabPlayer p : tab.getOnlinePlayers()) onJoin(p);
    }

    @Override
    public void onJoin(TabPlayer p) {
        p.loadPropertyFromConfig(this,"appearance-condition");
        refresh(p,true);
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        for (TabPlayer all : tab.getOnlinePlayers())
            refresh(p,all);
    }

    private void refresh(TabPlayer p, TabPlayer all) {
        if (p == all) return;
        if (getCondition(p,all)) sync(()->show(p(p),p(all)));
        else sync(()->hide(p(p),p(all)));
    }

    private void sync(Runnable run) {
        plugin.getServer().getScheduler().runTask(plugin,run);
    }

    private Player p(TabPlayer p) {
        return (Player) p.getPlayer();
    }

    public boolean getCondition(TabPlayer player, TabPlayer player2) {
        if (player == null || player2 == null) return def;
        if (pwp && !player.getWorld().equals(player2.getWorld())) return def;
        Property prop = player.getProperty("appearance-condition");
        if (prop == null) return def;
        String cond = prop.getCurrentRawValue();
        if (cond.isEmpty()) return def;
        return def != AdvancedConditions.getCondition(cond).isMet(player,player2);
    }

    @Override
    public void unload() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            for (Player all : Bukkit.getOnlinePlayers()) {
                if (p != all) show(p,all);
            }
        }
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
