package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.shared.features.advancedconditions.AdvancedConditions;
import lombok.Getter;
import me.neznamy.tab.api.nametag.NameTagManager;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ConditionalNametags extends RefreshableFeature implements JoinListener, QuitListener, UnLoadable {

    @Getter private final String featureName = "Conditional Nametags";
    @Getter private final String refreshDisplayName = "&aConditional Nametags&r";
    private final TAB tab;
    private final NameTagManager ntm;
    private final boolean def;
    private final boolean relational;
    private final Map<TabPlayer, Property> properties = new HashMap<>();

    public ConditionalNametags(boolean def, boolean relational) {
        tab = TAB.getInstance();
        this.ntm = tab.getNameTagManager();
        this.def = def;
        this.relational = relational;
        for (TabPlayer all : tab.getOnlinePlayers()) onJoin(all);
    }

    @Override
    public void onJoin(@NotNull TabPlayer player) {
        properties.put(player, player.loadPropertyFromConfig(this,"nametag-condition", ""));
        refresh(player, true);
    }

    @Override
    public void onQuit(@NotNull TabPlayer player) {
        properties.remove(player);
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        if (!p.isLoaded()) return;
        if (relational) {
            for (TabPlayer all : tab.getOnlinePlayers()) {
                if (p == all || !all.isLoaded()) continue;
                refresh(p, all);
                refresh(all, p);
            }
            return;
        }
        refresh(p,p);
    }

    private void refresh(TabPlayer target, TabPlayer viewer) {
        if (target != viewer) {
            if (getCondition(target,viewer)) ntm.showNameTag(target,viewer);
            else ntm.hideNameTag(target,viewer);
            return;
        }
        if (getCondition(target,target)) ntm.showNameTag(target);
        else ntm.hideNameTag(target);
    }


    public boolean getCondition(TabPlayer target, TabPlayer viewer) {
        if (target == null || viewer == null) return def;
        Property prop = properties.get(target);
        if (prop == null) return def;
        String cond = prop.getCurrentRawValue();
        if (cond.isEmpty()) return def;
        return def != AdvancedConditions.getCondition(cond).isMet(viewer,target);
    }

    @Override
    public void unload() {
        if (!tab.getFeatureManager().isFeatureEnabled("NameTags")) return;
        for (TabPlayer p : tab.getOnlinePlayers())
            if (p.isLoaded())
                ntm.showNameTag(p);
    }
}
