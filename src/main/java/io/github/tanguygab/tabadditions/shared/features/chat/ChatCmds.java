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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;

public class ChatCmds {

    private final TabAPI tab;
    private final ChatManager cm;

    public boolean msgEnabled;
    public List<String> msgAliases;
    public String msgSender;
    public String msgViewer;
    public boolean msgSelf;
    public boolean ignoreEnabled;
    public boolean toggleMsgEnabled;
    public boolean toggleMentionEnabled;
    public boolean replyEnabled;
    public boolean lastSenderReply;
    public Map<TabPlayer, String> replies = new HashMap<>();
    public long msgCooldownTime;
    public Map<TabPlayer,LocalDateTime> msgCooldown = new HashMap<>();

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
        msgEnabled = config.getBoolean("msg.enabled",true);
        msgSender = config.getString("msg.sender","{&7[&6&lMe &e➠ &6&l%viewer:prop-customchatname%&7] %msg%||%time%\\n\\n&fClick to reply to &6%viewer:prop-customchatname%&f.||suggest:/msg %player% }");
        msgViewer = config.getString("msg.viewer","{&7[&6&l%prop-customchatname% &e➠ &6&lMe&7] %msg%||%time%\\n\\n&fClick to reply to &6%prop-customchatname%&f.||suggest:/msg %player% }");
        msgSelf = config.getBoolean("msg.msg-self",true);
        ignoreEnabled = config.getBoolean("/ignore",true);
        toggleChatEnabled = config.getBoolean("/togglechat",true);
        toggleMsgEnabled = config.getBoolean("msg./togglemsg",true);
        replyEnabled = config.getBoolean("msg./reply",true);
        lastSenderReply = config.getBoolean("msg.save-last-sender-for-reply",true);
        msgAliases = config.getStringList("msg./msg-aliases",Arrays.asList("tell","whisper","w","m"));
        toggleMentionEnabled = config.getBoolean("mention./togglemention",true);
        msgCooldownTime = Long.parseLong(config.getInt("msg.cooldown",0)+"");

        socialSpyEnabled = config.getBoolean("socialspy.enabled",true);
        spyMsgsEnabled = config.getBoolean("socialspy.msgs.spy",true);
        spyMsgsOutput = config.getString("socialspy.msgs.output","{SocialSpy-Msg: [6&l%prop-customchatname% &e➠ 6&l%viewer:prop-customchatname%] %msg%||%time%}");

        clearChatEnabled = config.getBoolean("clearchat.enabled",true);
        clearChatAmount = config.getInt("clearchat.amount",100);
        clearChatLine = config.getString("clearchat.line","");

        PlaceholderManager pm = tab.getPlaceholderManager();
        pm.registerPlayerPlaceholder("%chat-mentions%",1000,p->cm.mentionDisabled.contains(p.getName().toLowerCase()) ? "Off" : "On");
        pm.registerPlayerPlaceholder("%chat-socialspy%",1000,p->cm.spies.contains(p.getName().toLowerCase()) ? "On" : "Off");
        pm.registerPlayerPlaceholder("%chat-messages%",1000,p->tab.getPlayerCache().getStringList("togglemsg").contains(p.getName().toLowerCase()) ? "Off" : "On");
        pm.registerPlayerPlaceholder("%chat-emojis%",1000,p->cm.toggleEmoji.contains(p.getName().toLowerCase()) ? "Off" : "On");
        pm.registerPlayerPlaceholder("%chat-enabled%",1000,p->cm.toggleChat.contains(p.getName().toLowerCase()) ? "Off" : "On");

        Platform p = TABAdditions.getInstance().getPlatform();
        p.registerCommand("msg",msgEnabled,msgAliases.toArray(new String[]{}));
        p.registerCommand("reply",replyEnabled,"r");
        p.registerCommand("togglechat",toggleChatEnabled);
        p.registerCommand("ignore",ignoreEnabled);
        p.registerCommand("togglemsg", toggleMsgEnabled);
        p.registerCommand("togglemention", toggleMentionEnabled);
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
        if (msgAliases.contains(cmd)) cmd = "msg";

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
            case "togglemention": return toggleCmd(toggleMentionEnabled,p,cm.mentionDisabled,msgs.mentionOn,msgs.mentionOff);
            case "toggleemoji": return toggleCmd(cm.getEmojiManager() != null && cm.getEmojiManager().isToggleEmojiCmdEnabled(),p,cm.toggleEmoji,msgs.emojiOn,msgs.emojiOff);
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
                List<String> list = new ArrayList<>(playerdata.getStringList(p.getName().toLowerCase()));

