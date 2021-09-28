package io.github.tanguygab.tabadditions.shared.features.chat;

import io.github.tanguygab.tabadditions.shared.Platform;
import io.github.tanguygab.tabadditions.shared.PlatformType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.PlaceholderManager;
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

    private final ChatManager cm;

    public boolean msgEnabled;
    public String msgSender;
    public String msgViewer;
    public boolean msgSelf;
    public boolean ignoreEnabled;
    public boolean togglemsgEnabled;
    public boolean togglementionEnabled;
    public boolean replyEnabled;
    public Map<TabPlayer, TabPlayer> replies = new HashMap<>();
    public long msgCooldownTime;
    public Map<TabPlayer,LocalDateTime> msgCooldown = new HashMap<>();


    public boolean emojisEnabled;

    public boolean socialspyEnabled;
    public boolean spyMsgsEnabled;
    public String spyMsgsOutput;

    public boolean clearchatEnabled;
    public String clearChatLine;
    public int clearChatAmount;

    public ChatCmds(ChatManager manager, ConfigurationFile config) {
        cm = manager;
        msgEnabled = config.getBoolean("msg.enabled",true);
        msgSender = config.getString("msg.sender","{&7[&6&lMe &e➠ &6&l%viewer:prop-customchatname%&7] %msg%||%time%\\n\\n&fClick to reply to &6%viewer:prop-customchatname%&f.||suggest:/msg %player% }");
        msgViewer = config.getString("msg.viewer","{&7[&6&l%prop-customchatname% &e➠ &6&lMe&7] %msg%||%time%\\n\\n&fClick to reply to &6%prop-customchatname%&f.||suggest:/msg %player% }");
        msgSelf = config.getBoolean("msg.msg-self",true);
        ignoreEnabled = config.getBoolean("msg./ignore",true);
        togglemsgEnabled = config.getBoolean("msg./togglemsg",true);
        replyEnabled = config.getBoolean("msg./reply",true);
        togglementionEnabled = config.getBoolean("mention./togglemention",true);
        msgCooldownTime = Long.parseLong(config.getInt("msg.cooldown",0)+"");

        emojisEnabled = config.getBoolean("emojis./emojis",true);

        socialspyEnabled = config.getBoolean("socialspy.enabled",true);
        spyMsgsEnabled = config.getBoolean("socialspy.msgs.spy",true);
        spyMsgsOutput = config.getString("socialspy.msgs.output","{SocialSpy-Msg: [6&l%prop-customchatname% &e➠ 6&l%viewer:prop-customchatname%] %msg%||%time%}");

        clearchatEnabled = config.getBoolean("clearchat.enabled",true);
        clearChatAmount = config.getInt("clearchat.amount",100);
        clearChatLine = config.getString("clearchat.line","");

        PlaceholderManager pm = TabAPI.getInstance().getPlaceholderManager();
        pm.registerPlayerPlaceholder("%chat-mentions%",1000,p->cm.mentionDisabled.contains(p.getName().toLowerCase()) ? "Off" : "On");
        pm.registerPlayerPlaceholder("%chat-socialspy%",1000,p->cm.spies.contains(p.getName().toLowerCase()) ? "On" : "Off");
        pm.registerPlayerPlaceholder("%chat-messages%",1000,p->TabAPI.getInstance().getPlayerCache().getStringList("togglemsg").contains(p.getName().toLowerCase()) ? "Off" : "On");

        Platform p = TABAdditions.getInstance().getPlatform();
        p.registerCommand("msg",msgEnabled,"tell","whisper","w","m");
        p.registerCommand("reply",replyEnabled,"r");
        p.registerCommand("ignore",ignoreEnabled);
        p.registerCommand("togglemsg",togglemsgEnabled);
        p.registerCommand("togglemention",togglementionEnabled);
        p.registerCommand("emojis",emojisEnabled);
        p.registerCommand("socialspy",socialspyEnabled);
        p.registerCommand("clearchat",clearchatEnabled);
    }

    public IChatBaseComponent createmsg(TabPlayer p, String msg, String chatformat, TabPlayer viewer) {
        return cm.createmsg(p,msg,chatformat,viewer);
    }

    public void execute(TabPlayer p, String cmd, String[] args) {
        TABAdditions plugin = TABAdditions.getInstance();
        List<String> args2 = Arrays.asList(args);
        String msg = "";
        for (String str : args2) {
            msg += str;
            if (args2.indexOf(str) != args2.size()-1)
                msg += " ";
        }

        ConfigurationFile playerdata = TabAPI.getInstance().getPlayerCache();
        ConfigurationFile translation = plugin.getTranslation();

        if (cm.emojiEnabled && cmd.equalsIgnoreCase("emojis")) {
            if (args.length < 1) {
                getEmojisCategories(p);
                return;
            }

            String cat = args[0];
            if (!cm.emojis.containsKey(cat) || !cm.emojis.get(cat).containsKey("list") || !p.hasPermission("tabadditions.chat.emoji.category."+cat)) {
                p.sendMessage(translation.getString("tab+_/emojis_category_not_found","&7This category doesn't exist."),true);
                return;
            }
            getEmojiCategory(p,cat);
            return;
        }

        if (socialspyEnabled && cmd.equalsIgnoreCase("socialspy") && p.hasPermission("tabadditions.chat.socialspy")) {
            if (cm.spies.contains(p.getName().toLowerCase())) {
                cm.spies.remove(p.getName().toLowerCase());
                p.sendMessage(translation.getString("tab+_chat_socialspy_off", "&cSocialSpy disabled."), true);
            }
            else {
                cm.spies.add(p.getName().toLowerCase());
                p.sendMessage(translation.getString("tab+_chat_socialspy_on", "&aSocialSpy enabled."), true);
            }
            return;
        }

        if (clearchatEnabled && cmd.equalsIgnoreCase("clearchat") && p.hasPermission("tabadditions.chat.clearchat")) {
            String linebreaks = "";
            for (int i = 0; i < clearChatAmount; i++)
                linebreaks+="\n"+clearChatLine;
            p.sendMessage(linebreaks,false);
            p.sendMessage(translation.getString("tab+_chat_cleared", "&aChat cleared by %name%!").replace("%name%",p.getName()),true);
            return;
        }
        if (togglementionEnabled && cmd.equalsIgnoreCase("togglemention")) {
            if (cm.mentionDisabled.contains(p.getName().toLowerCase())) {
                cm.mentionDisabled.remove(p.getName().toLowerCase());
                p.sendMessage(translation.getString("tab+_chat_mention_on", "&aMentions enabled."), true);
            }
            else {
                cm.mentionDisabled.add(p.getName().toLowerCase());
                p.sendMessage(translation.getString("tab+_chat_mention_off", "&cMentions disabled."), true);
            }
            return;
        }

        if (!msgEnabled) return;
        switch (cmd.toLowerCase()) {
            case "togglemsg": {
                if (!togglemsgEnabled) return;
                List<String> list = playerdata.getStringList("togglemsg");
                if (list.contains(p.getName().toLowerCase())) {
                    list.remove(p.getName().toLowerCase());
                    p.sendMessage(translation.getString("tab+_chat_messages_on", "&aYou will now receive new private messages!"), true);
                } else {
                    list.add(p.getName().toLowerCase());
                    p.sendMessage(translation.getString("tab+_chat_messages_off", "&cYou won't receive any new private messages!"), true);
                }
                playerdata.set("togglemsg", list);
                break;
            }
            case "ignore": {
                if (!ignoreEnabled) return;
                if (msg.split(" ").length < 1) {
                    p.sendMessage(translation.getString("player_not_found", "&4[TAB] Player not found!"), true);
                    return;
                }
                String p2 = msg.split(" ")[0];
                Map<String, List<String>> map = playerdata.getConfigurationSection("msg-ignore");
                if (map.containsKey(p.getName().toLowerCase())) {
                    if (map.get(p.getName().toLowerCase()).contains(p2.toLowerCase())) {
                        map.get(p.getName().toLowerCase()).remove(p2.toLowerCase());
                        p.sendMessage(translation.getString("tab+_ignore_off", "&aYou will now receive new private messages from %name%!").replace("%name%", p2), true);
                    } else {
                        map.get(p.getName().toLowerCase()).add(p2.toLowerCase());
                        p.sendMessage(translation.getString("tab+_ignore_on", "&cYou won't receive any new private messages from %name%!").replace("%name%", p2), true);
                    }
                } else map.put(p.getName().toLowerCase(), new ArrayList<>(Collections.singletonList(p2.toLowerCase())));
                playerdata.set("msg-ignore", map);
                break;
            }
            case "reply":
                if (!replyEnabled) return;
            case "msg": {
                if (msgCooldown.containsKey(p)) {
                    long time = ChronoUnit.SECONDS.between(msgCooldown.get(p), LocalDateTime.now());
                    if (time < msgCooldownTime) {
                        p.sendMessage(translation
                                .getString("tab+_message_cooldown", "&cYou have to wait %seconds% more seconds!")
                                .replace("%seconds%", msgCooldownTime-time+""), true);
                        return;
                    }
                    msgCooldown.remove(p);
                }
                if (msgCooldownTime != 0 && !p.hasPermission("tabadditions.chat.bypass.cooldown"))
                    msgCooldown.put(p,LocalDateTime.now());

                TabPlayer p2;
                if (cmd.equals("reply"))
                    p2 = replies.getOrDefault(p,null);
                else {
                    String player = msg.split(" ")[0];
                    msg = msg.replaceFirst(player+"( )?", "");
                    p2 = plugin.getPlayer(player);
                }

                if (p2 == null)
                    p.sendMessage(translation.getString("player_not_found", "&4[TAB] Player not found!"), true);
                else if (!msgSelf && p == p2)
                    p.sendMessage(translation.getString("tab+_cant_pm_self", "&cYou can't message yourself!"), true);
                else if (!p.hasPermission("tabadditions.chat.bypass.togglemsg") && playerdata.getStringList("togglemsg").contains(p2.getName().toLowerCase()))
                    p.sendMessage(translation.getString("tab+_has_pm_off", "&cThis player doesn't accept private messages"), true);
                else if (!p.hasPermission("tabadditions.chat.bypass.ignore") && playerdata.getStringList("msg-ignore." + p2.getName().toLowerCase(), new ArrayList<>()).contains(p.getName().toLowerCase()))
                    p.sendMessage(translation.getString("tab+_ignores_you", "&cThis player ignores you"), true);
                else if (msg.equals("") || msg.equals(" "))
                    p.sendMessage(translation.getString("tab+_chat_pm_empty", "&7You have to provide a message!"), true);
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
                    break;
                }
            }
        }
    }

    public boolean canUseEmojiCategory(TabPlayer p, String category) {
        String permOn = cm.emojis.get(category).containsKey("permission") ? cm.emojis.get(category).get("permission")+"" : "";
        return !permOn.equalsIgnoreCase("category") || p.hasPermission("tabadditions.chat.emoji.category."+category);

    }
    public boolean canUseEmoji(TabPlayer p, String category, String emoji) {
        String permOn = cm.emojis.get(category).containsKey("permission") ? cm.emojis.get(category).get("permission")+"" : "";
        if (permOn.equalsIgnoreCase("emoji") && p.hasPermission("tabadditions.chat.emoji."+emoji))
            return true;
        return !permOn.equalsIgnoreCase("category") || p.hasPermission("tabadditions.chat.emoji.category."+category);
    }

    public void getEmojisCategories(TabPlayer p) {
        List<IChatBaseComponent> list = new ArrayList<>();

        for (String emojiCategory : cm.emojis.keySet()) {
            if (canUseEmojiCategory(p,emojiCategory)) {
                IChatBaseComponent subcomp = cm.createComponent("\n"+EnumChatFormat.color(TABAdditions.getInstance().getTranslation()
                        .getString("tab+_/emojis_category","&7 - &8%category%")
                        .replace("%category%",emojiCategory)),p);
                subcomp.getModifier().onClickRunCommand("/emojis "+emojiCategory);
                list.add(subcomp);
            }
        }
        IChatBaseComponent comp = cm.createComponent("\n"+EnumChatFormat.color(TABAdditions.getInstance().getTranslation()
                .getString("tab+_/emojis_categories_header","&7All categories of emojis you have access to (%amount%):")
                .replace("%amount%",list.size()+"")
        ),p);
        comp.setExtra(list);
        p.sendMessage(comp);
    }

    public void getEmojiCategory(TabPlayer p, String category) {
        List<IChatBaseComponent> list = new ArrayList<>();

        Map<String,String> emojis = (Map<String,String>)cm.emojis.get(category).get("list");

        for (String emoji : emojis.keySet()) {
            if (!canUseEmoji(p,category,emoji)) continue;
            IChatBaseComponent comp = cm.createComponent("\n" + EnumChatFormat.color(TABAdditions.getInstance().getTranslation()
                    .getString("tab+_/emojis_emoji", "&7 - %emojiraw%&8: &r%emoji%")
                    .replace("%emojiraw%", emoji)
                    .replace("%emoji%", emojis.get(emoji)))
            ,p);
            comp.getModifier().onClickSuggestCommand(emoji);
            list.add(comp);
        }

        IChatBaseComponent comp = cm.createComponent(EnumChatFormat.color(TABAdditions.getInstance().getTranslation()
                .getString("tab+_/emojis_emojis_header","&7All emojis in this category (%amount%):")
                .replace("%amount%",list.size()+"")),p);
        comp.setExtra(list);
        p.sendMessage(comp);
    }

    public List<String> tabcomplete(TabPlayer p, String cmd, String[] args) {
        if (msgEnabled) return null;
        if ((cmd.equalsIgnoreCase("msg") || cmd.equalsIgnoreCase("ignore") && args.length == 1)) {
            List<String> list = new ArrayList<>();
            for (TabPlayer player : TabAPI.getInstance().getOnlinePlayers()) {
                if (!player.isVanished() && player != p && (TABAdditions.getInstance().getPlatform().getType() != PlatformType.SPIGOT || ((Player)p.getPlayer()).canSee((Player) player.getPlayer())))
                    list.add(player.getName());
            }
            return list;
        }
        return null;
    }

}
