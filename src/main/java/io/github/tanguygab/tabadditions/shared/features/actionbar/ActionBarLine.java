package io.github.tanguygab.tabadditions.shared.features.actionbar;


import lombok.AllArgsConstructor;
import lombok.Getter;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;

@AllArgsConstructor
public class ActionBarLine {

    @Getter private final String text;
    private final Condition condition;

    public boolean isConditionMet(TabPlayer player) {
        return condition == null || condition.isMet(player);
    }
}