                if (list.contains(p2.toLowerCase())) {
                    list.remove(p2.toLowerCase());
                    p.sendMessage(msgs.getIgnoreOff(p2), true);
                    return true;
                }
                list.add(p2.toLowerCase());
                p.sendMessage(msgs.getIgnoreOn(p2), true);

                playerdata.set("msg-ignore."+p.getName().toLowerCase(), list);
                return true;
            }
        }

        if (!msgEnabled) return false;
        switch (cmd.toLowerCase()) {
            case "togglemsg": {
                if (!toggleMsgEnabled) return false;
                List<String> list = playerdata.getStringList("togglemsg");
                toggleCmd(true,p,list,msgs.pmOn,msgs.pmOff);
                playerdata.set("togglemsg", list);
                return true;
            }
            case "reply": if (!replyEnabled) return false;
            case "msg": {
                if (plugin.isMuted(p)) return true;
                if (msgCooldown.containsKey(p)) {
                    long time = ChronoUnit.SECONDS.between(msgCooldown.get(p), LocalDateTime.now());
                    if (time < msgCooldownTime) {
                        p.sendMessage(msgs.getPmCooldown(msgCooldownTime-time), true);
                        return true;
                    }
                    msgCooldown.remove(p);
                }
                if (msgCooldownTime != 0 && !p.hasPermission("tabadditions.chat.bypass.cooldown"))
                    msgCooldown.put(p,LocalDateTime.now());

                String player;
                if (cmd.equals("reply")) player = replies.getOrDefault(p, "");
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
                else if (!msgSelf && p == p2) p.sendMessage(msgs.cantPmSelf, true);
                else if (!p.hasPermission("tabadditions.chat.bypass.togglemsg") && playerdata.getStringList("togglemsg", new ArrayList<>()).contains(p2.getName().toLowerCase()))
                    p.sendMessage(msgs.hasPmOff, true);
                else if (isIgnored(p,p2)) p.sendMessage(msgs.isIgnored, true);
                else if (msg.equals("") || msg.equals(" ")) p.sendMessage(msgs.pmEmpty, true);
                else {
                    p.sendMessage(createmsg(p, msg, msgSender, p2));
                    p2.sendMessage(createmsg(p, msg, msgViewer, p2));
                    replies.put(p, player);
                    if (lastSenderReply) replies.put(p2, p.getName());
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
            IChatBaseComponent subcomp = cm.createComponent("\n"+EnumChatFormat.color(translation.getEmojiCategory(p,category)),p);
            subcomp.getModifier().onClickRunCommand("/emojis "+categoryName);
            list.add(subcomp);

        }));
        IChatBaseComponent comp = cm.createComponent("\n"+EnumChatFormat.color(translation.getEmojiCategoryHeader(list.size(),p,emojis)),p);
        if (!list.isEmpty()) comp.setExtra(list);
        p.sendMessage(comp);
    }

    public void getEmojiCategory(TabPlayer p, EmojiCategory category) {
        List<IChatBaseComponent> list = new ArrayList<>();
        TranslationFile translation = TABAdditions.getInstance().getMsgs();

        Map<String,String> emojis = category.getEmojis();
        emojis.forEach((emoji,output)->{
            if (!category.canUse(p,emoji)) return;
            IChatBaseComponent comp = cm.createComponent("\n" + TABAdditions.getInstance().parsePlaceholders(translation.getEmoji(emoji,output),p),p);
            comp.getModifier().onClickSuggestCommand(emoji);
            list.add(comp);
        });

        if (list.isEmpty()) {
            p.sendMessage(translation.emojiCategoryNotFound, true);
            return;
        }
        IChatBaseComponent comp = cm.createComponent(EnumChatFormat.color(translation.getEmojiHeader(list.size(),category.getEmojis().size())),p);
        comp.setExtra(list);
        p.sendMessage(comp);

    }

    public boolean isIgnored(TabPlayer p, TabPlayer viewer) {
        return tab.getPlayerCache().getStringList("msg-ignore." + viewer.getName().toLowerCase(), new ArrayList<>()).contains(p.getName().toLowerCase()) && !p.hasPermission("tabadditions.chat.bypass.ignore");
    }
}
