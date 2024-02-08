package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.shared.features.advancedconditions.AdvancedConditions;
import lombok.Getter;
import me.neznamy.tab.api.nametag.NameTagManager;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class ConditionalNametags extends TabFeature implements JoinListener, Refreshable, UnLoadable {

    @Getter private final String featureName = "Conditional Nametags";
    @Getter private final String refreshDisplayName = "&aConditional Nametags&r";
    private final TAB tab;
    private final NameTagManager ntm;
    private final boolean def;
    private final boolean relational;

    public ConditionalNametags(boolean def, boolean relational) {
        tab = TAB.getInstance();
        this.ntm = tab.getNameTagManager();
        this.def = def;
        this.relational = relational;
        for (TabPlayer p : tab.getOnlinePlayers()) onJoin(p);
    }

    @Override
    public void onJoin(TabPlayer p) {
        p.loadPropertyFromConfig(this,"nametag-condition");
        refresh(p,true);
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        if (relational) {
            for (TabPlayer all : tab.getOnlinePlayers()) {
                if (p == all) continue;
                if (getCondition(p,all)) ntm.showNameTag(p,all);
                else ntm.hideNameTag(p,all);
            }
            return;
        }
        if (getCondition(p,p)) ntm.showNameTag(p);
        else ntm.hideNameTag(p);
    }

    public boolean getCondition(TabPlayer player, TabPlayer player2) {
        if (player == null || player2 == null) return def;
        Property prop = player.getProperty("nametag-condition");
        if (prop == null) return def;
        String cond = prop.getCurrentRawValue();
        if (cond.isEmpty()) return def;
        return def != AdvancedConditions.getCondition(cond).isMet(player,player2);
    }

    @Override
    public void unload() {
        if (!tab.getFeatureManager().isFeatureEnabled("NameTags")) return;
        for (TabPlayer p : tab.getOnlinePlayers())
            ntm.showNameTag(p);
    }

}
