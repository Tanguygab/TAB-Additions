package io.github.tanguygab.tabadditions.shared.features.chat;

import io.github.tanguygab.tabadditions.shared.features.advancedconditions.AdvancedConditions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.neznamy.tab.shared.platform.TabPlayer;

@AllArgsConstructor
public class ChatFormat {

    @Getter private final String name;
    private final AdvancedConditions condition;
    private final AdvancedConditions viewCondition;
    @Getter private final String channel;
    @Getter private final String text;

    public boolean isConditionMet(TabPlayer p) {
        return condition == null || condition.isMet(p);
    }

    public boolean hasViewCondition() {
        return viewCondition != null;
    }

    public boolean isViewConditionMet(TabPlayer sender, TabPlayer viewer) {
        return !hasViewCondition() || viewCondition.isMet(viewer,sender);
    }
}
