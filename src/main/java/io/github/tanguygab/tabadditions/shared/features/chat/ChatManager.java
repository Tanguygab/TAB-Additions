package io.github.tanguygab.tabadditions.shared.features.chat;

import github.scarsz.discordsrv.DiscordSRV;
import io.github.tanguygab.tabadditions.shared.ConfigType;
import io.github.tanguygab.tabadditions.shared.PlatformType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.TAFeature;
import io.github.tanguygab.tabadditions.spigot.TABAdditionsSpigot;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.YamlConfigurationFile;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.event.ChatEventListener;
import me.neznamy.tab.shared.features.types.event.CommandListener;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.rgb.RGBUtils;
import me.neznamy.tab.shared.rgb.TextColor;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ChatManager implements ChatEventListener, Loadable, JoinEventListener, CommandListener {

    private TABAdditions plinstance;
    private static ChatManager instance;
    private final Map<String,ChatFormat> formats = new HashMap<>();

    public boolean mentionEnabled = true;
    public boolean mentionForEveryone = true;
    public String mentionInput = "@%player%";
    public String mentionOutput = "&b";
    public final Map<TabPlayer,String> defformats = new HashMap<>();

    public ChatManager() {
        instance = this;
        load();
    }


    public static ChatManager getInstance() {
        return instance;
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
        YamlConfigurationFile config = plinstance.getConfig(ConfigType.CHAT);
        for (Object format : config.getConfigurationSection("chat-formats").keySet())
            formats.put(format+"",new ChatFormat(format+"", config.getConfigurationSection("chat-formats."+format)));

        mentionEnabled = config.getBoolean("mention.enabled",true);
        mentionForEveryone = config.getBoolean("mention.output-for-everyone",true);
        mentionInput = config.getString("mention.input","@%player%");
        mentionOutput = config.getString("mention.output","&b@%player%");

        TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(500,"refreshing Chat props", TAFeature.CHAT, UsageType.REPEATING_TASK,() -> {
            for (TabPlayer p : TAB.getInstance().getPlayers()) {
                p.loadPropertyFromConfig("chatprefix");
                p.loadPropertyFromConfig("customchatname",p.getName());
                p.loadPropertyFromConfig("chatsuffix");
            }
        });
    }

    public boolean isInRange(TabPlayer sender,TabPlayer viewer,int range) {
        if (plinstance.getPlatform().getType() == PlatformType.BUNGEE) return true;
        int zone = (int) Math.pow(range, 2);
        return sender.getWorldName().equals(viewer.getWorldName()) && ((Player) sender.getPlayer()).getLocation().distanceSquared(((Player) viewer.getPlayer()).getLocation()) < zone;
    }

    public ChatFormat defFormat() {
        Map<String,Object> map = new HashMap<>();
        Map<String,Object> components = new HashMap<>();
        Map<String,Object> text = new HashMap<>();

        text.put("text","%tab_chatprefix% %tab_customchatname% %tab_chatsuffix%&7\u00bb &r%msg%");
        components.put("text",text);
        map.put("components",components);
        return new ChatFormat("default", map);
    }

    @Override
    public boolean onChat(TabPlayer p, String msg, boolean cancelled) {
        if (cancelled) return true;

        List<String> codes = Arrays.asList("a", "b", "c", "d", "f", "k", "l", "m", "n", "o", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        for (String code : codes) {
            if (!p.hasPermission("tabadditions.chat.color.&" + code))
                msg = msg.replaceAll("&" + code, "");
        }
        msg = new RGBUtils().applyFormats(msg);
        if (!p.hasPermission("tabadditions.chat.color.rgb"))
            msg = msg.replaceAll("#[0-9a-fA-F]{6}","");

        ChatFormat format = getFormat(p);
        IChatBaseComponent format2 = format.getText();
        TextColor oldColor = null;
        List<IChatBaseComponent> list = new ArrayList<>();

        for (IChatBaseComponent comp : format2.getExtra()) {

            List<IChatBaseComponent> list2 = new ArrayList<>();
            for (IChatBaseComponent txt : comp.getExtra()) {
                if (txt.toRawText().contains("%msg%") && plinstance.getPlatform().getType() == PlatformType.SPIGOT && msg.contains("[item]")) {
                    txt = itemcheck(p, txt, msg);
                } else {
                    String msg2 = plinstance.parsePlaceholders(txt.toRawText(), p).replaceAll("%msg%", msg);
                    txt = IChatBaseComponent.fromColoredText(msg2).setColor(txt.getColor());
                }

                TextColor color = null;
                for (IChatBaseComponent c : txt.getExtra()) {
                    if (c.getColor() != null) {
                        color = c.getColor();
                        break;
                    }
                    if (txt.getColor() != null) {
                        color = txt.getColor();
                        break;
                    }
                }
                if (oldColor != null && txt.getColor() == null) {
                    txt.setColor(oldColor);
                }
                if (color != null)
                    oldColor = color;

                list2.add(txt);
            }
            comp.setExtra(list2);
            if (comp.getHoverValue() != null) {
                String txt = plinstance.parsePlaceholders(comp.getHoverValue() + "", p).replaceAll("%msg%", msg);
                IChatBaseComponent hover = IChatBaseComponent.fromColoredText(txt);
                comp.onHoverShowText(hover);
            }
            if (comp.getClickValue() != null) {
                String txt = plinstance.parsePlaceholders(comp.getClickValue() + "", p).replaceAll("%msg%", msg);
                comp.onClickSuggestCommand(txt);
            }
            list.add(comp);
        }
        format2.setExtra(list);

        TAB.getInstance().getPlatform().sendConsoleMessage(format2.toLegacyText(), true);


        IChatBaseComponent pformat = format2;
        if (mentionForEveryone && mentionEnabled)
            for (TabPlayer pl : TAB.getInstance().getPlayers())
                if (pl != p)
                    pformat = pingcheck(pl,pformat);
        for (TabPlayer pl : TAB.getInstance().getPlayers()) {
            if (canSee(p,pl)) {
                IChatBaseComponent ppformat = pformat.clone();
                if (!mentionForEveryone && mentionEnabled && pl != p) ppformat = pingcheck(pl, ppformat);
                pl.sendMessage(ppformat);
            }
        }
        if (TABAdditions.getInstance().getPlatform().getType() == PlatformType.SPIGOT && Bukkit.getServer().getPluginManager().isPluginEnabled("DiscordSRV"))
            if (TABAdditions.getInstance().getConfig(ConfigType.CHAT).getBoolean("DiscordSRV-Support",true)) {
                DiscordSRV discord = DiscordSRV.getPlugin();
                if (canSee(p, null))
                    discord.processChatMessage(Bukkit.getServer().getPlayer(p.getUniqueId()), msg, discord.getMainChatChannel(), false);
                else if (getFormat(p).isViewConditionMet(p,null) && !discord.getOptionalChannel(format.getChannel()).equals(discord.getMainChatChannel())) discord.processChatMessage(Bukkit.getServer().getPlayer(p.getUniqueId()),msg, discord.getOptionalChannel(format.getChannel()),false);
            }

        return true;
    }

    public boolean canSee(TabPlayer sender, TabPlayer viewer) {
        if (sender == viewer) return true;
        if (viewer != null && !getFormat(sender).getChannel().equals(getFormat(viewer).getChannel())) return false;
        else if (viewer == null && !getFormat(sender).getChannel().equals("")) return false;
        return getFormat(sender).isViewConditionMet(sender, viewer);
    }

    public IChatBaseComponent itemcheck(TabPlayer p, IChatBaseComponent comp, String msg) {

        List<IChatBaseComponent> msglist = new ArrayList<>();
        String[] list = TABAdditions.getInstance().parsePlaceholders(comp.getText(),p).split("%msg%");
        msglist.add(new IChatBaseComponent(list[0]));


        ItemStack item = ((Player) p.getPlayer()).getInventory().getItemInMainHand();

        IChatBaseComponent itemmsg = new IChatBaseComponent("");
        List<String> ar = new ArrayList<>(Arrays.asList(msg.split("\\[item]")));
        int itemcount = StringUtils.countMatches(msg,"[item]");

        if (ar.isEmpty()) ar.add("");
        for (String txt2 : ar) {
            IChatBaseComponent txt3 = IChatBaseComponent.fromColoredText(txt2);
            msglist.add(txt3);

            if (itemcount != 0) {
                IChatBaseComponent itemtxt = new IChatBaseComponent();
                String type = item.getType().toString().replace("_", " ").toLowerCase();
                String type2 = "";
                List<String> typelist = new ArrayList<>(Arrays.asList(type.split(" ")));
                for (String str : typelist) {
                    type2 = type2 + str.substring(0, 1).toUpperCase() + str.substring(1);
                    if (typelist.indexOf(str) != typelist.size() - 1) type2 = type2 + " ";
                }
                itemtxt = itemtxt.setText(type2);
                itemtxt = itemtxt.onHoverShowItem(((TABAdditionsSpigot) plinstance.getPlugin()).itemStack(item));
                msglist.add(itemtxt);
                itemcount = itemcount-1;
            }

        }

        if (list.length > 1) msglist.add(new IChatBaseComponent(list[1]));
        itemmsg.setExtra(msglist);
        comp.setExtra(msglist);
        comp.setText("");
        return comp;
    }
    public IChatBaseComponent pingcheck(TabPlayer p, IChatBaseComponent msg) {
        if (msg.getExtra() != null && !msg.getExtra().isEmpty())
            for (IChatBaseComponent comp : msg.getExtra()) {
                if (comp.getExtra() != null && !comp.getExtra().isEmpty()) {
                    for (IChatBaseComponent subcomp : comp.getExtra()) {
                        if (subcomp.getExtra() != null && !subcomp.getExtra().isEmpty()) {
                            for (IChatBaseComponent subcomp2 : comp.getExtra()) {
                                if (subcomp2.getExtra() != null && !subcomp2.getExtra().isEmpty()) {
                                    for (IChatBaseComponent subcomp3 : subcomp2.getExtra()) {
                                        if (subcomp3.getText().toLowerCase().contains(TABAdditions.getInstance().parsePlaceholders(mentionInput,p).toLowerCase())) {
                                            subcomp3.setText(subcomp3.getText().replaceAll("(?i)"+TABAdditions.getInstance().parsePlaceholders(mentionInput,p), TABAdditions.getInstance().parsePlaceholders(mentionOutput, p)));
                                            if (TABAdditions.getInstance().getPlatform().getType().equals(PlatformType.SPIGOT)) {
                                                Player player = (Player) p.getPlayer();
                                                player.playNote(player.getLocation(), Instrument.PLING, Note.flat(1, Note.Tone.A));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        return msg;
    }


    @Override
    public void unload() {

    }

    @Override
    public void onJoin(TabPlayer p) {
        p.loadPropertyFromConfig("chatprefix");
        p.loadPropertyFromConfig("customchatname",p.getName());
        p.loadPropertyFromConfig("chatsuffix");
    }

    @Override
    public Object getFeatureType() {
        return TAFeature.CHAT;
    }

    @Override
    public boolean onCommand(TabPlayer p, String msg) {
        msg = msg.replaceFirst("/","");
        YamlConfigurationFile config = plinstance.getConfig(ConfigType.CHAT);
        if (config.getConfigurationSection("commands") == null || !config.getConfigurationSection("commands").containsKey(msg))
            return false;
        Map<String,String> cmd = (Map<String, String>) config.getConfigurationSection("commands").get(msg);
        String condition = cmd.get("condition")+"";
        String format = cmd.get("format")+"";
        String name = cmd.get("name")+"";
        if (!plinstance.isConditionMet(condition,p))
            p.sendMessage("&cYou can't do that!",true);
        else {
            if (defformats.containsKey(p)) {
                defformats.remove(p);
                p.sendMessage("&7You left " + name + "!", true);
            }
            else {
                defformats.put(p, format);
                p.sendMessage("&7You joined " + name + "!", true);
            }
        }
        return true;
    }
}
