package io.github.tanguygab.tabadditions.shared.features.chat;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

import java.util.Map;

public class ChatFormat {

    private final String name;
    private final Condition condition;
    private final String child;
    private final String channel;
    private final String viewCondition;
    private final String text;

    public ChatFormat(String name, Condition condition, String child, String channel, String viewCondition, String text) {
        this.name = name;
        this.condition = condition;
        this.child = child;
        this.channel = channel;
        this.viewCondition = viewCondition;
        this.text = text;
    }

    public String getName() {
        return name;
    }
    public String getChildLayout() {
        return child;
    }
    public boolean isConditionMet(TabPlayer p) {
        return condition == null || condition.isMet(p);
    }

    public String getChannel() {
        return channel;
    }

    public boolean hasViewCondition() {
        return viewCondition != null;
    }

    public boolean isViewConditionMet(TabPlayer sender, TabPlayer viewer) {
        return !hasViewCondition() || TABAdditions.getInstance().isConditionMet(viewCondition,sender,viewer,true);
    }

    public String getText() {
        return text;
    }


}
