package io.github.tanguygab.tabadditions.shared.features.chat;

import io.github.tanguygab.tabadditions.shared.Platform;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.TranslationFile;
import io.github.tanguygab.tabadditions.shared.features.chat.emojis.EmojiCategory;
import io.github.tanguygab.tabadditions.shared.features.chat.emojis.EmojiManager;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.config.ConfigurationFile;

import java.util.*;
import java.util.regex.Pattern;

public class ChatCmds {

    private final TabAPI tab;
    private final ChatManager cm;

    public boolean ignoreEnabled;
    public Map<String,List<String>> ignored;

    public boolean toggleChatEnabled;

    public boolean socialSpyEnabled;
    public boolean spyMsgsEnabled;
    public String spyMsgsOutput;

    public boolean clearChatEnabled;
    public String clearChatLine;
    public int clearChatAmount;

    public ChatCmds(ChatManager manager, ConfigurationFile config) {
        tab = TabAPI.getInstance();
        cm = manager;

        ignoreEnabled = config.getBoolean("/ignore",true);
        ignored = tab.getPlayerCache().getConfigurationSection("msg-ignore");

        toggleChatEnabled = config.getBoolean("/togglechat",true);

        socialSpyEnabled = config.getBoolean("socialspy.enabled",true);
        spyMsgsEnabled = config.getBoolean("socialspy.msgs.spy",true);
        spyMsgsOutput = config.getString("socialspy.msgs.output","{SocialSpy-Msg: [6&l%prop-customchatname% &e➠ 6&l%viewer:prop-customchatname%] %msg%||%time%}");

        clearChatEnabled = config.getBoolean("clearchat.enabled",true);
        clearChatAmount = config.getInt("clearchat.amount",100);
        clearChatLine = config.getString("clearchat.line","");

        PlaceholderManager pm = tab.getPlaceholderManager();
        pm.registerPlayerPlaceholder("%chat-socialspy%",1000,p->cm.spies.contains(p.getName().toLowerCase()) ? "On" : "Off");
        pm.registerPlayerPlaceholder("%chat-enabled%",1000,p->cm.toggleChat.contains(p.getName().toLowerCase()) ? "Off" : "On");

        Platform p = TABAdditions.getInstance().getPlatform();
        p.registerCommand("togglechat",toggleChatEnabled);
        p.registerCommand("ignore",ignoreEnabled);
        p.registerCommand("socialspy", socialSpyEnabled);
        p.registerCommand("clearchat", clearChatEnabled);
    }

    public IChatBaseComponent createmsg(TabPlayer p, String msg, String chatformat, TabPlayer viewer) {
        return cm.createmsg(p,msg,chatformat,viewer);
    }

