package io.github.tanguygab.tabadditions.shared.features;

import lombok.Getter;
import me.neznamy.tab.api.nametag.NameTagManager;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import org.jetbrains.annotations.NotNull;

public class ConditionalNametags extends TabFeature implements JoinListener, Refreshable, UnLoadable {

    @Getter private final String featureName = "Conditional Nametags";
    @Getter private final String refreshDisplayName = "&aConditional Nametags&r";
    private final TAB tab;
    private final NameTagManager ntm;
    private final boolean def;

    public ConditionalNametags(boolean def, NameTagManager ntm) {
        tab = TAB.getInstance();
        this.ntm = ntm;
        this.def = def;
        for (TabPlayer p : tab.getOnlinePlayers()) {
            p.loadPropertyFromConfig(this,"nametag-condition");
            refresh(p,true);
        }
    }

    @Override
    public void onJoin(TabPlayer p) {
        p.loadPropertyFromConfig(this,"nametag-condition");
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        if (getCondition(p)) ntm.showNameTag(p);
        else ntm.hideNameTag(p);
    }

    public boolean getCondition(TabPlayer p) {
        Property prop = p.getProperty("nametag-condition");
        if (prop == null) return def;
        String cond = prop.getCurrentRawValue();
        if (cond.isEmpty()) return def;
        return Condition.getCondition(cond).isMet(p);
    }

    @Override
    public void unload() {
        if (!tab.getFeatureManager().isFeatureEnabled("NameTags")) return;
        for (TabPlayer p : tab.getOnlinePlayers())
            ntm.showNameTag(p);
    }

}
