package io.github.tanguygab.tabadditions.shared.features.chat;

import io.github.tanguygab.tabadditions.shared.SharedTA;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import org.bukkit.entity.Player;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Map;

public class ChatFormat {

    private final String name;
    private final Map<String,Object> config;

    public ChatFormat(String name) {
        this.name = name;
        config = SharedTA.chatConfig.getConfigurationSection("chat-formats."+name);
    }

    public String getName() {
        return name;
    }
    protected String getChildLayout() {
        return config.get("if-condition-not-met").toString();
    }
    protected boolean isConditionMet(TabPlayer p) {
        Object cond = config.get("condition");
        if (cond == null) return true;
        String condition = Shared.platform.replaceAllPlaceholders(cond.toString(),p);

        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        try {
            return (boolean) engine.eval(condition);
        } catch (ScriptException e) {
            return true;
        }
    }

    public boolean isInRange(TabPlayer sender,TabPlayer viewer,String range) {
        if (SharedTA.platform.type().equals("Bungee")) return true;
        int zone = (int) Math.pow(Integer.parseInt(range), 2);
        return sender.getWorldName().equals(viewer.getWorldName()) && ((Player) sender.getPlayer()).getLocation().distanceSquared(((Player) viewer.getPlayer()).getLocation()) < zone;
    }

}
