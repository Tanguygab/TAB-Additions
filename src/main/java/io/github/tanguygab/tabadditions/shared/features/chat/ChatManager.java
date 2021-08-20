package io.github.tanguygab.tabadditions.shared.features.chat;

import github.scarsz.discordsrv.DiscordSRV;
import io.github.tanguygab.tabadditions.shared.ConfigType;
import io.github.tanguygab.tabadditions.shared.PlatformType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.spigot.TABAdditionsSpigot;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.chat.TextColor;
import me.neznamy.tab.api.chat.rgb.RGBUtils;
import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.shared.TAB;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;

public class ChatManager extends TabFeature {

    private TABAdditions plinstance;
    private TabAPI tab;
    private final Map<String,ChatFormat> formats = new HashMap<>();
    public final Map<TabPlayer,String> defformats = new HashMap<>();


    public boolean itemEnabled = true;
    public String itemInput = "[item]";
    public String itemOutput = "%item% x%amount%";
    public String itemOutputSingle = "%item%";
    public String itemOutputAir = "No Item";
    public boolean itemPermssion = false;

    public boolean mentionEnabled = true;
    public String mentionInput = "@%player%";
    public String mentionOutput = "&b";
    public String mentionSound = "BLOCK_NOTE_BLOCK_PLING";

    public Map<String,String> emojis = new HashMap<>();
    public boolean emojiUntranslate = false;

    public long cooldownTime = 0;
    public Map<TabPlayer,LocalDateTime> cooldown = new HashMap<>();

    public ChatManager() {
        super("&aChat&r");
        tab = TabAPI.getInstance();
        load();
    }

    public ChatFormat getFormat(TabPlayer p) {
        String format;
        if (defformats.containsKey(p))
            format = defformats.get(p);
        else format = plinstance.getConfig(ConfigType.CHAT).getString("default-format","default");
        if (format.equalsIgnoreCase("")) return defFormat();

        ChatFormat f = formats.get(format);
        while (f != null && !f.isConditionMet(p)) {
            f = formats.get(f.getChildLayout());

            if (f == null)
                return defFormat();
        }
        return f;
    }

    @Override
    public void load() {
        plinstance = TABAdditions.getInstance();
        ConfigurationFile config = plinstance.getConfig(ConfigType.CHAT);
        for (Object format : config.getConfigurationSection("chat-formats").keySet())
            formats.put(format+"",new ChatFormat(format+"", config.getConfigurationSection("chat-formats."+format),this));

        itemEnabled = config.getBoolean("item.enabled",true);
        itemInput = config.getString("item.input","[item]");
        itemOutput = config.getString("item.output","%name% x%amount%");
        itemOutputSingle = config.getString("item.output-single","%name%");
        itemOutputAir = config.getString("item.output-air","No Item");
        itemPermssion = config.getBoolean("item.permission",false);

        mentionEnabled = config.getBoolean("mention.enabled",true);
        mentionInput = config.getString("mention.input","@%player%");
        mentionOutput = config.getString("mention.output","&b@%player%");
        mentionSound = config.getString("mention.sound","BLOCK_NOTE_BLOCK_PLING");

        emojis = config.getConfigurationSection("emojis");
        emojiUntranslate = config.getBoolean("block-emojis-without-permission",false);

        cooldownTime = Long.parseLong(config.getInt("message-cooldown",0)+"");

        for (TabPlayer p : tab.getOnlinePlayers()) {
            p.loadPropertyFromConfig(this,"chatprefix");
            p.loadPropertyFromConfig(this,"customchatname",p.getName());
            p.loadPropertyFromConfig(this,"chatsuffix");
        }
    }

    public ChatFormat defFormat() {
        Map<String,Object> map = new HashMap<>();
        Map<String,Object> components = new HashMap<>();
        Map<String,Object> text = new HashMap<>();

        text.put("text","%tab_chatprefix% %tab_customchatname% %tab_chatsuffix%&7\u00bb &r%msg%");
        components.put("text",text);
        map.put("components",components);
        return new ChatFormat("default", map,this);
    }