    public boolean execute(TabPlayer p, String msg) {
        TABAdditions plugin = TABAdditions.getInstance();
        String cmd = msg.split(" ")[0];
        msg = msg.contains(" ") ? msg.substring(cmd.length()+1) : "";
        if (cmd.equals("r")) cmd = "reply";
        if (cm.getMsgManager() != null && cm.getMsgManager().isAlias(cmd)) cmd = "msg";

        ConfigurationFile playerdata = tab.getPlayerCache();
        TranslationFile msgs = plugin.getMsgs();

        switch (cmd) {
            case "emojis": {
                EmojiManager emojis = cm.getEmojiManager();
                if (emojis == null || emojis.isEmojisCmdEnabled()) return false;

                if (msg.equals("")) {
                    getEmojisCategories(p);
                    return false;
                }

                String cat = msg.split(" ")[0];
                EmojiCategory category = emojis.getEmojiCategory(cat);
                if (category == null || !category.canUse(p)) {
                    p.sendMessage(msgs.emojiCategoryNotFound,true);
                    return true;
                }
                getEmojiCategory(p,category);
                return true;
            }
            case "clearchat": {
                if (!clearChatEnabled || !p.hasPermission("tabadditions.chat.clearchat")) return false;

                String linebreaks = "";
                for (int i = 0; i < clearChatAmount; i++)
                    linebreaks+="\n"+clearChatLine;
                for (TabPlayer all : tab.getOnlinePlayers())
                    all.sendMessage(linebreaks,false);
                p.sendMessage(msgs.getChatCleared(p),true);
                return true;
            }
            case "socialspy": return toggleCmd(!socialSpyEnabled || !p.hasPermission("tabadditions.chat.socialspy"),p,cm.spies,msgs.socialSpyOff,msgs.socialSpyOn);
            case "togglemention": return toggleCmd(cm.getMentionManager() != null && cm.getMentionManager().isToggleMentionCmd(),p,cm.getMentionManager().toggleMention,msgs.mentionOn,msgs.mentionOff);
            case "toggleemoji": return toggleCmd(cm.getEmojiManager() != null && cm.getEmojiManager().isToggleEmojiCmdEnabled(),p,cm.getEmojiManager().toggleEmoji,msgs.emojiOn,msgs.emojiOff);
            case "togglechat": return toggleCmd(toggleChatEnabled,p,cm.toggleChat,msgs.chatOn,msgs.chatOff);
            case "ignore": {
                if (!ignoreEnabled) return false;
                if (msg.equals("")) {
                    p.sendMessage(msgs.providePlayer, true);
                    return true;
                }
                String p2 = msg.split(" ")[0];
                if (p.getName().equalsIgnoreCase(p2)) {
                    p.sendMessage(msgs.cantIgnoreSelf,true);
                    return true;
                }
                List<String> ignoredPlayers = ignored.computeIfAbsent(p.getName().toLowerCase(), k -> new ArrayList<>());
                if (ignoredPlayers.contains(p2.toLowerCase())) {
                    ignoredPlayers.remove(p2.toLowerCase());
                    p.sendMessage(msgs.getIgnoreOff(p2), true);
                    return true;
                }
                ignoredPlayers.add(p2.toLowerCase());
                p.sendMessage(msgs.getIgnoreOn(p2), true);
                return true;
            }
        }

        MsgManager mm = cm.getMsgManager();
        if (mm == null) return false;
        switch (cmd.toLowerCase()) {
            case "togglemsg": {
                if (!mm.isToggleMsgCmdEnabled()) return false;
                List<String> list = playerdata.getStringList("togglemsg");
                toggleCmd(true,p,list,msgs.pmOn,msgs.pmOff);
                playerdata.set("togglemsg", list);
                return true;
            }
            case "reply": if (!mm.isReplyCmdEnabled()) return false;
            case "msg": {
                if (plugin.isMuted(p)) return true;
                if (mm.isOnCooldown(p)) {
                    p.sendMessage(msgs.getPmCooldown(mm.getCooldown(p)), true);
                    return true;
                }
                mm.setCooldown(p);

                String player;
                if (cmd.equals("reply")) player = mm.getLastReply(p);
                else {
                    player = msg.split(" ")[0];
                    msg = msg.replaceFirst(Pattern.quote(player)+"( )?", "");
                }
                if (player.equals("")) {
                    p.sendMessage(msgs.providePlayer,true);
                    return true;
                }
                TabPlayer p2 = plugin.getPlayer(player);
                if (p2 == null) p.sendMessage(msgs.getPlayerNotFound(player), true);
                else if (!mm.canMsgSelf() && p == p2) p.sendMessage(msgs.cantPmSelf, true);
                else if (!p.hasPermission("tabadditions.chat.bypass.togglemsg") && playerdata.getStringList("togglemsg", new ArrayList<>()).contains(p2.getName().toLowerCase()))
                    p.sendMessage(msgs.hasPmOff, true);
                else if (isIgnored(p,p2)) p.sendMessage(msgs.isIgnored, true);
                else if (msg.equals("") || msg.equals(" ")) p.sendMessage(msgs.pmEmpty, true);
                else {
                    p.sendMessage(createmsg(p, msg, mm.getSenderOutput(), p2));
                    p2.sendMessage(createmsg(p, msg, mm.getViewerOutput(), p2));
                    mm.setLastReply(p, player);
                    if (mm.saveLastSenderForReply()) mm.setLastReply(p2, p.getName());
                    if (spyMsgsEnabled) {
                        List<String> list = new ArrayList<>(cm.spies);
                        for (String spy : list) {
                            TabPlayer tabspy = plugin.getPlayer(spy);
                            if (tabspy != null)
                                tabspy.sendMessage(createmsg(p, msg, spyMsgsOutput, p2));
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    private boolean toggleCmd(boolean isEnabled, TabPlayer p, List<String> list, String msgOn, String msgOff) {
        if (!isEnabled) return false;

        if (list.contains(p.getName().toLowerCase())) {
            list.remove(p.getName().toLowerCase());
            p.sendMessage(msgOn, true);
            return true;
        }
        list.add(p.getName().toLowerCase());
        p.sendMessage(msgOff, true);
        return true;
    }

    public void getEmojisCategories(TabPlayer p) {
        List<IChatBaseComponent> list = new ArrayList<>();
        TranslationFile translation = TABAdditions.getInstance().getMsgs();
        EmojiManager emojis = cm.getEmojiManager();

        emojis.getEmojiCategories().forEach(((categoryName, category) -> {
            if (!category.canUse(p)) return;
            int owned = category.ownedEmojis(p);
            if (owned == 0) return;
            IChatBaseComponent subcomp = cm.createComponent("\n"+EnumChatFormat.color(translation.getEmojiCategory(p,category)));
            subcomp.getModifier().onClickRunCommand("/emojis "+categoryName);
            list.add(subcomp);

        }));
        IChatBaseComponent comp = cm.createComponent("\n"+EnumChatFormat.color(translation.getEmojiCategoryHeader(list.size(),p,emojis)));
        if (!list.isEmpty()) comp.setExtra(list);
        p.sendMessage(comp);
    }

    public void getEmojiCategory(TabPlayer p, EmojiCategory category) {
        List<IChatBaseComponent> list = new ArrayList<>();
        TranslationFile translation = TABAdditions.getInstance().getMsgs();

        Map<String,String> emojis = category.getEmojis();
        emojis.forEach((emoji,output)->{
            if (!category.canUse(p,emoji)) return;
            IChatBaseComponent comp = cm.createComponent("\n" + TABAdditions.getInstance().parsePlaceholders(translation.getEmoji(emoji,output),p));
            comp.getModifier().onClickSuggestCommand(emoji);
            list.add(comp);
        });

        if (list.isEmpty()) {
            p.sendMessage(translation.emojiCategoryNotFound, true);
            return;
        }
        IChatBaseComponent comp = cm.createComponent(EnumChatFormat.color(translation.getEmojiHeader(list.size(),category.getEmojis().size())));
        comp.setExtra(list);
        p.sendMessage(comp);

    }

    public boolean isIgnored(TabPlayer p, TabPlayer viewer) {
        return tab.getPlayerCache().getStringList("msg-ignore." + viewer.getName().toLowerCase(), new ArrayList<>()).contains(p.getName().toLowerCase()) && !p.hasPermission("tabadditions.chat.bypass.ignore");
    }
}
