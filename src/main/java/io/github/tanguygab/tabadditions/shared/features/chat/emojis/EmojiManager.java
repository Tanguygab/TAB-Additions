package io.github.tanguygab.tabadditions.shared.features.chat.emojis;


import io.github.tanguygab.tabadditions.shared.Platform;
import io.github.tanguygab.tabadditions.shared.features.chat.Chat;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatManager;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatUtils;
import lombok.Getter;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.shared.platform.TabPlayer;

import java.util.*;
import java.util.regex.Pattern;

public class EmojiManager extends ChatManager {

    private final String output;
    private final boolean untranslate;
    @Getter private final boolean autoCompleteEnabled;
    @Getter private final Map<String,EmojiCategory> emojiCategories = new HashMap<>();
    @Getter private int totalEmojiCount;
    private final Map<TabPlayer, List<String>> emojisAutoCompleteList = new HashMap<>();
    @Getter private final boolean emojisCmdEnabled;
    @Getter private final boolean toggleCmd;
    public final List<UUID> toggled;

    public EmojiManager(Chat cm, String emojiOutput, boolean untranslateEmojis, boolean autoCompleteEmojis, Map<String,Map<String,Object>> emojis, boolean emojisCmdEnabled, boolean toggleCmd) {
        super(cm);
        this.output = emojiOutput;
        this.untranslate = untranslateEmojis;
        autoCompleteEnabled = autoCompleteEmojis;
        for (String category : emojis.keySet()) {
            Map<String,String> emojisMap = (Map<String,String>) emojis.get(category).get("list");
            totalEmojiCount+=emojisMap.size();
            emojiCategories.put(category,new EmojiCategory(category, emojisMap,emojis.get(category).getOrDefault("output","")+""));
        }
        PlaceholderManager pm = tab.getPlaceholderManager();
        Platform platform = plugin.getPlatform();

        this.emojisCmdEnabled = emojisCmdEnabled;
        //if (emojisCmdEnabled) platform.registerCommand("emojis",true);

        this.toggleCmd = toggleCmd;
        toggled = plugin.loadData("emojis-off",toggleCmd);
        if (toggleCmd) {
            //platform.registerCommand("toggleemojis", true);
            pm.registerPlayerPlaceholder("%chat-emojis%",1000,p->hasEmojisToggled((TabPlayer) p) ? "Off" : "On");
        }

        pm.registerServerPlaceholder("%chat-emoji-total%",-1, ()-> totalEmojiCount +"");
        pm.registerPlayerPlaceholder("%chat-emoji-owned%",5000,p->ownedEmojis((TabPlayer) p)+"");
    }

    public void unload() {
        if (autoCompleteEnabled) for (TabPlayer p : tab.getOnlinePlayers()) unloadAutoComplete(p);
        plugin.unloadData("emojis-off",toggled,toggleCmd);
    }
/*
    public String process(TabPlayer p, String msg, String hoverclick) {
        if (hasEmojisToggled(p)) return msg;

        for (EmojiCategory category : emojiCategories.values()) {
            if (!category.canUse(p)) continue;
            Map<String, String> list = category.getEmojis();
            if (list == null || list.isEmpty()) continue;

            for (String emoji : list.keySet()) {
                int count = ChatUtils.countMatches(msg, emoji);
                if (count == 0 || emoji.equals("")) continue;
                if (!category.canUse(p,emoji)) {
                    if (untranslate && msg.contains(list.get(emoji)))
                        msg = msg.replace(list.get(emoji), emoji);
                    continue;
                }
                List<String> list2 = Arrays.asList(msg.split(Pattern.quote(emoji)));
                msg = "";
                int counted = 0;
                String output1 = getOutput(category);
                output1 = output1.replace("%emojiraw%", emoji).replace("%emoji%", list.get(emoji));
                String output = hoverclick + plugin.parsePlaceholders(cm.removeSpaces(output1),p) + "{";
                if (list2.isEmpty()) {
                    for (int i = 0; i < count; i++) msg+=output;
                    return msg;
                }
                for (String part : list2) {
                    if (list2.indexOf(part) + 1 == list2.size() && counted == count)
                        msg += part;
                    else {
                        msg += part + output;
                        counted++;
                    }
                }
            }
        }
        return msg;
    }
*/
    public String getOutput(EmojiCategory category) {
        return category == null || category.getOutput().equals("") ? output : category.getOutput();
    }

    public EmojiCategory getEmojiCategory(String category) {
        return emojiCategories.get(category);
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
        //plugin.getPlatform().addToChatComplete(p,list);
    }
    public void unloadAutoComplete(TabPlayer p) {
        if (emojisAutoCompleteList.containsKey(p));
            //plugin.getPlatform().removeFromChatComplete(p, emojisAutoCompleteList.get(p));
    }

    public boolean hasEmojisToggled(TabPlayer p) {
        return toggled.contains(p.getName().toLowerCase());
    }
}