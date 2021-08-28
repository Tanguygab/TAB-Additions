package io.github.tanguygab.tabadditions.shared.features.chat;

import io.github.tanguygab.tabadditions.shared.PlatformType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.api.config.YamlConfigurationFile;
import me.neznamy.tab.shared.TAB;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class ChatCmds {

    private final ChatManager cm;

    public boolean msgEnabled;
    public String msgSender;
    public String msgViewer;
    public boolean msgSelf;
    public boolean ignoreCmd;
    public boolean togglemsgCmd;
    public boolean replyCmd;
    public Map<TabPlayer, TabPlayer> replies = new HashMap<>();

    public boolean emojisCmd;

    public boolean clearChatCmd;
    public String clearChatLine;
    public int clearChatAmount;

    public ChatCmds(ChatManager manager, ConfigurationFile config) {
        cm = manager;
        msgEnabled = config.getBoolean("msg.enabled",true);
        msgSender = config.getString("msg.sender","{&7[&6&lMe &e➠ &6&l%viewer:prop-customchatname%&7] %msg%||%time%\\n\\n&fClick to reply to &6%viewer:prop-customchatname%&f.||suggest:/msg %player% }");
        msgViewer = config.getString("msg.viewer","{&7[&6&l%prop-customchatname% &e➠ &6&lMe&7] %msg%||%time%\\n\\n&fClick to reply to &6%prop-customchatname%&f.||suggest:/msg %player% }");
        msgSelf = config.getBoolean("msg.msg-self",true);
        ignoreCmd = config.getBoolean("msg./ignore",true);
        togglemsgCmd = config.getBoolean("msg./togglemsg",true);
        replyCmd = config.getBoolean("msg./reply",true);

        emojisCmd = config.getBoolean("emojis./emojis",true);

        clearChatCmd = config.getBoolean("clearchat.enabled",true);
        clearChatAmount = config.getInt("clearchat.amount",100);
        clearChatLine = config.getString("clearchat.line","");
    }

    public IChatBaseComponent createmsg(TabPlayer p, String msg, String chatformat, TabPlayer viewer) {
        return cm.createmsg(p,msg,chatformat,viewer);
    }

    public void execute(TabPlayer p, String cmd, String[] args) {
        List<String> args2 = Arrays.asList(args);
        String msg = "";
        for (String str : args2) {
            msg += str;
            if (args2.indexOf(str) != args2.size()-1)
                msg += " ";
        }

        ConfigurationFile playerdata = getPlayerData();
        ConfigurationFile translation = TAB.getInstance().getConfiguration().getTranslation();

        if (cm.emojiEnabled && cmd.equalsIgnoreCase("emojis")) {
            String output = translation
                    .getString("tab+_/emojis_header","&7All emojis you have access to (%amount%):")
                    .replace("%amount%","0");
            for (String emoji : cm.emojis.keySet()) {
                if (p.hasPermission("tabadditions.chat.emoji."+emoji))
                    output+="\n"+translation
                        .getString("tab+_/emojis_emoji","&7%emojiraw%&8: &r%emoji%")
                        .replace("%emojiraw%",emoji)
                        .replace("%emoji%",cm.emojis.get(emoji));
            }
            p.sendMessage(output,true);
            return;
        }
        if (clearChatCmd && cmd.equalsIgnoreCase("clearchat") && p.hasPermission("tabadditions.chat.clearchat")) {
            String linebreaks = "";
            for (int i = 0; i < clearChatAmount; i++)
                linebreaks+="\n"+clearChatLine;
            p.sendMessage(linebreaks,false);
            p.sendMessage(translation.getString("tab+_chat_cleared", "&aChat cleared by %name%!").replace("%name%",p.getName()),true);
            return;
        }

        if (!msgEnabled) return;
        switch (cmd.toLowerCase()) {
            case "togglemsg": {
                if (!togglemsgCmd) return;
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
                if (!ignoreCmd) return;
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
                if (!replyCmd) return;
            case "msg": {
                TabPlayer p2;
                if (cmd.equals("r") || cmd.equals("reply"))
                    p2 = replies.getOrDefault(p, null);
                else {
                    String player = msg.split(" ")[0];
                    msg = msg.replaceFirst(player+" ", "");
                    p2 = TABAdditions.getInstance().getPlayer(player);
                }

                if (p2 == null)
                    p.sendMessage(translation.getString("player_not_found", "&4[TAB] Player not found!"), true);
                else if (!msgSelf && p == p2)
                    p.sendMessage(translation.getString("tab+_cant_pm_self", "&cYou can't message yourself!"), true);
                else if (!p.hasPermission("tabadditions.chat.bypass.togglemsg") && TAB.getInstance().getConfiguration().getPlayerData("togglemsg").contains(p2.getName()))
                    p.sendMessage(translation.getString("tab+_has_pm_off", "&cThis player doesn't accept private messages"), true);
                else if (!p.hasPermission("tabadditions.chat.bypass.ignore") && playerdata.getStringList("msg-ignore." + p2.getName().toLowerCase(), new ArrayList<>()).contains(p.getName().toLowerCase()))
                    p.sendMessage(translation.getString("tab+_ignores_you", "&cThis player ignores you"), true);
                else {
                    p.sendMessage(createmsg(p, msg, msgSender, p2));
                    p2.sendMessage(createmsg(p, msg, msgViewer, p2));
                    replies.put(p, p2);
                    replies.put(p2, p);
                    break;
                }
            }
        }
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

    public ConfigurationFile getPlayerData() {
        if (TAB.getInstance().getConfiguration().getPlayerData("togglesmsg") == null) {
            File file = new File(TAB.getInstance().getPlatform().getDataFolder(), "playerdata.yml");

            try {
                if (file.exists() || file.createNewFile())
                    return new YamlConfigurationFile(null, file);
            } catch (Exception var4) {
                TabAPI.getInstance().getErrorManager().criticalError("Failed to load playerdata.yml", var4);
            }
        } return TAB.getInstance().getConfiguration().getPlayerDataFile();
    }

}
