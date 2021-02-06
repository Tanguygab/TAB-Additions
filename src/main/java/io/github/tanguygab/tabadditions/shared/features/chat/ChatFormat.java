package io.github.tanguygab.tabadditions.shared.features.chat;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.packets.IChatBaseComponent;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
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
        Object cond = config.get("condition");
        if (cond == null) return true;
        String condition = TABAdditions.getInstance().parsePlaceholders(cond.toString(),p);

        Map<String,Boolean> conditions = ChatManager.getInstance().conditions;
        boolean value;
        if (conditions.containsKey(condition))
            return conditions.get(condition);
        try {
            value = Boolean.parseBoolean(String.valueOf(new ScriptEngineManager().getEngineByName("javascript").eval(condition)));
            ChatManager.getInstance().conditions.put(condition,value);
            return value;
        } catch (ScriptException e) {
            return false;
        }
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
