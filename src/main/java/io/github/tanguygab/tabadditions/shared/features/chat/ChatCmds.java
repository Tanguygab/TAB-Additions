package io.github.tanguygab.tabadditions.shared.features.chat;

import io.github.tanguygab.tabadditions.shared.Platform;
import io.github.tanguygab.tabadditions.shared.PlatformType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.TranslationFile;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.config.ConfigurationFile;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

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
    public Map<TabPlayer, TabPlayer> replies = new HashMap<>();
    public long msgCooldownTime;
    public Map<TabPlayer,LocalDateTime> msgCooldown = new HashMap<>();

    public boolean emojisEnabled;
    public boolean toggleEmojiEnabled;

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
        ignoreEnabled = config.getBoolean("msg./ignore",true);
        toggleMsgEnabled = config.getBoolean("msg./togglemsg",true);
        replyEnabled = config.getBoolean("msg./reply",true);
        msgAliases = config.getStringList("msg./msg-aliases",Arrays.asList("tell","whisper","w","m"));
        toggleMentionEnabled = config.getBoolean("mention./togglemention",true);
        msgCooldownTime = Long.parseLong(config.getInt("msg.cooldown",0)+"");

        emojisEnabled = config.getBoolean("emojis./emojis",true);
        toggleEmojiEnabled = config.getBoolean("emojis./toggleemoji",true);

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

        Platform p = TABAdditions.getInstance().getPlatform();
        p.registerCommand("msg",msgEnabled,msgAliases.toArray(new String[]{}));
        p.registerCommand("reply",replyEnabled,"r");
        p.registerCommand("ignore",ignoreEnabled);
        p.registerCommand("togglemsg", toggleMsgEnabled);
        p.registerCommand("togglemention", toggleMentionEnabled);
        p.registerCommand("emojis",emojisEnabled);
        p.registerCommand("socialspy", socialSpyEnabled);
        p.registerCommand("clearchat", clearChatEnabled);
        p.registerCommand("toggleemoji", toggleEmojiEnabled);
    }

    public IChatBaseComponent createmsg(TabPlayer p, String msg, String chatformat, TabPlayer viewer) {
        return cm.createmsg(p,msg,chatformat,viewer);
    }

    public boolean execute(TabPlayer p, String cmd) {
        TABAdditions plugin = TABAdditions.getInstance();
        String msg = cmd.contains(" ") ? cmd.replace(cmd.split(" ")[0]+" ","") : "";
        cmd = cmd.split(" ")[0];
        if (cmd.equals("r")) cmd = "reply";
        if (msgAliases.contains(cmd)) cmd = "msg";

        ConfigurationFile playerdata = tab.getPlayerCache();
        TranslationFile translation = plugin.getMsgs();

        switch (cmd) {
            case "emojis": {
                if (!cm.emojiEnabled) return false;

                if (msg.equals("")) {
                    getEmojisCategories(p);
                    return false;
                }

                String cat = msg.split(" ")[0];
                if (!canUseEmojiCategory(p,cat)) {
                    p.sendMessage(translation.emojiCategoryNotFound,true);
                    return true;
                }
                getEmojiCategory(p,cat);
                return true;
            }
            case "socialspy": {
                if (!socialSpyEnabled || !p.hasPermission("tabadditions.chat.socialspy")) return false;

                if (cm.spies.contains(p.getName().toLowerCase())) {
                    cm.spies.remove(p.getName().toLowerCase());
                    p.sendMessage(translation.socialSpyOff, true);
                }
                else {
                    cm.spies.add(p.getName().toLowerCase());
                    p.sendMessage(translation.socialSpyOn, true);
                }
                return true;
            }
            case "clearchat": {
                if (!clearChatEnabled || !p.hasPermission("tabadditions.chat.clearchat")) return false;

                String linebreaks = "";
                for (int i = 0; i < clearChatAmount; i++)
                    linebreaks+="\n"+clearChatLine;
                p.sendMessage(linebreaks,false);
                p.sendMessage(translation.getChatCleared(p),true);
                return true;
            }
            case "togglemention": {
                if (!toggleMentionEnabled) return false;

                if (cm.mentionDisabled.contains(p.getName().toLowerCase())) {
                    cm.mentionDisabled.remove(p.getName().toLowerCase());
                    p.sendMessage(translation.mentionOn, true);
                }
                else {
                    cm.mentionDisabled.add(p.getName().toLowerCase());
                    p.sendMessage(translation.mentionOff, true);
                }
                return true;
            }
            case "toggleemoji": {
                if (!toggleEmojiEnabled) return false;

                if (cm.toggleEmoji.contains(p.getName().toLowerCase())) {
                    cm.toggleEmoji.remove(p.getName().toLowerCase());
                    p.sendMessage(translation.emojiOn, true);
                }
                else {
                    cm.toggleEmoji.add(p.getName().toLowerCase());
                    p.sendMessage(translation.emojiOff, true);
                }
                return true;
            }
        }

        if (!msgEnabled) return false;
        switch (cmd.toLowerCase()) {
            case "togglemsg": {
                if (!toggleMsgEnabled) return false;
                List<String> list = playerdata.getStringList("togglemsg");
                if (list.contains(p.getName().toLowerCase())) {
                    list.remove(p.getName().toLowerCase());
                    p.sendMessage(translation.pmOn, true);
                } else {
                    list.add(p.getName().toLowerCase());
                    p.sendMessage(translation.pmOff, true);
                }
                playerdata.set("togglemsg", list);
                return true;
            }
            case "ignore": {
                if (!ignoreEnabled) return false;
                if (msg.equals("")) {
                    p.sendMessage(translation.providePlayer, true);
                    return true;
                }
                String p2 = msg.split(" ")[0];
                Map<String, List<String>> map = playerdata.getConfigurationSection("msg-ignore");
                if (map.containsKey(p.getName().toLowerCase())) {
                    if (map.get(p.getName().toLowerCase()).contains(p2.toLowerCase())) {
                        map.get(p.getName().toLowerCase()).remove(p2.toLowerCase());
                        p.sendMessage(translation.getIgnoreOff(p2), true);
                    } else {
                        map.get(p.getName().toLowerCase()).add(p2.toLowerCase());
                        p.sendMessage(translation.getIgnoreOn(p2), true);
                    }
                } else map.put(p.getName().toLowerCase(), new ArrayList<>(Collections.singletonList(p2.toLowerCase())));
                playerdata.set("msg-ignore", map);
                return true;
            }
            case "reply":
                if (!replyEnabled) return false;
            case "msg": {
                if (msgCooldown.containsKey(p)) {
                    long time = ChronoUnit.SECONDS.between(msgCooldown.get(p), LocalDateTime.now());
                    if (time < msgCooldownTime) {
                        p.sendMessage(translation.getPmCooldown(msgCooldownTime-time), true);
                        return true;
                    }
                    msgCooldown.remove(p);
                }
                if (msgCooldownTime != 0 && !p.hasPermission("tabadditions.chat.bypass.cooldown"))
                    msgCooldown.put(p,LocalDateTime.now());

                String player;
                TabPlayer p2;
                if (cmd.equals("reply")) {
                    p2 = replies.getOrDefault(p, null);
                    player = p2 != null ? p2.getName() : "";
                }
                else {
                    player = msg.split(" ")[0];
                    msg = msg.replaceFirst(player+"( )?", "");
                    p2 = plugin.getPlayer(player);
                }
                if (player.equals(""))
                    p.sendMessage(translation.providePlayer,true);
                else if (p2 == null)
                    p.sendMessage(translation.getPlayerNotFound(player), true);
                else if (!msgSelf && p == p2)
                    p.sendMessage(translation.cantPmSelf, true);
                else if (!p.hasPermission("tabadditions.chat.bypass.togglemsg") && playerdata.getStringList("togglemsg", new ArrayList<>()).contains(p2.getName().toLowerCase()))
                    p.sendMessage(translation.hasPmOff, true);
                else if (!p.hasPermission("tabadditions.chat.bypass.ignore") && isIgnored(p,p2))
                    p.sendMessage(translation.isIgnored, true);
                else if (msg.equals("") || msg.equals(" "))
                    p.sendMessage(translation.pmEmpty, true);
                else {
                    p.sendMessage(createmsg(p, msg, msgSender, p2));
                    p2.sendMessage(createmsg(p, msg, msgViewer, p2));
                    replies.put(p, p2);
                    replies.put(p2, p);
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

    public boolean canUseEmojiCategory(TabPlayer p, String category) {
        if (!cm.emojiCounts.containsKey(category)) return false;
        String permOn = cm.emojis.get(category).containsKey("permission") ? cm.emojis.get(category).get("permission")+"" : "";
        return !permOn.equalsIgnoreCase("category") || p.hasPermission("tabadditions.chat.emoji.category."+category);

    }
    public boolean canUseEmoji(TabPlayer p, String category, String emoji) {
        if (!cm.emojiCounts.containsKey(category)) return false;
        String permOn = cm.emojis.get(category).containsKey("permission") ? cm.emojis.get(category).get("permission")+"" : "";
        if (permOn.equalsIgnoreCase("emoji"))
            return p.hasPermission("tabadditions.chat.emoji."+emoji);
        return !permOn.equalsIgnoreCase("category") || p.hasPermission("tabadditions.chat.emoji.category."+category);
    }

    public void getEmojisCategories(TabPlayer p) {
        List<IChatBaseComponent> list = new ArrayList<>();
        TranslationFile translation = TABAdditions.getInstance().getMsgs();

        for (String emojiCategory : cm.emojis.keySet()) {
            if (canUseEmojiCategory(p,emojiCategory)) {
                int owned = cm.ownedEmojis(p,emojiCategory);
                if (owned == 0) continue;
                IChatBaseComponent subcomp = cm.createComponent("\n"+EnumChatFormat.color(translation
                        .getEmojiCategory(emojiCategory,owned,cm.emojiCounts.get(emojiCategory))),p);
                subcomp.getModifier().onClickRunCommand("/emojis "+emojiCategory);
                list.add(subcomp);
            }
        }
        IChatBaseComponent comp = cm.createComponent("\n"+EnumChatFormat.color(translation
                .getEmojiCategoryHeader(list.size(),cm.ownedEmojis(p),cm.emojiTotalCount)),p);
        comp.setExtra(list);
        p.sendMessage(comp);
    }

    public void getEmojiCategory(TabPlayer p, String category) {
        List<IChatBaseComponent> list = new ArrayList<>();
        TranslationFile translation = TABAdditions.getInstance().getMsgs();

        Map<String,String> emojis = (Map<String,String>)cm.emojis.get(category).get("list");

        for (String emoji : emojis.keySet()) {
            if (!canUseEmoji(p,category,emoji)) continue;
            IChatBaseComponent comp = cm.createComponent("\n" + EnumChatFormat.color(translation.getEmoji(emoji,emojis.get(emoji))),p);
            comp.getModifier().onClickSuggestCommand(emoji);
            list.add(comp);
        }

        if (list.isEmpty()) {
            p.sendMessage(translation.emojiCategoryNotFound, true);
            return;
        }
        IChatBaseComponent comp = cm.createComponent(EnumChatFormat.color(translation.getEmojiHeader(list.size(),cm.emojiCounts.get(category))),p);
        comp.setExtra(list);
        p.sendMessage(comp);

    }

    public boolean isIgnored(TabPlayer p, TabPlayer viewer) {
        return tab.getPlayerCache().getStringList("msg-ignore." + viewer.getName().toLowerCase(), new ArrayList<>()).contains(p.getName().toLowerCase());
    }
}
