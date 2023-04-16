package io.github.tanguygab.tabadditions.shared.features.chat;

import com.loohp.interactivechat.api.InteractiveChatAPI;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.TranslationFile;
import io.github.tanguygab.tabadditions.shared.features.chat.emojis.EmojiManager;
import io.github.tanguygab.tabadditions.shared.features.chat.mentions.MentionManager;
import io.github.tanguygab.tabadditions.spigot.TABAdditionsSpigot;
import me.neznamy.tab.shared.placeholders.RelationalPlaceholderImpl;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.chat.ChatClickable;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.chat.TextColor;
import me.neznamy.tab.shared.chat.rgb.RGBUtils;
import me.neznamy.tab.shared.config.ConfigurationFile;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatManager extends TabFeature implements JoinListener, CommandListener, Loadable, UnLoadable {

    private final TABAdditions plugin;
    private final TAB tab;
    private final ConfigurationFile config;
    private EmojiManager emojiManager;
    private MsgManager msgManager;
    private MentionManager mentionManager;

    private final Map<String,ChatFormat> formats = new HashMap<>();
    private ChatFormat defaultFormat;
    public final Map<TabPlayer,String> defformats = new HashMap<>();
    private final Pattern rgbPattern = Pattern.compile("#[0-9a-fA-F]{6}");
    public int msgPlaceholderStay;

    public RelationalPlaceholderImpl chatPlaceholder;

    private final Pattern chatPartPattern = Pattern.compile("\\{(?<text>[^}|]+)((\\|\\|(?<hover>[^}|]+)?)(\\|\\|(?<click>[^}|]+))?)?}");

    public boolean itemEnabled;
    public String itemMainHand;
    public String itemOffHand;
    public String itemOutput;
    public String itemOutputSingle;
    public String itemOutputAir ;
    public boolean itemPermssion;


    public Map<String,Map<String,Object>> customInteractions;

    public ChatCmds cmds;

    public List<String> toggleChat = new ArrayList<>();
    public boolean spySave;
    public boolean spyChannelsEnabled;
    public String spyChannelsOutput;
    public boolean spyViewConditionsEnabled;
    public String spyViewConditionsOutput;
    public List<String> spies = new ArrayList<>();

    public long cooldownTime;
    public Map<TabPlayer,LocalDateTime> cooldown = new HashMap<>();

    public boolean embedURLs;
    public boolean embedURLsAutoAddHttp;
    public String urlsOutput;
    public Pattern urlPattern = Pattern.compile("([&§][a-fA-Fk-oK-OrR0-9])?(?<url>(http(s)?:/.)?(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&/=]*))");
    public Pattern ipv4Pattern = Pattern.compile("(?:[0-9]{1,3}\\.){3}[0-9]{1,3}");

    public boolean filterEnabled;
    public String filterChar;
    public int filterFakeLength;
    public String filterOutput;
    public List<Pattern> filterPatterns = new ArrayList<>();
    public List<String> filterExempt;
    public Map<String,FormatCommand> commands;
    public boolean discordEnabled;
    public String discordPlugin;
    public String discordFormat;
    private boolean chatFromBukkitBridge;

    public ChatManager(ConfigurationFile config) {
        tab = TAB.getInstance();
        plugin = TABAdditions.getInstance();
        this.config = config;
        load();
    }

    @Override
    public String getFeatureName() {
        return "Chat";
    }

    public ChatFormat getFormat(TabPlayer p) {
        String format;
        if (defformats.containsKey(p))
            format = defformats.get(p);
        else format = config.getString("default-format","default");
        if (format.equalsIgnoreCase("")) return defaultFormat;

        ChatFormat f = formats.get(format);
        while (f != null && !f.isConditionMet(p)) {
            f = formats.get(f.getChildLayout());

            if (f == null) return defaultFormat;
        }
        return f;
    }

    @Override
    public void load() {
        defaultFormat = new ChatFormat("default", null, null, null, null, "{%prop-chatprefix% %prop-customchatname% %prop-chatsuffix%&7» &r%msg%||%time%}");
        Map<String,Map<String,String>> chatFormats = config.getConfigurationSection("chat-formats");
        chatFormats.forEach((format,cfg)-> formats.put(format + "", new ChatFormat(format,
                cfg.containsKey("condition") ? Condition.getCondition(cfg.get("condition")) : null,
                cfg.getOrDefault("if-condition-not-met",""),
                cfg.getOrDefault("channel",""),
                cfg.get("view-condition"),
                cfg.get("text")
        )));


        if (config.getBoolean("emojis.enabled",true))
            emojiManager = new EmojiManager(this,config.getString("emojis.output","{%emoji%%lastcolor%|| %emoji% &7%emojiraw% &d/emojis||command:/emojis}"),
                    config.getBoolean("emojis.block-without-permission",false),
                    config.getBoolean("emojis.auto-complete",true),
                    config.getConfigurationSection("emojis.categories"),
                    config.getBoolean("emojis./emojis",true),
                    config.getBoolean("emojis./toggleemoji",true)
            );
        if (config.getBoolean("msg.enabled",true))
            msgManager = new MsgManager(this,config.getString("msg.sender","{&7[&6&lMe &e➠ &6&l%viewer:prop-customchatname%&7] %msg%||%time%\\n\\n&fClick to reply to &6%viewer:prop-customchatname%&f.||suggest:/msg %player% }"),
                    config.getString("msg.viewer","{&7[&6&l%prop-customchatname% &e➠ &6&lMe&7] %msg%||%time%\\n\\n&fClick to reply to &6%prop-customchatname%&f.||suggest:/msg %player% }"),
                    config.getInt("msg.cooldown",0),
                    config.getStringList("msg./msg-aliases",Arrays.asList("tell","whisper","w","m")),
                    config.getBoolean("msg.msg-self",true),
                    config.getBoolean("msg./togglemsg",true),
                    config.getBoolean("msg./reply",true),
                    config.getBoolean("msg.save-last-sender-for-reply",true)
            );
        if (config.getBoolean("mention.enabled",true))
            mentionManager = new MentionManager(this,config.getString("mention.input","@%player%"),
                    config.getString("mention.output","&b@%player%"),
                    config.getString("mention.sound","BLOCK_NOTE_BLOCK_PLING"),
                    config.getBoolean("mention./togglemention",true),
                    config.getBoolean("mention.output-for-everyone",true),
                    config.getConfigurationSection("mention.custom-mentions")
            );

        cmds = new ChatCmds(this,config);

        msgPlaceholderStay = config.getInt("msg-placeholder-stay",3000);

        itemEnabled = config.getBoolean("item.enabled",true);
        itemMainHand = config.getString("item.mainhand","[item]");
        itemOffHand = config.getString("item.offhand","[offhand]");
        itemOutput = config.getString("item.output","%name% x%amount%");
        itemOutputSingle = config.getString("item.output-single","%name%");
        itemOutputAir = config.getString("item.output-air","No Item");
        itemPermssion = config.getBoolean("item.permission",false);

        customInteractions = config.getConfigurationSection("custom-interactions");


        spySave = config.getBoolean("socialspy.keep-after-reload",true);
        if (cmds.socialSpyEnabled && spySave) {
            spies.addAll(plugin.getPlayerData().getStringList("socialspy", new ArrayList<>()));
            plugin.getPlayerData().set("socialspy",null);
        }
        spyChannelsEnabled = config.getBoolean("socialspy.channels.spy",true);
        spyChannelsOutput = config.getString("socialspy.channels.output","{SocialSpy-Channel: %prop-customchatname% &8» %msg%||Channel: %channel%\n%time%}");
        spyViewConditionsEnabled = config.getBoolean("socialspy.view-conditions.spy",true);
        spyViewConditionsOutput = config.getString("socialspy.view-conditions.output","{SocialSpy-ViewCondition: %prop-customchatname% &8» %msg%||Condition: %condition%\n%time%}");

        cooldownTime = Long.parseLong(config.getInt("message-cooldown",0)+"");

        embedURLs = config.getBoolean("embed-urls.enabled",true);
        embedURLsAutoAddHttp = config.getBoolean("embed-urls.auto-add-http",false);
        urlsOutput = config.getString("embed-urls.output","{&8&l[&4Link&8&l]||&7URL: %url%\n\n&7Click to open||url:%url%}");

        filterEnabled = config.getBoolean("char-filter.enabled",true);
        filterChar = config.getString("char-filter.char-replacement","*");
        filterFakeLength = config.getInt("char-filter.fake-length",0);
        filterOutput = config.getString("char-filter.output","{%replacement%||Someone used a bad word!\n\nClick to see it anyways||suggest:%word%}");
        config.getStringList("char-filter.filter", new ArrayList<>()).forEach(filter->filterPatterns.add(Pattern.compile(filter)));
        filterExempt = config.getStringList("char-filter.exempt", new ArrayList<>());

        discordEnabled = config.getBoolean("discord.enabled",false);
        discordPlugin = config.getString("discord.plugin","DiscordSRV");
        discordFormat = config.getString("discord.format","%msg%");

        chatFromBukkitBridge = config.getBoolean("chat-from-bukkit-bridge",false);

        if (cmds.toggleChatEnabled) {
            toggleChat.addAll(plugin.getPlayerData().getStringList("togglechat", new ArrayList<>()));
            plugin.getPlayerData().set("togglechat",null);
        }

        commands = new HashMap<>();
        Map<String,Map<String,String>> commandsMap = config.getConfigurationSection("commands");
        commandsMap.forEach((cmd,cfg)-> commands.put(cmd,new FormatCommand(cfg.get("name"),cmd,formats.get(cfg.get("format")),Condition.getCondition(cfg.get("condition")),cfg.get("prefix"))));



        chatPlaceholder = (RelationalPlaceholderImpl) tab.getPlaceholderManager().registerRelationalPlaceholder("%rel_chat%",-1,(viewer, target)->"");


        for (TabPlayer p : tab.getOnlinePlayers()) onJoin(p);
    }



    public EmojiManager getEmojiManager() {
        return emojiManager;
    }

    public MsgManager getMsgManager() {
        return msgManager;
    }
    public MentionManager getMentionManager() {
        return mentionManager;
    }

    public boolean isBukkitBridgeChatEnabled() {
        return chatFromBukkitBridge;
    }

    public void unload() {
        if (mentionManager != null) mentionManager.unload();
        if (emojiManager != null) emojiManager.unload();
        ConfigurationFile data = plugin.getPlayerData();
        if (cmds.socialSpyEnabled && spySave) data.set("socialspy", spies);
        if (cmds.toggleChatEnabled) data.set("togglechat", toggleChat);
        if (cmds.ignoreEnabled) data.set("msg-ignore", cmds.ignored);
    }

    public void onChat(TabPlayer p, String msg) {
        if (plugin.isMuted(p)) return;
        if (cooldown.containsKey(p)) {
            long time = ChronoUnit.SECONDS.between(cooldown.get(p),LocalDateTime.now());
            if (time < cooldownTime) {
                p.sendMessage(plugin.getMsgs().getCooldown(cooldownTime-time), true);
                return;
            }
            cooldown.remove(p);
        }
        if (cooldownTime != 0 && !p.hasPermission("tabadditions.chat.bypass.cooldown"))
            cooldown.put(p,LocalDateTime.now());

        ChatFormat prefixFormat = null;
        for (FormatCommand format : commands.values()) {
            if (!format.checkMessage(msg)) continue;
            if (format.getCondition().isMet(p)) {
                prefixFormat = format.getFormat();
                msg = msg.substring(1);
            }
            break;
        }
        ChatFormat chatFormat = prefixFormat == null ? getFormat(p) : prefixFormat;
        tab.sendConsoleMessage(createmsg(p,msg,chatFormat.getText(),null).toLegacyText(), true);

        for (TabPlayer viewer : tab.getOnlinePlayers()) {
            if (toggleChat.contains(viewer.getName().toLowerCase()) || cmds.isIgnored(p,viewer)) continue;
            IChatBaseComponent comp = null;
            if (canSee(p,viewer,chatFormat)) comp = createmsg(p, msg, chatFormat.getText(), viewer);
            else if (isSpying(p,viewer).equals("channel")) comp = createmsg(p, msg, spyChannelsOutput,viewer);
            else if (isSpying(p,viewer).equals("view-condition")) comp = createmsg(p, msg, spyViewConditionsOutput,viewer);
            if (comp == null) continue;
            viewer.sendMessage(comp);
            String msgFormatted = plugin.toFlatText(comp);
            chatPlaceholder.updateValue(viewer,p,msgFormatted);

            TAB.getInstance().getCPUManager().runTaskLater(msgPlaceholderStay,this,"update %rel_chat% for "+viewer.getName()+" viewing "+p.getName(),()->{
                if (chatPlaceholder.getLastValue(viewer,p).equals(msgFormatted))
                    chatPlaceholder.updateValue(viewer,p,"");
            });
        }

        if (discordEnabled) {
            String msgToDiscord = createmsg(p,msg,discordFormat,null).toLegacyText();
            if (canSee(p,null,chatFormat))
                plugin.getPlatform().sendToDiscord(p.getUniqueId(),msgToDiscord,chatFormat.getChannel(),false,discordPlugin);
            else if (getFormat(p).isViewConditionMet(p,null))
                plugin.getPlatform().sendToDiscord(p.getUniqueId(),msgToDiscord,chatFormat.getChannel(),true,discordPlugin);
        }
    }

    public IChatBaseComponent createmsg(TabPlayer p, String msg, String chatFormat, TabPlayer viewer) {
        List<String> codes = Arrays.asList("a", "b", "c", "d", "e", "f", "k", "l", "m", "n", "o", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        for (String code : codes) {
            if (!p.hasPermission("tabadditions.chat.color.&" + code))
                msg = msg.replace("&" + code, "");
        }

        msg = RGBUtils.getInstance().applyFormats(msg);
        if (!p.hasPermission("tabadditions.chat.color.rgb")) {
            Matcher matcher = rgbPattern.matcher(msg);
            while (matcher.find())
                msg = msg.replace(matcher.group(),"");
        }

        String format = removeSpaces(chatFormat);

        if (plugin.getPlatform().isPluginEnabled("InteractiveChat"))
            try {msg = InteractiveChatAPI.markSender(msg,p.getUniqueId());}
            catch (IllegalStateException ignored) {}

        msg = msg.replace("{","<bracketleft>").replace("}","<bracketright>").replace("|","<bar>");
        return compcheck(msg,format,p,viewer);
    }

    public IChatBaseComponent compcheck(String msg, String text, TabPlayer p, TabPlayer viewer) {
        text = plugin.parsePlaceholders(text,p,viewer).replace("%channel%",getFormat(p).getChannel());
        if (!text.startsWith("{")) text = "{"+text;
        if (!text.endsWith("}")) text = text+"}";
        text = textcheck(text,p,viewer,msg);
        text = EnumChatFormat.color(text);
        Matcher m = chatPartPattern.matcher(text);
        List<IChatBaseComponent> list = new ArrayList<>();
        TextColor lastcolor = null;
        String lastMagic = "";
        while (m.find()) {
            String color =  (lastcolor != null ? "#"+lastcolor.getHexCode() : "")+lastMagic;
            String txt = (!m.group("text").equals("[item]") ? color : "") + m.group("text");
            txt = txt.replace("%lastcolor%",color);
            String hover;
            try {hover = m.group("hover");}
            catch (Exception e) {hover = null;}
            String click;
            try {click = m.group("click");}
            catch (Exception e) {click = null;}

            IChatBaseComponent comp = createComponent(txt);

            if (hover != null) comp = hovercheck(comp,hover.replace("%msg%",msg),p,viewer,lastcolor);
            if (click != null) clickcheck(comp,click.replace("%msg%",msg));


            if (comp.toLegacyText().lastIndexOf(EnumChatFormat.COLOR_STRING)+1 < comp.toLegacyText().length() && "KkLlMmNnOoRrXxRr".contains(comp.toLegacyText().charAt(comp.toLegacyText().lastIndexOf(EnumChatFormat.COLOR_STRING)+1)+"")) {
                List<Character> chars = new ArrayList<>();
                for (char c : txt.toCharArray())
                    chars.add(c);
                Collections.reverse(chars);

                for (int i = 0; i < chars.size() - 1; ++i) {
                    if (chars.get(i) == EnumChatFormat.COLOR_CHAR && "KkLlMmNnOoRrXxRr".indexOf(chars.get(i - 1)) > -1) {
                        lastMagic = EnumChatFormat.COLOR_STRING + Character.toLowerCase(chars.get(i - 1));
                        break;
                    }
                }
            }

            lastcolor = getLastColor(comp);
            list.add(comp);
        }
        IChatBaseComponent finalcomp = new IChatBaseComponent("");
        if (!list.isEmpty())
            finalcomp.setExtra(list);
        return finalcomp;
    }

    public String textcheck(String text,TabPlayer p, TabPlayer viewer, String msg) {
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

            if (mentionManager != null)
                txt = txt.replace("%msg%", mentionManager.process(msg,p,viewer,hoverclick));
            else txt = txt.replace("%msg%",msg);

            if (embedURLs) txt = urlcheck(txt,hoverclick);
            if (filterEnabled) txt = filtercheck(p,txt,hoverclick);

            if (itemEnabled && (!itemPermssion || p.hasPermission("tabadditions.chat.item"))) {
                if (!itemMainHand.equals(""))
                    txt = txt.replace(itemMainHand, hoverclick+"{[item]||item:mainhand}{");
                if (!itemOffHand.equals(""))
                    txt = txt.replace(itemOffHand, hoverclick+"{[item]||item:offhand}{");
            }

            if (emojiManager != null) txt = emojiManager.process(p,txt,hoverclick);

            for (String interaction : customInteractions.keySet()) {
                if (!customInteractions.get(interaction).containsKey("permission") || ((boolean) customInteractions.get(interaction).get("permission") && p.hasPermission("tabadditions.chat.interaction." + interaction))) {
                    if (!customInteractions.get(interaction).get("input").equals(""))
                        txt = txt.replace(customInteractions.get(interaction).get("input")+"", hoverclick+removeSpaces(plugin.parsePlaceholders(customInteractions.get(interaction).get("output")+"",p,viewer))+"{");
                }
            }
            text = text.replace(txtold,txt);
        }
        return text;
    }
    public IChatBaseComponent hovercheck(IChatBaseComponent comp, String hover, TabPlayer p, TabPlayer viewer, TextColor lastcolor) {
        if (hover == null || hover.equals("")) return comp;

        if (hover.startsWith("material:")) {
            if (plugin.getPlatform().isProxy()) return comp;
            Material mat = Material.getMaterial(hover.replace("material:", ""));
            if (mat == null) return comp;
            ItemStack item = new ItemStack(mat);
            comp.getModifier().onHoverShowItem(((TABAdditionsSpigot) plugin.getPlugin()).itemStack(item));
            return comp;
        }

        if (hover.startsWith("item:")) {
            if (plugin.getPlatform().isProxy()) return comp;
            ItemStack item = (ItemStack) getItem(hover,p);

            String itemtxt;
            if (item.getType() != Material.AIR) {
                String name = getItemName(item);
                if (item.getAmount() > 1)
                    itemtxt = itemOutput.replace("%name%",name).replace("%amount%",item.getAmount()+"");
                else itemtxt = itemOutputSingle.replace("%name%",name);
            } else itemtxt = itemOutputAir;

            if (plugin.toFlatText(comp).replaceAll("^\\s+","").equals("[item]")) {
                String color = lastcolor == null ? "" : "#"+lastcolor.getHexCode();
                comp = createComponent(color+ plugin.parsePlaceholders(itemtxt,p,viewer)+color);
            }
            comp.getModifier().onHoverShowItem(((TABAdditionsSpigot) plugin.getPlugin()).itemStack(item));
            return comp;
        }
        comp.getModifier().onHoverShowText(createComponent(hover));
        return comp;
    }
    public void clickcheck(IChatBaseComponent comp, String click) {
        if (click == null || click.equals("")) return;

        if (click.startsWith("command:"))
            comp.getModifier().onClickRunCommand(click.replace("command:",""));
        if (click.startsWith("suggest:"))
            comp.getModifier().onClickSuggestCommand(click.replace("suggest:",""));
        if (click.startsWith("url:")) {
            String url = click.replace("url:", "").trim();
            if (!url.startsWith("https://") && !url.startsWith("http://"))
                url = "http://"+url;
            comp.getModifier().onClickOpenUrl(url);
        }
        if (click.startsWith("copy:"))
            comp.getModifier().onClick(ChatClickable.EnumClickAction.COPY_TO_CLIPBOARD,click.replace("copy:",""));

    }
    public String urlcheck(String msg, String hoverclick) {
        String msg2 = msg.replaceAll("#[A-Fa-f0-9]{6}"," "); // removing RGB colors to avoid IPV4 check from killing them
        Matcher urlm = urlPattern.matcher(msg2);
        Matcher ipv4m = ipv4Pattern.matcher(msg2);


        while (urlm.find()) {
            String oldurl = urlm.group("url");
            String url = oldurl;
            if (embedURLsAutoAddHttp && !url.startsWith("https://") && !url.startsWith("http://"))
                url = "http://"+url;
            msg = msg.replace(oldurl,hoverclick+removeSpaces(urlsOutput.replace("%url%",url))+"{");
        }
        while (ipv4m.find()) {
            String ipv4 = ipv4m.group();
            msg = msg.replace(ipv4,hoverclick+removeSpaces(urlsOutput.replace("%url%",ipv4))+"{");

        }
        return msg;
    }
    public String filtercheck(TabPlayer p,String msg, String hoverclick) {
        if (p.hasPermission("tabadditions.chat.bypass.filter")) return msg;
        for (Pattern pattern : filterPatterns) {
            Matcher matcher = pattern.matcher(msg);
            Map<Integer, String> map = new HashMap<>();
            for (String bypass : filterExempt) {
                if (!msg.contains(bypass)) continue;
                Matcher m = Pattern.compile(bypass).matcher(msg);
                while (m.find()) {
                    map.put(m.start(), bypass);
                }
            }
            Map<String, Integer> posjumps = new HashMap<>();
            while (matcher.find()) {
                String word = matcher.group();
                StringBuilder wordreplaced = new StringBuilder();
                int i = filterFakeLength < 1 ? word.length() : filterFakeLength;
                wordreplaced.append(filterChar.repeat(i));
                int posjump = posjumps.getOrDefault(word,0);
                String output = hoverclick+removeSpaces(filterOutput.replace("%word%",word).replace("%replacement%", wordreplaced.toString()))+"{";

                if (map.isEmpty()) {
                    msg = msg.replace(word,output);
                } else {
                    for (int pos : map.keySet()) {
                        if (map.get(pos).length() > word.length() && pos <= matcher.start() && pos + map.get(pos).length() > matcher.start())
                            continue;
                        StringBuilder sb = new StringBuilder(msg);
                        sb.replace(matcher.start() + posjump, matcher.end() + posjump, output);
                        msg = sb.toString();

                        posjumps.put(word,posjump+output.length()-word.length());
                    }
                }
            }
        }
        return msg;
    }

    public Object getItem(String str, TabPlayer p) {
        str = str.replace("item:","");
        Player player = (Player) p.getPlayer();

        if (str.equalsIgnoreCase("mainhand")) {
            if (tab.getServerVersion().getMinorVersion() >= 9)
                return player.getInventory().getItemInMainHand();
            else
                //noinspection deprecation
                return player.getInventory().getItemInHand();
        }
        if (str.equalsIgnoreCase("offhand")) {
            if (tab.getServerVersion().getMinorVersion() >= 9)
                return player.getInventory().getItemInOffHand();
            else
                return new ItemStack(Material.AIR);
        }
        try {
            int i = Integer.parseInt(str);
            ItemStack item = player.getInventory().getItem(i);
            if (item != null) return item;
        } catch (Exception ignored) {}

        return new ItemStack(Material.AIR);
    }
    public String getItemName(Object i) {
        ItemStack item = (ItemStack) i;
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) return item.getItemMeta().getDisplayName();

        String type = item.getType().toString().replace("_", " ").toLowerCase();
        StringBuilder type2 = new StringBuilder();
        List<String> typelist = new ArrayList<>(Arrays.asList(type.split(" ")));
        for (String str : typelist) {
            type2.append(str.substring(0, 1).toUpperCase()).append(str.substring(1));
            if (typelist.indexOf(str) != typelist.size() - 1) type2.append(" ");
        }
        return type2.toString();
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
        if (component.getText().contains("§")) {
            int i = component.getText().lastIndexOf("§");
            if (component.getText().toCharArray().length == i+1) return null;
            char c = component.getText().charAt(i+1);
            if (EnumChatFormat.getByChar(c) != null && EnumChatFormat.getByChar(c).getHexCode() != null)
                return new TextColor(EnumChatFormat.getByChar(c));
            if ("KkLlMmNnOoRrXxRr".contains(c+"")) {
                StringBuilder sb = new StringBuilder(component.getText());
                sb.setCharAt(sb.lastIndexOf("§"),' ');
                int i2 = sb.lastIndexOf("§");
                if (sb.toString().toCharArray().length == i2+1) return null;
                char c2 = sb.charAt(i2+1);
                if (EnumChatFormat.getByChar(c) != null && EnumChatFormat.getByChar(c).getHexCode() != null)
                    return new TextColor(EnumChatFormat.getByChar(c2));

            }
        }
        return component.getModifier().getColor();
    }

    public boolean canSee(TabPlayer sender, TabPlayer viewer, ChatFormat f) {
        if (sender == viewer) return true;
        if (viewer == null) return f.getChannel().equals("") && !f.hasViewCondition();
        if (!f.getChannel().equals(getFormat(viewer).getChannel())) return false;
        return f.isViewConditionMet(sender, viewer);
    }
    public String isSpying(TabPlayer sender, TabPlayer viewer) {
        if (!getFormat(sender).getChannel().equals(getFormat(viewer).getChannel()) && spyChannelsEnabled && spies.contains(viewer.getName().toLowerCase())) return "channel";
        if (!getFormat(sender).isViewConditionMet(sender, viewer) && spyViewConditionsEnabled && spies.contains(viewer.getName().toLowerCase())) return "view-condition";
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
    public IChatBaseComponent createComponent(String str) {
        return IChatBaseComponent.fromColoredText(str.replace("<bracketleft>","{").replace("<bracketright>","}").replace("<bar>","|"));
    }

    @Override
    public void onJoin(TabPlayer p) {
        if (plugin.getPlatform().supportsChatSuggestions() && emojiManager != null && emojiManager.isAutoCompleteEnabled())
            emojiManager.loadAutoComplete(p);
    }

    @Override
    public boolean onCommand(TabPlayer p, String msg) {
        msg = msg.replaceFirst("/","");
        TranslationFile translation = plugin.getMsgs();

        FormatCommand cmd = commands.get(msg);
        if (cmd == null) return cmds.execute(p,msg);

        if (cmd.getCondition().isMet(p)) {
            String name = cmd.getName();
            if (defformats.containsKey(p)) {
                defformats.remove(p);
                p.sendMessage(translation.getCmdLeave(name), true);
            }
            else {
                defformats.put(p, cmd.getFormat().getName());
                p.sendMessage(translation.getCmdJoin(name), true);
            }
        } else p.sendMessage(translation.NO_PERMISSIONS, true);
        return true;
    }

}