    public void onChat(TabPlayer p, String msg) {
        if (plinstance.isMuted(p)) return;
        if (cooldown.containsKey(p)) {
            long time = ChronoUnit.SECONDS.between(cooldown.get(p),LocalDateTime.now());
            if (time < cooldownTime) {
                p.sendMessage(TAB.getInstance().getConfiguration().getTranslation()
                        .getString("tab+_message_cooldown", "&cYou have to wait %seconds% more seconds!")
                        .replace("%seconds%", cooldownTime-time+""), true);
                return;
            }
            cooldown.remove(p);
        }
        if (cooldownTime != 0 && !p.hasPermission("tabadditions.chat.cooldown.bypass"))
            cooldown.put(p,LocalDateTime.now());

        msg = emojicheck(p,msg);

        ChatFormat chatFormat = getFormat(p);

        tab.sendConsoleMessage(createmsg(p,msg,chatFormat,null).toLegacyText(), true);

        for (TabPlayer pl : tab.getOnlinePlayers()) {
            if (canSee(p,pl))
                pl.sendMessage(createmsg(p,msg,chatFormat,pl));
        }
        if (TABAdditions.getInstance().getPlatform().getType() == PlatformType.SPIGOT && Bukkit.getServer().getPluginManager().isPluginEnabled("DiscordSRV"))
            if (TABAdditions.getInstance().getConfig(ConfigType.CHAT).getBoolean("DiscordSRV-Support",true)) {
                DiscordSRV discord = DiscordSRV.getPlugin();
                if (canSee(p, null))
                    discord.processChatMessage(Bukkit.getServer().getPlayer(p.getUniqueId()), msg, discord.getMainChatChannel(), false);
                else if (getFormat(p).isViewConditionMet(p,null) && !discord.getOptionalChannel(chatFormat.getChannel()).equals(discord.getMainChatChannel())) discord.processChatMessage(Bukkit.getServer().getPlayer(p.getUniqueId()),msg, discord.getOptionalChannel(chatFormat.getChannel()),false);
            }

        return;
    }

