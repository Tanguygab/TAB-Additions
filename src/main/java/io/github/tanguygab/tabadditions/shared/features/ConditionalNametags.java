package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.team.TeamManager;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

public class ConditionalNametags extends TabFeature implements JoinListener, Refreshable, UnLoadable {

    private final TAB tab;
    private final boolean def;

    public ConditionalNametags() {
        tab = TAB.getInstance();
        def = TABAdditions.getInstance().getConfig().getBoolean("conditional-nametags.show-by-default",true);
        for (TabPlayer p : tab.getOnlinePlayers()) {
            p.loadPropertyFromConfig(this,"nametag-condition");
            refresh(p,true);
        }
    }

    @Override
    public String getFeatureName() {
        return "Conditional Nametags";
    }
    @Override
    public String getRefreshDisplayName() {
        return "&aConditional Nametags&r";
    }

    @Override
    public void onJoin(TabPlayer p) {
        p.loadPropertyFromConfig(this,"nametag-condition");
    }

    @Override
    public void refresh(TabPlayer p, boolean force) {
        TeamManager tm = tab.getTeamManager();
        if (getCondition(p)) tm.showNametag(p);
        else tm.hideNametag(p);
    }

    public boolean getCondition(TabPlayer p) {
        Property prop = p.getProperty("nametag-condition");
        if (prop == null) return def;
        String cond = prop.getCurrentRawValue();
        if (cond.equals("")) return def;
        return Condition.getCondition(cond).isMet(p);
    }

    @Override
    public void unload() {
        for (TabPlayer p : tab.getOnlinePlayers())
            tab.getTeamManager().showNametag(p);
    }

}
