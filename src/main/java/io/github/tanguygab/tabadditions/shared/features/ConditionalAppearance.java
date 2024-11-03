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

import java.util.HashMap;
import java.util.Map;

public class ConditionalAppearance extends RefreshableFeature implements JoinListener, QuitListener, UnLoadable {

    @Getter private final String featureName = "Conditional Appearance";
    @Getter private final String refreshDisplayName = "&aConditional Appearance&r";
    private final TAB tab;
    private final Plugin plugin;
    private final boolean def;
    private final boolean pwp;
    private final Map<TabPlayer, Property> properties = new HashMap<>();

    public ConditionalAppearance(Object plugin, boolean def) {
        tab = TAB.getInstance();
        this.plugin = (Plugin) plugin;
        this.def = def;
        pwp = tab.getFeatureManager().isFeatureEnabled("PerWorldPlayerList");
        if (pwp) addUsedPlaceholder("%world%");
        for (TabPlayer p : tab.getOnlinePlayers()) onJoin(p);
    }

    @Override
    public void onJoin(@NotNull TabPlayer player) {
        properties.put(player, player.loadPropertyFromConfig(this,"appearance-condition", ""));
        refresh(player,true);
    }

    @Override
    public void onQuit(@NotNull TabPlayer player) {
        properties.remove(player);
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        for (TabPlayer all : tab.getOnlinePlayers()) {
            if (p == all) return;
            refresh(p, all);
            refresh(all, p);
        }
    }

    private void refresh(TabPlayer target, TabPlayer viewer) {
        if (getCondition(target,viewer)) sync(()->show(p(viewer),p(target)));
        else sync(()->hide(p(viewer),p(target)));
    }

    private void sync(Runnable run) {
        plugin.getServer().getScheduler().runTask(plugin,run);
    }

    private Player p(TabPlayer p) {
        return (Player) p.getPlayer();
    }

    public boolean getCondition(TabPlayer target, TabPlayer viewer) {
        if (target == null || viewer == null) return def;
        if (pwp && !target.world.equals(viewer.world)) return def;
        Property prop = properties.get(target);
        if (prop == null) return def;
        String cond = prop.getCurrentRawValue();
        if (cond.isEmpty()) return def;
        return def != AdvancedConditions.getCondition(cond).isMet(viewer,target);
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
    private void show(Player viewer, Player target) {
        try {viewer.showPlayer(plugin, target);}
        catch (NoSuchMethodError e) {viewer.showPlayer(target);}
    }
    @SuppressWarnings("deprecation")
    private void hide(Player viewer, Player target) {
        try {viewer.hidePlayer(plugin, target);}
        catch (NoSuchMethodError e) {viewer.hidePlayer(target);}
    }

}
