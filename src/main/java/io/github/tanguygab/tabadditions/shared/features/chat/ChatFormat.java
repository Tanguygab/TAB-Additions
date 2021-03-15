package io.github.tanguygab.tabadditions.shared.features.chat;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
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
        String conditionname = TAB.getInstance().getPlatform().replaceAllPlaceholders(config.get("condition")+"",p);
        Condition condition = Condition.getCondition(conditionname);
        if (condition == null) return true;
        return condition.isMet(p);
    }

    public String getChannel() {
        if (!config.containsKey("channel")) return "";
        return config.get("channel")+"";
    }

    public boolean isInChannel(TabPlayer sender, TabPlayer viewer) {
        if (!config.containsKey("channel")) return true;
        if (sender == viewer) return true;
        ChatManager cm = ChatManager.getInstance();
        return cm.getFormat(sender).getChannel().equals(cm.getFormat(viewer).getChannel());
        //if (!ChatManager.getInstance().channels.containsKey(config.get("channel")+"")) return true;
        //return ChatManager.getInstance().channels.get(config.get("channel")+"").isViewConditionMet(sender,viewer);
    }

    public boolean isViewConditionMet(TabPlayer sender, TabPlayer viewer) {
        if (!config.containsKey("view-condition") || config.get("view-condition").equals("")) return true;
        if (sender == null || viewer == null) return false;
        String conditionname = TAB.getInstance().getPlatform().replaceAllPlaceholders(config.get("view-condition")+"",viewer);
        if (conditionname.startsWith("inRange:")) {
            try {
                int range = Integer.parseInt(conditionname.replace("inRange:",""));
                return ChatManager.getInstance().isInRange(sender,viewer,range);
            } catch (NumberFormatException e) {return true;}
        }
        Condition condition = Condition.getCondition(conditionname);
        if (condition == null) return true;
        return condition.isMet(viewer);
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
