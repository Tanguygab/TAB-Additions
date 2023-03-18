package io.github.tanguygab.tabadditions.shared.features.chat.emojis;

import me.neznamy.tab.api.TabPlayer;

import java.util.HashMap;
import java.util.Map;

public class EmojiCategory {

    private final String name;
    private final Map<String,String> emojis = new HashMap<>();
    private final String output;

    public EmojiCategory(String name, Map<String,String> emojis, String output) {
        this.name = name;
        this.emojis.putAll(emojis);
        this.output = output;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getEmojis() {
        return emojis;
    }

    public String getOutput() {
        return output;
    }

    public boolean canUse(TabPlayer p) {
        return p.hasPermission("tabadditions.chat.emoji.category."+name);
    }

    public boolean canUse(TabPlayer p, String emoji) {
        return canUse(p) || p.hasPermission("tabadditions.chat.emoji."+emoji);
    }

    public int ownedEmojis(TabPlayer p) {
        if (canUse(p)) return emojis.size();
        int i=0;
        for (String emoji : emojis.keySet())
            if (canUse(p,emoji))
                i++;
        return i;
    }
}
