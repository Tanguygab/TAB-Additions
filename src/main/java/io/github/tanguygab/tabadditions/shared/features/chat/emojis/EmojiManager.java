package io.github.tanguygab.tabadditions.shared.features.chat.emojis;

import io.github.tanguygab.tabadditions.shared.features.chat.Chat;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatManager;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatUtils;
import lombok.Getter;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.shared.platform.TabPlayer;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class EmojiManager extends ChatManager {

    private final String output;
    private final boolean untranslate;
    @Getter private final boolean autoCompleteEnabled;
    @Getter private final Map<String,EmojiCategory> emojiCategories = new HashMap<>();
    @Getter private int totalEmojiCount;
    private final Map<TabPlayer, List<String>> emojisAutoCompleteList = new HashMap<>();
    @Getter private final boolean emojisCmdEnabled;

    public EmojiManager(Chat chat, String emojiOutput, boolean untranslateEmojis, boolean autoCompleteEmojis, Map<String,Map<String,Object>> emojis, boolean emojisCmdEnabled, boolean toggleCmd) {
        super(chat,toggleCmd,"emojis-off","toggleemojis","chat-emojis");
        this.output = emojiOutput;
        this.untranslate = untranslateEmojis;
        autoCompleteEnabled = plugin.getPlatform().supportsChatSuggestions() && autoCompleteEmojis;
        for (String category : emojis.keySet()) {
            Map<String,String> emojisMap = (Map<String,String>) emojis.get(category).get("list");
            totalEmojiCount+=emojisMap.size();
            emojiCategories.put(category,new EmojiCategory(category, emojisMap,emojis.get(category).getOrDefault("output","")+""));
        }

        this.emojisCmdEnabled = emojisCmdEnabled;
        if (emojisCmdEnabled) plugin.getPlatform().registerCommand("emojis");

        for (TabPlayer p : tab.getOnlinePlayers())
            if (autoCompleteEmojis && !hasCmdToggled(p))
                loadAutoComplete(p);

        PlaceholderManager pm = tab.getPlaceholderManager();
        pm.registerServerPlaceholder("%chat-emoji-total%",-1, ()-> totalEmojiCount +"");
        pm.registerPlayerPlaceholder("%chat-emoji-owned%",5000,p->ownedEmojis((TabPlayer) p)+"");
    }

    @Override
    public void unload() {
        if (autoCompleteEnabled) for (TabPlayer p : tab.getOnlinePlayers()) unloadAutoComplete(p);
        super.unload();
    }

    public String process(TabPlayer sender, TabPlayer viewer, String msg) {
        if (hasCmdToggled(viewer) || hasCmdToggled(sender)) return msg;

        for (EmojiCategory category : emojiCategories.values()) {
            if (!category.canUse(sender)) continue;
            Map<String, String> emojis = category.getEmojis();
            if (emojis == null || emojis.isEmpty()) continue;

            for (String emoji : emojis.keySet()) {
                int count = ChatUtils.countMatches(msg, emoji);
                if (count == 0 || emoji.equals("")) continue;
                if (!category.canUse(sender,emoji)) {
                    if (untranslate && msg.contains(emojis.get(emoji)))
                        msg = msg.replace(emojis.get(emoji), emoji);
                    continue;
                }
                List<String> list = Arrays.asList(msg.split(Pattern.quote(emoji)));
                msg = "";
                int counted = 0;
                String output = plugin.parsePlaceholders(getOutput(category)
                        .replace("%emojiraw%",emoji)
                        .replace("%emoji%",emojis.get(emoji)),
                        sender,viewer);
                if (list.isEmpty()) return output.repeat(count);
                StringBuilder msgBuilder = new StringBuilder(msg);
                for (String part : list) {
                    if (list.indexOf(part)+1 != list.size() || counted != count) {
                        msgBuilder.append(part).append(output);
                        counted++;
                    } else msgBuilder.append(part);
                }
                msg = msgBuilder.toString();
            }
        }
        return msg;
    }

    public String getOutput(EmojiCategory category) {
        return category == null || category.getOutput().equals("") ? output : category.getOutput();
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
        plugin.getPlatform().updateChatComplete(p,list,true);
    }
    public void unloadAutoComplete(TabPlayer p) {
        if (emojisAutoCompleteList.containsKey(p))
            plugin.getPlatform().updateChatComplete(p, emojisAutoCompleteList.get(p),false);
    }

    public boolean onCommand(TabPlayer sender, String command) {
        if (command.startsWith("/emojis") && emojisCmdEnabled) {
            String cat = command.contains(" ") ? command.split(" ")[1] : "";
            if (cat.equals("")) {
                getEmojisCategories(sender);
                return true;
            }

            EmojiCategory category = emojiCategories.get(cat);
            if (category == null || !category.canUse(sender)) {
                sender.sendMessage(translation.emojiCategoryNotFound,true);
                return true;
            }
            getEmojiCategory(sender,category);
            return true;
        }
        return command.equals("/toggleemojis") && plugin.toggleCmd(toggleCmd,sender,toggled,translation.emojisOn,translation.emojisOff);
    }

    private void getEmojisCategories(TabPlayer p) {
        StringBuilder builder = new StringBuilder();
        AtomicInteger i = new AtomicInteger();
        AtomicInteger emojisOwned = new AtomicInteger();
        emojiCategories.forEach(((categoryName, category) -> {
            if (!category.canUse(p)) return;
            int owned = category.ownedEmojis(p);
            if (owned == 0) return;
            emojisOwned.addAndGet(owned);
            builder.append("\n <click:run_command:\"/emojis ").append(categoryName).append("\">").append(translation.getEmojiCategory(p, category)).append("</click>");
            i.getAndIncrement();
        }));

        String output = plugin.parsePlaceholders(translation.getEmojiCategoryHeader(i.get(),p,emojisOwned.get(),emojiCategories.size())+builder,p);
        plugin.getPlatform().getAudience(p).sendMessage(chat.mm.deserialize(ChatUtils.toMMColors(output)));
    }

    private void getEmojiCategory(TabPlayer p, EmojiCategory category) {
        StringBuilder builder = new StringBuilder();
        Map<String,String> emojis = category.getEmojis();
        AtomicInteger i = new AtomicInteger(0);
        emojis.forEach((emoji,output)->{
            if (!category.canUse(p,emoji)) return;
            builder.append("\n <click:suggest_command:\"").append(emoji).append("\">").append(translation.getEmoji(emoji, output)).append("</click>");
            i.getAndIncrement();
        });

        if (i.get() == 0) {
            p.sendMessage(translation.emojiCategoryNotFound, true);
            return;
        }
        String output = plugin.parsePlaceholders(translation.getEmojiHeader(i.get(),emojis.size())+builder,p);
        plugin.getPlatform().getAudience(p).sendMessage(chat.mm.deserialize(ChatUtils.toMMColors(output)));
    }

}