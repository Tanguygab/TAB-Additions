package io.github.tanguygab.tabadditions.shared.features.chat;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatFormat {

    private final String name;
    private final Map<String,Object> config;
    private final TabFeature feature;

    public ChatFormat(String name, Map<String,Object> config, TabFeature feature) {
        this.name = name;
        this.config = config;
        this.feature = feature;
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
        return TABAdditions.getInstance().isConditionMet(config.get("condition")+"",p,feature);
    }

    public String getChannel() {
        if (!config.containsKey("channel")) return "";
        return config.get("channel")+"";
    }

    public boolean isViewConditionMet(TabPlayer sender, TabPlayer viewer) {
        if (!config.containsKey("view-condition") || config.get("view-condition").equals("")) return true;
        return TABAdditions.getInstance().isConditionMet(config.get("view-condition")+"",sender,viewer,viewer,feature);

    }

    public boolean hasRelationalPlaceholders() {
        if (!config.containsKey("relational")) return false;
        return (boolean) config.get("relational");
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
                    text = IChatBaseComponent.optimizedComponent(complist.get("text")+"");

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
                else if (complist.containsKey("command"))
                    text.onClickRunCommand(complist.get("command")+"");
                else if (complist.containsKey("url"))
                    text.onClickOpenUrl(complist.get("url")+"");

                list.add(text);
            }
        finalText.setExtra(list);
        return finalText;
    }


}
