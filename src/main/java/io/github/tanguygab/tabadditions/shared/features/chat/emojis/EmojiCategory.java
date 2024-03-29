package io.github.tanguygab.tabadditions.shared.features.chat.emojis;


import lombok.AllArgsConstructor;
import lombok.Getter;
import me.neznamy.tab.shared.platform.TabPlayer;
import java.util.Map;

@Getter
@AllArgsConstructor
public class EmojiCategory {

    private final String name;
    private final Map<String,String> emojis;
    private final String output;

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