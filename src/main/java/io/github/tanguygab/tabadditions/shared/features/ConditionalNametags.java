package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.shared.ConfigType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.Property;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.team.TeamManager;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

import java.util.concurrent.Future;

public class ConditionalNametags extends TabFeature {

    private final TabAPI tab;
    private Future<?> task;
    private final boolean def;

    public ConditionalNametags() {
        tab = TabAPI.getInstance();
        for (TabPlayer p : tab.getOnlinePlayers()) p.loadPropertyFromConfig(this,"nametag-condition");
        def = TABAdditions.getInstance().getConfig(ConfigType.MAIN).getBoolean("conditional-nametags.show-by-default",true);
        load();
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
    public void load() {
        task = tab.getThreadManager().startRepeatingMeasuredTask(500,this,"handling Conditional Nametags",()->{
            TeamManager tm = tab.getTeamManager();
            for (TabPlayer p : tab.getOnlinePlayers()) {
                if (p == null) return;
                if (getCondition(p)) tm.showNametag(p);
                else tm.hideNametag(p);
            }
        });
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
        task.cancel(true);
        for (TabPlayer p : tab.getOnlinePlayers())
            tab.getTeamManager().showNametag(p);
    }

}
