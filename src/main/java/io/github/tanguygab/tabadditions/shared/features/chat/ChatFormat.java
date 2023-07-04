package io.github.tanguygab.tabadditions.shared.features.chat;

import io.github.tanguygab.tabadditions.shared.features.advancedconditions.AdvancedConditions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.neznamy.tab.shared.platform.TabPlayer;

@AllArgsConstructor
public class ChatFormat {

    @Getter private final String name;
    @Getter private final String displayName;
    private final AdvancedConditions condition;
    @Getter private final AdvancedConditions viewCondition;
    @Getter private final String channel;
    @Getter private final String text;

    public boolean isConditionMet(TabPlayer p) {
        return condition == null || condition.isMet(p);
    }

    public boolean hasNoViewCondition() {
        return viewCondition == null;
    }

    public boolean isViewConditionMet(TabPlayer sender, TabPlayer viewer) {
        return hasNoViewCondition() || viewCondition.isMet(viewer,sender);
    }
}
