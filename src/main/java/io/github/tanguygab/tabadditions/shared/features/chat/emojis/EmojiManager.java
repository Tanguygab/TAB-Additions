package io.github.tanguygab.tabadditions.shared.features.chat.emojis;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.PlaceholderManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmojiManager {

    private final String output;
    private final boolean untranslate;
    private final boolean autoComplete;
    private final Map<String,EmojiCategory> emojiCategories = new HashMap<>();
    private int totalEmojiCount;
    private final Map<TabPlayer, List<String>> emojisAutoCompleteList = new HashMap<>();

    public EmojiManager(String emojiOutput, boolean untranslateEmojis, boolean autoCompleteEmojis, Map<String,Map<String,Object>> emojis) {
        this.output = emojiOutput;
        this.untranslate = untranslateEmojis;
        this.autoComplete = autoCompleteEmojis;
        for (String category : emojis.keySet()) {
            Map<String,String> emojisMap = (Map<String,String>) emojis.get(category).get("list");
            totalEmojiCount+=emojisMap.size();
            emojiCategories.put(category,new EmojiCategory(category, emojisMap,emojis.get(category).getOrDefault("output","")+""));
        }
        PlaceholderManager pm = TabAPI.getInstance().getPlaceholderManager();
        pm.registerServerPlaceholder("%chat-emoji-total%",-1, ()-> totalEmojiCount +"");
        pm.registerPlayerPlaceholder("%chat-emoji-owned%",5000,p->ownedEmojis(p)+"");
    }

    public void unload() {
        for (TabPlayer p : TabAPI.getInstance().getOnlinePlayers())
            unloadAutoComplete(p);
    }

    public String getOutput(EmojiCategory category) {
        return category == null || category.getOutput().equals("") ? output : category.getOutput();
    }

    public boolean isAutoCompleteEnabled() {
        return autoComplete;
    }

    public boolean isUntranslateEnabled() {
        return untranslate;
    }

    public EmojiCategory getEmojiCategory(String category) {
        return emojiCategories.get(category);
    }
    public Map<String, EmojiCategory> getEmojiCategories() {
        return emojiCategories;
    }

    public int getTotalEmojiCount() {
        return totalEmojiCount;
    }

    public int ownedEmojis(TabPlayer p) {
        int amt=0;
        for (EmojiCategory category : emojiCategories.values())
            amt+=category.ownedEmojis(p);
        return amt;
    }

    public void loadAutoComplete(TabPlayer p) {
        List<String> list = new ArrayList<>();
        for (EmojiCategory category : emojiCategories.values()) {
            if (category.canUse(p)) {
                list.addAll(category.getEmojis().keySet());
                continue;
            }
            for (String emoji : category.getEmojis().keySet())
                if (category.canUse(p,emoji))
                    list.add(emoji);
        }
        emojisAutoCompleteList.put(p,list);
        TABAdditions.getInstance().getPlatform().addToChatComplete(p,list);
    }
    public void unloadAutoComplete(TabPlayer p) {
        if (emojisAutoCompleteList.containsKey(p))
            TABAdditions.getInstance().getPlatform().removeFromChatComplete(p, emojisAutoCompleteList.get(p));
    }
}
