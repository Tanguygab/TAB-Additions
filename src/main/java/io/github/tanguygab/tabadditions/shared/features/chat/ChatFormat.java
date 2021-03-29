package io.github.tanguygab.tabadditions.shared.features.chat;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatFormat {

    private final String name;
    private final Map<String,Object> config;

    public ChatFormat(String name,Map<String,Object> config) {
        this.name = name;
        this.config = config;
    }

    public String getName() {
        return name;
    }
    public String getChildLayout() {
        Object child = config.get("if-condition-not-met");
        if (child == null)
            child = "";
        return child+"";
    }
    public boolean isConditionMet(TabPlayer p) {
        if (!config.containsKey("condition")) return true;
        return TABAdditions.getInstance().isConditionMet(config.get("condition")+"",p);
    }

    public String getChannel() {
        if (!config.containsKey("channel")) return "";
        return config.get("channel")+"";
    }

    public boolean isViewConditionMet(TabPlayer sender, TabPlayer viewer) {
        if (!config.containsKey("view-condition") || config.get("view-condition").equals("")) return true;
        if (sender == null || viewer == null) return false;
        String conditionname = TABAdditions.getInstance().parsePlaceholders(config.get("view-condition")+"",sender,viewer,viewer);
        for (String cond : conditionname.split(";")) {
            if (cond.startsWith("!inRange:") || cond.startsWith("inRange:")) {
                try {
                    int range = Integer.parseInt(cond.replace("!", "").replace("inRange:", ""));
                    boolean result = ChatManager.getInstance().isInRange(sender, viewer, range);
                    if (cond.startsWith("!") && result) return false;
                    if (!cond.startsWith("!") && !result) return false;
                } catch (NumberFormatException ignored) {}
            } else {
                Condition condition = Condition.getCondition(cond.replace("!",""));
                if (condition != null) {
                    if (cond.startsWith("!") && condition.isMet(viewer)) return false;
                    if (!cond.startsWith("!") && !condition.isMet(viewer)) return false;
                }
            }
        }
        return true;

    }

    public IChatBaseComponent getText() {
        IChatBaseComponent finalText = new IChatBaseComponent().setText("");

        Map<String,Map<String,Object>> components = ((Map<String,Map<String,Object>>) config.get("components"));
        List<IChatBaseComponent> list = new ArrayList<>();
        if (components != null)
            for (String component : components.keySet()) {
                IChatBaseComponent text = new IChatBaseComponent("");
                Map<String,Object> complist = components.get(component);

                if (complist.containsKey("text"))
                    text = IChatBaseComponent.fromColoredText(complist.get("text")+"");

                List<String> hover = (List<String>) complist.get("hover");
                String hoverTxt = "";
                if (hover != null) {
                    for (String str : hover) {
                        hoverTxt = hoverTxt + str;
                        if (hover.indexOf(str) < hover.size()-1)
                            hoverTxt = hoverTxt + "\n";
                    }
                }
                text.onHoverShowText(hoverTxt);

                if (complist.containsKey("suggest"))
                    text.onClickSuggestCommand(complist.get("suggest")+"");

                list.add(text);
            }
        finalText.setExtra(list);
        return finalText;
    }


}
