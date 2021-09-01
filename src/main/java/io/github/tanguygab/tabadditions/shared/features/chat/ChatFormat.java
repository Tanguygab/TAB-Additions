package io.github.tanguygab.tabadditions.shared.features.chat;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;

import java.util.Map;

public class ChatFormat {

    private final String name;
    private final Map<String, String> config;

    public ChatFormat(String name, Map<String,String> config) {
        this.name = name;
        this.config = config;
    }

    public String getName() {
        return name;
    }
    public String getChildLayout() {
        String child = config.get("if-condition-not-met");
        if (child == null) child = "";
        return child;
    }
    public boolean isConditionMet(TabPlayer p) {
        if (!config.containsKey("condition")) return true;
        return TABAdditions.getInstance().isConditionMet(config.get("condition"),p);
    }

    public String getChannel() {
        if (!config.containsKey("channel")) return "";
        return config.get("channel")+"";
    }
    public String getViewCondition() {
        if (!config.containsKey("view-condition")) return "";
        return config.get("view-condition")+"";
    }

    public boolean isViewConditionMet(TabPlayer sender, TabPlayer viewer) {
        if (!config.containsKey("view-condition") || config.get("view-condition").equals("")) return true;
        return TABAdditions.getInstance().isConditionMet(config.get("view-condition"),sender,viewer,viewer);

    }

    public String getText() {
        return config.get("text");
    }


}

