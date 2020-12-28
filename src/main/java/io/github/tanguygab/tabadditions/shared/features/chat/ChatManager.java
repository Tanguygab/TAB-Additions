package io.github.tanguygab.tabadditions.shared.features.chat;

import io.github.tanguygab.tabadditions.shared.SharedTA;
import me.neznamy.tab.api.TabPlayer;

import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;

public class ChatManager {

    private static ChatManager instance;
    private final Map<String,ChatFormat> formats = new HashMap<>();

    public ChatManager() {
        instance = this;
        for (Object format : SharedTA.chatConfig.getConfigurationSection("chat-formats").keySet())
            formats.put(format.toString(),new ChatFormat(format.toString()));
    }

    public static ChatManager getInstance() {
        return instance;
    }
    public String getFormat(TabPlayer p) {
        String format = SharedTA.chatConfig.getString("default-format","default");
        if (format.equalsIgnoreCase("")) return "null";

        ChatFormat f = formats.get(format);
        while (f != null && !f.isConditionMet(p)) {
            f = formats.get(f.getChildLayout());
            if (f == null) return "null";
            format = f.getName();
        }
        return format;
    }
}
