package io.github.tanguygab.tabadditions.shared.features.chat;

import io.github.tanguygab.tabadditions.shared.PlatformType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.config.ConfigurationFile;
import org.bukkit.entity.Player;

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

        emojisEnabled = config.getBoolean("emojis./emojis",true);

        socialspyEnabled = config.getBoolean("socialspy.enabled",true);
        spyMsgsEnabled = config.getBoolean("socialspy.msgs.spy",true);
        spyMsgsOutput = config.getString("socialspy.msgs.output","{SocialSpy-Msg: [6&l%prop-customchatname% &e➠ 6&l%viewer:prop-customchatname%] %msg%||%time%}");

        clearchatEnabled = config.getBoolean("clearchat.enabled",true);
        clearChatAmount = config.getInt("clearchat.amount",100);
        clearChatLine = config.getString("clearchat.line","");
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

        ConfigurationFile playerdata = TAB.getInstance().getConfiguration().getPlayerDataFile();
        ConfigurationFile translation = TAB.getInstance().getConfiguration().getTranslation();

        if (cm.emojiEnabled && cmd.equalsIgnoreCase("emojis")) {
            List<IChatBaseComponent> list = new ArrayList<>();
            int count = 0;
            for (String emoji : cm.emojis.keySet()) {
                if (p.hasPermission("tabadditions.chat.emoji."+emoji)) {
                    IChatBaseComponent comp = IChatBaseComponent.optimizedComponent("\n" + TAB.getInstance().getPlaceholderManager().color(translation
                            .getString("tab+_/emojis_emoji", "&7%emojiraw%&8: &r%emoji%")
                            .replace("%emojiraw%", emoji)
                            .replace("%emoji%", cm.emojis.get(emoji)))
                    );
                    comp.onClickSuggestCommand(emoji);
                    list.add(comp);
                    count++;
                }
            }
            IChatBaseComponent comp = IChatBaseComponent.optimizedComponent(TAB.getInstance().getPlaceholderManager().color(translation
                    .getString("tab+_/emojis_header","&7All emojis you have access to (%amount%):")
                    .replace("%amount%",count+"")));
            comp.setExtra(list);
            p.sendMessage(comp);
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
                p.sendMessage(translation.getString("tab+_chat_mention_off", "&cMentions disabled."), true);
            }
            else {
                cm.mentionDisabled.add(p.getName().toLowerCase());
                p.sendMessage(translation.getString("tab+_chat_mention_on", "&aMentions enabled."), true);
            }
            return;
        }

        if (!msgEnabled) return;
        switch (cmd.toLowerCase()) {
            case "togglemsg": {
                if (!togglemsgEnabled) return;
                List<String> list = playerdata.getStringList("togglemsg");
                if (list.contains(p.getName())) {
                    list.remove(p.getName());
                    p.sendMessage(translation.getString("tab+_togglemsg_off", "&aYou will now receive new private messages!"), true);
                } else {
                    list.add(p.getName());
                    p.sendMessage(translation.getString("tab+_togglemsg_on", "&cYou won't receive any new private messages!"), true);
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
                String p2 = msg.split(" ")[0].toLowerCase();
                Map<String, List<String>> map = playerdata.getConfigurationSection("msg-ignore");
                if (map.containsKey(p.getName())) {
                    if (map.get(p.getName()).contains(p2)) {
                        map.get(p.getName()).remove(p2);
                        p.sendMessage(translation.getString("tab+_ignore_off", "&aYou will now receive new private messages from %name%!").replace("%name%", p2), true);
                    } else {
                        map.get(p.getName()).add(p2);
                        p.sendMessage(translation.getString("tab+_ignore_on", "&cYou won't receive any new private messages from %name%!").replace("%name%", p2), true);
                    }
                } else map.put(p.getName(), new ArrayList<>(Collections.singletonList(p2)));
                playerdata.set("msg-ignore", map);
                break;
            }
            case "reply":
            case "r":
                if (!replyEnabled) return;
            case "msg": {
                TabPlayer p2;
                if (cmd.equals("r") || cmd.equals("reply"))
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
                else if (!p.hasPermission("tabadditions.chat.bypass.togglemsg") && playerdata.getStringList("togglemsg").contains(p2.getName()))
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

    public List<String> tabcomplete(TabPlayer p, String cmd, String[] args) {
        if (msgEnabled) return null;
        if ((cmd.equalsIgnoreCase("msg") || cmd.equalsIgnoreCase("ignore") && args.length == 1)) {
            List<String> list = new ArrayList<>();
            for (TabPlayer player : TAB.getInstance().getPlayers()) {
                if (!player.isVanished() && player != p && (TABAdditions.getInstance().getPlatform().getType() != PlatformType.SPIGOT || ((Player)p.getPlayer()).canSee((Player) player.getPlayer())))
                    list.add(player.getName());
            }
            return list;
        }
        return null;
    }

}
