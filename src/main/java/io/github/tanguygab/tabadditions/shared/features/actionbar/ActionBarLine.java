package io.github.tanguygab.tabadditions.shared.features.actionbar;


import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;

public class ActionBarLine {

    private final String name;
    private final String text;
    private final Condition condition;

    public ActionBarLine(String name, String text, Condition condition) {
        this.name = name;
        this.text = text;
        this.condition = condition;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public boolean isConditionMet(TabPlayer player) {
        return condition == null || condition.isMet(player);
    }
}
