package io.github.tanguygab.tabadditions.shared.features.chat;

import github.scarsz.discordsrv.DiscordSRV;
import io.github.tanguygab.tabadditions.shared.ConfigType;
import io.github.tanguygab.tabadditions.shared.PlatformType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.spigot.TABAdditionsSpigot;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.ChatClickable;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatManager extends TabFeature {

    private TABAdditions plinstance;
    private final TabAPI tab;
    private final Map<String,ChatFormat> formats = new HashMap<>();
    public final Map<TabPlayer,String> defformats = new HashMap<>();

    private final Pattern chatPartPattern = Pattern.compile("\\{(?<text>[^|]+)((\\|\\|(?<hover>[^|]+))(\\|\\|(?<click>[^|]+))?)?}");

    public boolean itemEnabled = true;
    public String itemMainHand = "[item]";
    public String itemOffHand = "[offhand]";
    public String itemOutput = "%item% x%amount%";
    public String itemOutputSingle = "%item%";
    public String itemOutputAir = "No Item";
    public boolean itemPermssion = false;

    public boolean mentionEnabled = true;
    public String mentionInput = "@%player%";
    public String mentionOutput = "&b";
    public String mentionSound = "BLOCK_NOTE_BLOCK_PLING";

    public Map<String,Map<String,Object>> customInteractions = new HashMap<>();

    public ChatCmds cmds;

    public boolean emojiEnabled = true;
    public boolean emojiPermission = false;
    public boolean emojiUntranslate = false;
    public String emojiOutput = "";
    public Map<String,String> emojis = new HashMap<>();

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
    public ChatFormat defFormat() {
        Map<String,String> map = new HashMap<>();

        map.put("text","{%tab_chatprefix% %tab_customchatname% %tab_chatsuffix%&7\u00bb &r%msg%}");
        return new ChatFormat("default", map);
    }

    @Override
    public void load() {
        plinstance = TABAdditions.getInstance();
        ConfigurationFile config = plinstance.getConfig(ConfigType.CHAT);
        for (Object format : config.getConfigurationSection("chat-formats").keySet())
            formats.put(format+"",new ChatFormat(format+"", config.getConfigurationSection("chat-formats."+format)));

        itemEnabled = config.getBoolean("item.enabled",true);
        itemMainHand = config.getString("item.mainhand","[item]");
        itemOffHand = config.getString("item.offhand","[offhand]");
        itemOutput = config.getString("item.output","%name% x%amount%");
        itemOutputSingle = config.getString("item.output-single","%name%");
        itemOutputAir = config.getString("item.output-air","No Item");
        itemPermssion = config.getBoolean("item.permission",false);

        customInteractions = config.getConfigurationSection("custom-interactions");

        cmds = new ChatCmds(this,config);

        mentionEnabled = config.getBoolean("mention.enabled",true);
        mentionInput = config.getString("mention.input","@%player%");
        mentionOutput = config.getString("mention.output","&b@%player%");
        mentionSound = config.getString("mention.sound","BLOCK_NOTE_BLOCK_PLING");

        emojiEnabled = config.getBoolean("emojis.enabled",true);
        emojiPermission = config.getBoolean("emojis.permission",false);
        emojiOutput = config.getString("emojis.output","");
        emojiUntranslate = config.getBoolean("emojis.block-without-permission",false);
        emojis = config.getConfigurationSection("emojis.list");

        cooldownTime = Long.parseLong(config.getInt("message-cooldown",0)+"");

        for (TabPlayer p : tab.getOnlinePlayers()) {
            p.loadPropertyFromConfig(this,"chatprefix");
            p.loadPropertyFromConfig(this,"customchatname",p.getName());
            p.loadPropertyFromConfig(this,"chatsuffix");
        }
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
        if (cooldownTime != 0 && !p.hasPermission("tabadditions.chat.bypass.cooldown"))
            cooldown.put(p,LocalDateTime.now());

        ChatFormat chatFormat = getFormat(p);

        tab.sendConsoleMessage(createmsg(p,msg,chatFormat.getText(),null).toLegacyText(), true);

        for (TabPlayer pl : tab.getOnlinePlayers()) {
            if (canSee(p,pl))
                pl.sendMessage(createmsg(p,msg,chatFormat.getText(),pl));
        }
        if (TABAdditions.getInstance().getPlatform().getType() == PlatformType.SPIGOT && Bukkit.getServer().getPluginManager().isPluginEnabled("DiscordSRV"))
            if (TABAdditions.getInstance().getConfig(ConfigType.CHAT).getBoolean("DiscordSRV-Support",true)) {
                DiscordSRV discord = DiscordSRV.getPlugin();
                if (canSee(p, null))
                    discord.processChatMessage(Bukkit.getServer().getPlayer(p.getUniqueId()), msg, discord.getMainChatChannel(), false);
                else if (getFormat(p).isViewConditionMet(p,null) && !discord.getOptionalChannel(chatFormat.getChannel()).equals(discord.getMainChatChannel())) discord.processChatMessage(Bukkit.getServer().getPlayer(p.getUniqueId()),msg, discord.getOptionalChannel(chatFormat.getChannel()),false);
            }
    }

    public IChatBaseComponent createmsg(TabPlayer p, String msg, String chatFormat, TabPlayer viewer) {
        List<String> codes = Arrays.asList("a", "b", "c", "d", "f", "k", "l", "m", "n", "o", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        for (String code : codes) {
            if (!p.hasPermission("tabadditions.chat.color.&" + code))
                msg = msg.replace("&" + code, "");
        }

        msg = pingcheck(p,msg,viewer);
        msg = new RGBUtils().applyFormats(msg, true);

        String format = removeSpaces(chatFormat);

        return compcheck(msg,format,p,viewer);
    }

    public IChatBaseComponent compcheck(String msg, String text, TabPlayer p, TabPlayer viewer) {
        text = plinstance.parsePlaceholders(text,p,viewer,p).replace("%msg%",msg);
        if (!text.startsWith("{")) text = "{"+text;
        if (!text.endsWith("}")) text = text+"}";
        text = textcheck(text,p,viewer);
        text = EnumChatFormat.color(text);
        Matcher m = chatPartPattern.matcher(text);
        List<IChatBaseComponent> list = new ArrayList<>();
        TextColor lastcolor = null;
        while (m.find()) {

            String txt = (lastcolor != null && !m.group("text").equals("[item]") ? "#"+lastcolor.getHexCode() : "") + m.group("text");

            String hover;
            try {hover = m.group("hover");}
            catch (Exception e) {hover = null;}
            String click;
            try {click = m.group("click");}
            catch (Exception e) {click = null;}

            IChatBaseComponent comp = IChatBaseComponent.optimizedComponent(txt);
            if (hover != null) comp = hovercheck(comp,hover,p,viewer,lastcolor);
            if (click != null) clickcheck(comp,click);

            lastcolor = getLastColor(comp);
            list.add(comp);
        }
        IChatBaseComponent finalcomp = new IChatBaseComponent("");

        finalcomp.setExtra(list);
        return finalcomp;
    }

    public String textcheck(String text,TabPlayer p, TabPlayer viewer) {
        Matcher m = chatPartPattern.matcher(text);

        while (m.find()) {
            String txtold = m.group("text");
            String txt = m.group("text");
            String hover = null;
            try {hover = m.group("hover");}
            catch (Exception ignored) {}
            String click = null;
            try {click = m.group("click");}
            catch (Exception ignored) {}
            String hoverclick = (hover != null ? "||"+hover : "") + (click != null ? "||"+click : "")+"}";
            if (itemEnabled && (!itemPermssion || p.hasPermission("tabadditions.chat.item"))) {
                txt = txt.replace(itemMainHand, hoverclick+"{[item]||item:mainhand}{");
                txt = txt.replace(itemOffHand, hoverclick+"{[item]||item:offhand}{");
            }

            if (emojiEnabled) txt = emojicheck(p,txt,hoverclick);

            for (String interaction : customInteractions.keySet()) {
                if (!customInteractions.get(interaction).containsKey("permission") || ((boolean) customInteractions.get(interaction).get("permission") && p.hasPermission("tabadditions.chat.interaction." + interaction))) {
                    txt = txt.replace(customInteractions.get(interaction).get("input")+"", hoverclick+removeSpaces(plinstance.parsePlaceholders(customInteractions.get(interaction).get("output")+"",p,viewer,p))+"{");
                }
            }
            text = text.replace(txtold,txt);
        }
        return text;
    }
    public IChatBaseComponent hovercheck(IChatBaseComponent comp, String hover, TabPlayer p, TabPlayer viewer, TextColor lastcolor) {
        if (hover == null || hover.equals("")) return comp;

        if (hover.startsWith("material:")) {
            Material mat = Material.getMaterial(hover.replace("material:", ""));
            if (mat == null) return comp;
            ItemStack item = new ItemStack(mat);
            comp.getModifier().onHoverShowItem(((TABAdditionsSpigot) plinstance.getPlugin()).itemStack(item));
            return comp;
        }

        if (hover.startsWith("item:")) {
            ItemStack item = getItem(hover,p);

            String itemtxt;
            if (item.getType() != Material.AIR) {
                String name = getItemName(item);
                if (item.getAmount() > 1)
                    itemtxt = itemOutput.replace("%name%",name).replace("%amount%",item.getAmount()+"");
                else itemtxt = itemOutputSingle.replace("%name%",name);
            } else itemtxt = itemOutputAir;

            if (comp.getText().replaceAll("^\\s+","").equals("[item]")) {
                comp = IChatBaseComponent.optimizedComponent((lastcolor == null ? "" : "#"+lastcolor.getHexCode())+ plinstance.parsePlaceholders(itemtxt,p,viewer,p));
            }
            comp.getModifier().onHoverShowItem(((TABAdditionsSpigot) plinstance.getPlugin()).itemStack(item));
            return comp;
        }
        comp.getModifier().onHoverShowText(IChatBaseComponent.optimizedComponent(plinstance.parsePlaceholders(hover,p,viewer,p)));
        return comp;
    }
    public void clickcheck(IChatBaseComponent comp, String click) {
        if (click == null || click.equals("")) return;

        if (click.startsWith("command:"))
            comp.getModifier().onClickRunCommand(click.replace("command:",""));
        if (click.startsWith("suggest:"))
            comp.getModifier().onClickSuggestCommand(click.replace("suggest:",""));
        if (click.startsWith("url:"))
            comp.getModifier().onClickOpenUrl(click.replace("url:","").replace(" ",""));
        if (click.startsWith("copy:"))
            comp.getModifier().onClick(ChatClickable.EnumClickAction.COPY_TO_CLIPBOARD,click.replace("copy:",""));

    }
    public String emojicheck(TabPlayer p, String msg, String hoverclick) {
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
            int counted = 0;
            String output = hoverclick+removeSpaces(emojiOutput.replace("%emojiraw%",emoji).replace("%emoji%",emojis.get(emoji)))+"{";
            for (String part : list) {
                if (list.indexOf(part)+1 == list.size() && counted == count)
                    msg += part;
                else {
                    msg += part + output;
                    counted++;
                }
            }
        }
        return msg;
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

    public ItemStack getItem(String str, TabPlayer p) {
        str = str.replace("item:","");
        Player player = (Player) p.getPlayer();

        if (str.equalsIgnoreCase("mainhand")) {
            try {return player.getInventory().getItemInMainHand();}
            catch (Exception e) {//noinspection deprecation
                return player.getInventory().getItemInHand();}
        }
        if (str.equalsIgnoreCase("offhand")) {
            try {return player.getInventory().getItemInOffHand();}
            catch (Exception e) {return new ItemStack(Material.AIR);}
        }
        try {
            int i = Integer.parseInt(str);
            ItemStack item = player.getInventory().getItem(i);
            if (item != null) return item;
        } catch (Exception ignored) {}

        return new ItemStack(Material.AIR);
    }
    public String getItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) return item.getItemMeta().getDisplayName();

        String type = item.getType().toString().replace("_", " ").toLowerCase();
        String type2 = "";
        List<String> typelist = new ArrayList<>(Arrays.asList(type.split(" ")));
        for (String str : typelist) {
            type2 = type2 + str.substring(0, 1).toUpperCase() + str.substring(1);
            if (typelist.indexOf(str) != typelist.size() - 1) type2 = type2 + " ";
        }
        return type2;
    }
    public TextColor getLastColor(IChatBaseComponent component) {
        if (component.getExtra() != null && !component.getExtra().isEmpty()) {
            List<IChatBaseComponent> list = new ArrayList<>(component.getExtra());
            Collections.reverse(list);
            for (IChatBaseComponent comp : list) {
                return getLastColor2(comp);
            }
        }
        return getLastColor2(component);
    }
    public TextColor getLastColor2(IChatBaseComponent component) {
        if (component.getText().contains("\u00A7")) {
            int i = component.getText().lastIndexOf("\u00A7");
            if (component.getText().chars().toArray().length == i+1) return null;
            char c = component.getText().charAt(i+1);
            if (EnumChatFormat.getByChar(c) != null)
                return new TextColor(EnumChatFormat.getByChar(c));
        }
        return component.getModifier().getColor();
    }

    public boolean canSee(TabPlayer sender, TabPlayer viewer) {
        if (sender == viewer) return true;
        if (viewer != null && !getFormat(sender).getChannel().equals(getFormat(viewer).getChannel())) return false;
        else if (viewer == null && !getFormat(sender).getChannel().equals("")) return false;
        return getFormat(sender).isViewConditionMet(sender, viewer);
    }
    public int countMatches(CharSequence str, CharSequence sub) {
        if (str != null &&  str.length() != 0 && sub != null && sub.length() != 0) {
            int count = 0;
            for(int idx = 0; (idx = str.toString().indexOf(sub.toString(),idx)) != -1; idx += sub.length())
                ++count;
            return count;
        } else return 0;
    }
    public String removeSpaces(String str) {
        return str.replace("{ ","{").replace(" }","}").replace(" || ","||");
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
        ConfigurationFile translation = TAB.getInstance().getConfiguration().getTranslation();

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
