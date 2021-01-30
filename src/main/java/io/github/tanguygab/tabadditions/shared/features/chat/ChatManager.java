package io.github.tanguygab.tabadditions.shared.features.chat;

import io.github.tanguygab.tabadditions.shared.SharedTA;
import me.neznamy.tab.api.TabPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ChatManager {

    private static ChatManager instance;
    private final Map<String,ChatFormat> formats = new HashMap<>();
    public final Map<String,Boolean> conditions = new HashMap<>();

    public ChatManager() {
        instance = this;
        for (Object format : SharedTA.chatConfig.getConfigurationSection("chat-formats").keySet())
            formats.put(format.toString(),new ChatFormat(format.toString(),SharedTA.chatConfig.getConfigurationSection("chat-formats."+format)));
    }

    public static ChatManager getInstance() {
        return instance;
    }
    public ChatFormat getFormat(TabPlayer p) {
        String format = SharedTA.chatConfig.getString("default-format","default");
        if (format.equalsIgnoreCase("")) return defFormat();

        ChatFormat f = formats.get(format);
        while (f != null && !f.isConditionMet(p)) {
            f = formats.get(f.getChildLayout());

            if (f == null)
                return defFormat();
        }
        return f;
    }
    public boolean isInRange(TabPlayer sender,TabPlayer viewer,String range) {
        if (SharedTA.platform.type().equals("Bungee")) return true;
        int zone = (int) Math.pow(Integer.parseInt(range), 2);
        return sender.getWorldName().equals(viewer.getWorldName()) && ((Player) sender.getPlayer()).getLocation().distanceSquared(((Player) viewer.getPlayer()).getLocation()) < zone;
    }

    public ChatFormat defFormat() {
        Map<String,Object> map = new HashMap<>();
        Map<String,Object> components = new HashMap<>();
        Map<String,Object> text = new HashMap<>();

        text.put("text","%tab_chatprefix% %tab_customchatname% %tab_chatsuffix%é&7>> &r%msg%");
        components.put("text",text);
        map.put("components",components);
        return new ChatFormat("default", map);
    }
}