    public IChatBaseComponent createmsg(TabPlayer p, String msg, ChatFormat chatFormat, TabPlayer viewer) {

        List<String> codes = Arrays.asList("a", "b", "c", "d", "f", "k", "l", "m", "n", "o", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        for (String code : codes) {
            if (!p.hasPermission("tabadditions.chat.color.&" + code))
                msg = msg.replace("&" + code, "");
        }
        msg = pingcheck(p, msg, viewer);
        msg = new RGBUtils().applyFormats(msg, true);
        msg = EnumChatFormat.color(msg);
        if (!p.hasPermission("tabadditions.chat.color.rgb")) {
            msg = Pattern.quote(msg)
                    .replaceAll("#[0-9a-fA-F]{6}", "")
                    .replace("\\Q", "")
                    .replace("\\E", "");
        }

        Map<String, Map<String, Object>> format = chatFormat.getText();
        IChatBaseComponent messsage = new IChatBaseComponent("");
        TextColor oldColor = null;
        for (Map<String, Object> component : format.values()) {
            IChatBaseComponent comp = checkText(p, viewer, msg, component,oldColor);
            checkHoverAndClick(comp, p, viewer, msg, component);
            messsage.addExtra(comp);
            oldColor = getLastColor(comp);
        }
        return messsage;
    }
    public TextColor getLastColor(IChatBaseComponent component) {
        if (component.getExtra() == null) return component.getModifier().getColor();
        List<IChatBaseComponent> list = new ArrayList<>(component.getExtra());
        Collections.reverse(list);
        for (IChatBaseComponent comp : list) {
                if (comp.getModifier().getColor() != null)
                    return comp.getModifier().getColor();
        }
        if (component.getModifier().getColor() != null) return component.getModifier().getColor();
        if (component.getText().contains("\u00A7")) {
            int i = component.getText().lastIndexOf("\u00A7");
            if (component.getText().chars().toArray().length == i+2) return null;
            char c = component.getText().charAt(i+1);
            return new TextColor(EnumChatFormat.getByChar(c));
        }
        return null;
    }

    public IChatBaseComponent checkText(TabPlayer p, TabPlayer viewer, String msg, Map<String,Object> config, TextColor lastcolor) {
        if (!config.containsKey("text")) return new IChatBaseComponent("");
        if (plinstance.getPlatform().getType() == PlatformType.SPIGOT && itemEnabled && (!itemPermssion || p.hasPermission("tabadditions.item")) && msg.contains(itemInput) && config.get("text").toString().contains("%msg%"))
            return itemcheck(config.get("text")+"",p,msg,viewer, lastcolor);
        return IChatBaseComponent.optimizedComponent((lastcolor != null ? lastcolor.getHexCode() : "")+plinstance
                .parsePlaceholders(config.get("text")+"", p,viewer,p)
                .replace("%msg%", msg));
    }

    public IChatBaseComponent checkHoverAndClick(IChatBaseComponent comp,TabPlayer p, TabPlayer viewer, String msg, Map<String,Object> config) {
        if (config.containsKey("hover") && config.get("hover") instanceof List) {
            List<String> list = (List<String>) config.get("hover");
            String txt = "";
            for (String str : list) {
                if (list.indexOf(str) > 0) txt += "\n";
                txt += str;
            }
            txt = plinstance.parsePlaceholders(txt,p,viewer,p).replace("%msg%", msg);
            IChatBaseComponent hover = IChatBaseComponent.optimizedComponent(txt);
            comp.getModifier().onHoverShowText(hover);
        }
        if (config.containsKey("suggest")) {
            String txt = plinstance.parsePlaceholders(config.get("suggest")+"",p,viewer,p).replace("%msg%", msg);
            comp.getModifier().onClickSuggestCommand(txt);
        }
        else if (config.containsKey("command")) {
            String txt = plinstance.parsePlaceholders(config.get("command")+"",p,viewer,p).replace("%msg%", msg);
            comp.getModifier().onClickRunCommand(txt);
        }
        else if (config.containsKey("url")) {
            String txt = plinstance.parsePlaceholders(config.get("url")+"",p,viewer,p).replace("%msg%", msg);
            comp.getModifier().onClickOpenUrl(txt);
        }
        return comp;
    }

    public boolean canSee(TabPlayer sender, TabPlayer viewer) {
        if (sender == viewer) return true;
        if (viewer != null && !getFormat(sender).getChannel().equals(getFormat(viewer).getChannel())) return false;
        else if (viewer == null && !getFormat(sender).getChannel().equals("")) return false;
        return getFormat(sender).isViewConditionMet(sender, viewer);
    }

    public String emojicheck(TabPlayer p, String msg) {
        for (String emoji : emojis.keySet()) {
            int count = countMatches(msg,emoji);

            if (count == 0) continue;
            if (!p.hasPermission("tabadditions.chat.emoji."+emoji)) {
                if (emojiUntranslate && msg.contains(emojis.get(emoji)))
                    msg = msg.replace(emojis.get(emoji), emoji);
                continue;
            }

            List<String> list = Arrays.asList(msg.split(Pattern.quote(emoji)));
            msg = "";
            for (String part : list) {
                if (list.indexOf(part)+1 == list.size() && count != 1)
                    msg += part;
                else if (part.contains("&")) {
                    int i = part.lastIndexOf("&");
                    if (part.chars().toArray().length == i+2) return null;
                    char c = part.charAt(i+1);
                    EnumChatFormat color = EnumChatFormat.getByChar(c);
                    msg += part + emojis.get(emoji).replace("%lastcolor%",color == null ? "&r" : (color.getHexCode()));
                } else
                    msg += part + emojis.get(emoji).replace("%lastcolor%","&r");
            }
        }
        return msg;
    }

    public IChatBaseComponent itemcheck(String text, TabPlayer p, String msg, TabPlayer viewer, TextColor lastcolor) {

        IChatBaseComponent comp = new IChatBaseComponent("");
        if (lastcolor != null)
            comp.getModifier().setColor(lastcolor);

        List<String> list = new ArrayList<>(Arrays.asList(TABAdditions.getInstance().parsePlaceholders(text,p,viewer,p).split("%msg%")));
        if (list.size() < 1) list.add("");
        comp.addExtra(IChatBaseComponent.optimizedComponent(list.get(0)));

        ItemStack item;
        try {item = ((Player) p.getPlayer()).getInventory().getItemInMainHand();}
        catch (NoSuchMethodError e) {item = ((Player) p.getPlayer()).getInventory().getItemInHand();}

        List<String> ar = new ArrayList<>(Arrays.asList(msg.split(Pattern.quote(itemInput))));
        int itemcount = countMatches(msg,itemInput);

        if (ar.isEmpty()) ar.add("");
        TextColor color = null;
        for (String txt2 : ar) {
            IChatBaseComponent txt3 = IChatBaseComponent.optimizedComponent((color != null ? color.getHexCode() : "")+txt2);
            color = getLastColor(txt3);
            comp.addExtra(txt3);

            if (itemcount != 0) {
                IChatBaseComponent itemtxt;
                if (item.getType() != Material.AIR) {
                    String name;
                    if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
                        String type = item.getType().toString().replace("_", " ").toLowerCase();
                        String type2 = "";
                        List<String> typelist = new ArrayList<>(Arrays.asList(type.split(" ")));
                        for (String str : typelist) {
                            type2 = type2 + str.substring(0, 1).toUpperCase() + str.substring(1);
                            if (typelist.indexOf(str) != typelist.size() - 1) type2 = type2 + " ";
                        }
                        name = type2;
                    } else name = item.getItemMeta().getDisplayName();
                    if (item.getAmount() > 1)
                        itemtxt = IChatBaseComponent.optimizedComponent((color != null ? color.getHexCode() : "")+itemOutput.replace("%name%",name).replace("%amount%",item.getAmount()+""));
                    else itemtxt = IChatBaseComponent.optimizedComponent((color != null ? color.getHexCode() : "")+itemOutputSingle.replace("%name%",name));
                } else itemtxt = IChatBaseComponent.optimizedComponent((color != null ? color.getHexCode() : "")+itemOutputAir);
                itemtxt.setText(plinstance.parsePlaceholders(itemtxt.getText(),p,viewer,p));
                itemtxt.getModifier().onHoverShowItem(((TABAdditionsSpigot) plinstance.getPlugin()).itemStack(item));
                color = getLastColor(itemtxt);
                comp.addExtra(itemtxt);
                itemcount = itemcount-1;
            }

        }

        if (list.size() > 1) comp.addExtra(IChatBaseComponent.optimizedComponent((color != null ? color.getHexCode() : "")+list.get(1)));
        comp.setText("");
        return comp;
    }
    public String pingcheck(TabPlayer p, String msg, TabPlayer viewer) {

        String input = TABAdditions.getInstance().parsePlaceholders(mentionInput,p,viewer,viewer);
        if (msg.toLowerCase().contains(input.toLowerCase())) {
            msg = (msg.replaceAll("(?i)" + input, TABAdditions.getInstance().parsePlaceholders(mentionOutput, p, viewer, viewer)));
            if (TABAdditions.getInstance().getPlatform().getType().equals(PlatformType.SPIGOT)) {
                Player player = (Player) p.getPlayer();
                try {player.playSound(player.getLocation(), Sound.valueOf(mentionSound), 1, 1);}
                catch (Exception ignored) {}
            }
        }

        return msg;
    }

