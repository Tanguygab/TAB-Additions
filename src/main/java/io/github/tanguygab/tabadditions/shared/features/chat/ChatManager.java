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

    public boolean itemEnabled;
    public String itemMainHand;
    public String itemOffHand;
    public String itemOutput;
    public String itemOutputSingle;
    public String itemOutputAir ;
    public boolean itemPermssion;

    public boolean mentionEnabled;
    public String mentionInput;
    public String mentionOutput;
    public String mentionSound;

    public Map<String,Map<String,Object>> customInteractions;

    public ChatCmds cmds;

    public boolean emojiEnabled;
    public boolean emojiPermission;
    public boolean emojiUntranslate;
    public String emojiOutput;
    public Map<String,String> emojis;

    public boolean spySave;
    public boolean spyChannelsEnabled;
    public String spyChannelsOutput;
    public boolean spyViewConditionsEnabled;
    public String spyViewConditionsOutput;
    public List<TabPlayer> spies = new ArrayList<>();

    public long cooldownTime;
    public Map<TabPlayer,LocalDateTime> cooldown = new HashMap<>();

    public boolean embedURLs;
    public String urlsOutput;
    public Pattern urlPattern = Pattern.compile("(http(s)?:/.)?(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}( ?\\. ?| ?\\(?dot\\)? ?)[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&/=]*)");
    public Pattern ipv4Pattern = Pattern.compile("(?:[0-9]{1,3}( ?\\. ?|\\(?dot\\)?)){3}[0-9]{1,3}");

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

        spySave = config.getBoolean("socialspy.keep-after-reload",true);
        if (cmds.socialspyEnabled && spySave) {
            cmds.getPlayerData().getStringList("socialspy", new ArrayList<>()).forEach(p->spies.add(plinstance.getPlayer(p)));
            cmds.getPlayerData().set("socialspy",null);
        }
        spyChannelsEnabled = config.getBoolean("socialspy.channels.spy",true);
        spyChannelsOutput = config.getString("socialspy.channels.output","{SocialSpy-Channel: %prop-customchatname% &8» %msg%||Channel: %channel%\\n%time%}");
        spyViewConditionsEnabled = config.getBoolean("socialspy.view-conditions.spy",true);
        spyViewConditionsOutput = config.getString("socialspy.view-conditions.output","{SocialSpy-ViewCondition: %prop-customchatname% &8» %msg%||Condition: %condition%\\n%time%}");

        cooldownTime = Long.parseLong(config.getInt("message-cooldown",0)+"");

        embedURLs = config.getBoolean("embed-urls.enabled",true);
        urlsOutput = config.getString("embed-urls.output","{&8&l[&4Link&8&l]||&7URL: %url% \\n \\n&7Click to open||url:%url%}");

        for (TabPlayer p : tab.getOnlinePlayers()) {
            p.loadPropertyFromConfig(this,"chatprefix");
            p.loadPropertyFromConfig(this,"customchatname",p.getName());
            p.loadPropertyFromConfig(this,"chatsuffix");
        }
    }

    @Override
    public void unload() {
        if (cmds.socialspyEnabled && spySave) {
            List<String> list = new ArrayList<>();
            spies.forEach(p -> list.add(p.getName()));
            cmds.getPlayerData().set("socialspy", list);
        }
    }

    public void onChat(TabPlayer p, String msg) {
        if (plinstance.isMuted(p)) return;
        if (cooldown.containsKey(p)) {
            long time = ChronoUnit.SECONDS.between(cooldown.get(p),LocalDateTime.now());
            if (time < cooldownTime) {
                p.sendMessage(TABAdditions.getInstance().getTABConfigs().getTranslation()
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
                pl.sendMessage(createmsg(p, msg, chatFormat.getText(), pl));
            else if (isSpying(p,pl).equals("channel"))
                pl.sendMessage(createmsg(p, msg, spyChannelsOutput,pl));
            else if (isSpying(p,pl).equals("view-condition"))
                pl.sendMessage(createmsg(p, msg, spyViewConditionsOutput,pl));
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
        text = plinstance.parsePlaceholders(text,p,viewer,p,this)
                .replace("%msg%",msg)
                .replace("%channel%",getFormat(p).getChannel())
                .replace("%condition%",getFormat(p).getViewCondition());
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
                if (!itemMainHand.equals(""))
                    txt = txt.replace(itemMainHand, hoverclick+"{[item]||item:mainhand}{");
                if (!itemOffHand.equals(""))
                    txt = txt.replace(itemOffHand, hoverclick+"{[item]||item:offhand}{");
            }

            if (emojiEnabled) txt = emojicheck(p,txt,hoverclick);
            if (embedURLs) txt = urlcheck(p,txt,hoverclick);

            for (String interaction : customInteractions.keySet()) {
                if (!customInteractions.get(interaction).containsKey("permission") || ((boolean) customInteractions.get(interaction).get("permission") && p.hasPermission("tabadditions.chat.interaction." + interaction))) {
                    if (!customInteractions.get(interaction).get("input").equals(""))
                        txt = txt.replace(customInteractions.get(interaction).get("input")+"", hoverclick+removeSpaces(plinstance.parsePlaceholders(customInteractions.get(interaction).get("output")+"",p,viewer,p,this))+"{");
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
                comp = IChatBaseComponent.optimizedComponent((lastcolor == null ? "" : "#"+lastcolor.getHexCode())+ plinstance.parsePlaceholders(itemtxt,p,viewer,p,this));
            }
            comp.getModifier().onHoverShowItem(((TABAdditionsSpigot) plinstance.getPlugin()).itemStack(item));
            return comp;
        }
        comp.getModifier().onHoverShowText(IChatBaseComponent.optimizedComponent(hover));
        return comp;
    }
    public void clickcheck(IChatBaseComponent comp, String click) {
        if (click == null || click.equals("")) return;

        if (click.startsWith("command:"))
            comp.getModifier().onClickRunCommand(click.replace("command:",""));
        if (click.startsWith("suggest:"))
            comp.getModifier().onClickSuggestCommand(click.replace("suggest:",""));
        if (click.startsWith("url:"))
            comp.getModifier().onClickOpenUrl(click.replace("url:","").trim());
        if (click.startsWith("copy:"))
            comp.getModifier().onClick(ChatClickable.EnumClickAction.COPY_TO_CLIPBOARD,click.replace("copy:",""));

    }
    public String emojicheck(TabPlayer p, String msg, String hoverclick) {
        for (String emoji : emojis.keySet()) {
            int count = countMatches(msg,emoji);
            if (count == 0 || emoji.equals("")) continue;
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
    public String urlcheck(TabPlayer p, String msg, String hoverclick) {
        Matcher urlm = urlPattern.matcher(msg);
        Matcher ipv4m = ipv4Pattern.matcher(msg);

        while (urlm.find()) {
            String url = urlm.group();
            msg = msg.replace(url,hoverclick+removeSpaces(urlsOutput.replace("%url%",url))+"{");
        }
        while (ipv4m.find()) {
            String ipv4 = ipv4m.group();
            msg = msg.replace(ipv4,hoverclick+removeSpaces(urlsOutput.replace("%url%","http:"+ipv4))+"{");
        }
        return msg;
    }

    public String pingcheck(TabPlayer p, String msg, TabPlayer viewer) {
        String input = TABAdditions.getInstance().parsePlaceholders(mentionInput,p,viewer,viewer,this);
        if (input.equals("")) return msg;
        if (msg.toLowerCase().contains(input.toLowerCase())) {
            msg = (msg.replaceAll("(?i)" + input, TABAdditions.getInstance().parsePlaceholders(mentionOutput, p, viewer, viewer,this)));
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
        if (viewer == null) return getFormat(sender).getChannel().equals("");
        if (!getFormat(sender).getChannel().equals(getFormat(viewer).getChannel())) return false;
        return getFormat(sender).isViewConditionMet(sender, viewer);
    }
    public String isSpying(TabPlayer sender, TabPlayer viewer) {
        if (!getFormat(sender).getChannel().equals(getFormat(viewer).getChannel()) && spyChannelsEnabled && spies.contains(viewer)) return "channel";
        if (!getFormat(sender).isViewConditionMet(sender, viewer) && spyViewConditionsEnabled && spies.contains(viewer)) return "view-condition";
        return "";
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
        ConfigurationFile translation = TABAdditions.getInstance().getTABConfigs().getTranslation();

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