    public int countMatches(CharSequence str, CharSequence sub) {
        if (str != null &&  str.length() != 0 && sub != null && sub.length() != 0) {
            int count = 0;

            for(int idx = 0; (idx = str.toString().indexOf(sub.toString(),idx)) != -1; idx += sub.length()) {
                ++count;
            }


            return count;
        } else {
            return 0;
        }
    }

    @Override
    public void onJoin(TabPlayer p) {
        p.loadPropertyFromConfig(this,"chatprefix");
        p.loadPropertyFromConfig(this,"customchatname",p.getName());
        p.loadPropertyFromConfig(this,"chatsuffix");
    }

    @Override
    public boolean onCommand(TabPlayer p, String msg) {
        msg = msg.replaceFirst("/","");
        ConfigurationFile config = plinstance.getConfig(ConfigType.CHAT);
        ConfigurationFile playerdata = TAB.getInstance().getConfiguration().getPlayerDataFile();
        ConfigurationFile translation = TAB.getInstance().getConfiguration().getTranslation();

        if (msg.equals("togglemsg")) {
            List<String> list = TAB.getInstance().getConfiguration().getPlayerData("togglemsg");
            if (list.contains(p.getName())) {
                list.remove(p.getName());
                p.sendMessage(translation.getString("tab+_togglemsg_off","&aYou will now receive new private messages!"),true);
            }
            else {
                list.add(p.getName());
                p.sendMessage(translation.getString("tab+_togglemsg_on","&cYou won't receive any new private messages!"),true);
            }
            playerdata.set("togglemsg",list);
            return true;
        }
        if (msg.startsWith("ignore ")) {
            if (msg.split(" ").length < 2) {
                p.sendMessage(translation.getString("player_not_found","&4[TAB] Player not found!"),true);
                return true;
            }
            String p2 = msg.split(" ")[1].toLowerCase();
            TAB.getInstance().getConfiguration().getPlayerData("togglemsg");
            Map<String, List<String>> map = playerdata.getConfigurationSection("msg-ignore");
            if (map.containsKey(p.getName())) {
                if (map.get(p.getName()).contains(p2)) {
                    map.get(p.getName()).remove(p2);
                    p.sendMessage(translation.getString("tab+_ignore_off","&aYou will now receive new private messages from %name%!").replace("%name%",p2),true);
                }
                else {
                    map.get(p.getName()).add(p2);
                    p.sendMessage(translation.getString("tab+_ignore_on","&cYou won't receive any new private messages f%name%!").replace("%name%",p2),true);
                }
            }
            else map.put(p.getName(), new ArrayList<>(Collections.singletonList(p2)));
            playerdata.set("msg-ignore",map);
            return true;
        }

        if (formats.containsKey("msg") && msg.startsWith("msg ") && msg.split(" ").length >= 3) {
            String player = msg.split(" ")[1];
            String msg2 = msg.replace(msg.split(" ")[0]+" "+player+" ","");
            TabPlayer p2 = plinstance.getPlayer(player);
            if (p2 == null)
                p.sendMessage(translation.getString("player_not_found","&4[TAB] Player not found!"),true);
            else if (TAB.getInstance().getConfiguration().getPlayerData("togglemsg").contains(p2.getName()))
                p.sendMessage(translation.getString("tab+_has_pm_off","&cThis player doesn't accept private messages"),true);
            else if (playerdata.getStringList("msg-ignore."+p2.getName().toLowerCase(),new ArrayList<>()).contains(p.getName().toLowerCase()))
                p.sendMessage(translation.getString("tab+_ignores_you","&cThis player ignores you"),true);
            else {
                p2.sendMessage(createmsg(p, msg2, formats.get("msg"),p2));
                if (formats.containsKey("msgSender"))
                    p.sendMessage(createmsg(p,msg2,formats.get("msgSender"),p2));
            }
            return true;
        }
        if (config.getConfigurationSection("commands") == null || !config.getConfigurationSection("commands").containsKey(msg))
            return false;
        Map<String,String> cmd = (Map<String, String>) config.getConfigurationSection("commands").get(msg);
        String condition = cmd.get("condition")+"";
        String format = cmd.get("format")+"";
        String name = cmd.get("name")+"";
        if (!plinstance.isConditionMet(condition,p))
            p.sendMessage(translation.getString("no_permission","&cI'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error."),true);
        else {
            if (defformats.containsKey(p)) {
                defformats.remove(p);
                p.sendMessage(translation.getString("tab+_chat-cmd-leave","&7You left %name%!").replace("%name%",name), true);
            }
            else {
                defformats.put(p, format);
                p.sendMessage(translation.getString("tab+_chat-cmd-join","&7You joined %name%!").replace("%name%",name), true);
            }
        }
        return true;
    }
}
