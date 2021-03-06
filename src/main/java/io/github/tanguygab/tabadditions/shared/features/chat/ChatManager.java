package io.github.tanguygab.tabadditions.shared.features.chat;

import io.github.tanguygab.tabadditions.shared.ConfigType;
import io.github.tanguygab.tabadditions.shared.PlatformType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.spigot.TABAdditionsSpigot;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.YamlConfigurationFile;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.event.ChatEventListener;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.rgb.TextColor;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ChatManager implements ChatEventListener, Loadable, JoinEventListener {

    private TABAdditions plinstance;
    private static ChatManager instance;
    private final TabFeature feature;
    private final Map<String,ChatFormat> formats = new HashMap<>();
    public final Map<String,Boolean> conditions = new HashMap<>();

    public boolean mentionEnabled = true;
    public boolean mentionForEveryone = true;
    public String mentionInput = "@%player%";
    public String mentionOutput = "&b";

    public ChatManager(TabFeature feature) {
        feature.setDisplayName("&aChat");
        this.feature = feature;
        instance = this;
        load();
    }


    public static ChatManager getInstance() {
        return instance;
    }
    public ChatFormat getFormat(TabPlayer p) {
        String format = plinstance.getConfig(ConfigType.CHAT).getString("default-format","default");
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
            formats.put(format.toString(),new ChatFormat(format.toString(), config.getConfigurationSection("chat-formats."+format)));

        mentionEnabled = config.getBoolean("mention.enabled",true);
        mentionForEveryone = config.getBoolean("mention.output-for-everyone",true);
        mentionInput = config.getString("mention.input","@%player%");
        mentionOutput = config.getString("mention.output","&b@%player%");

        TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(500,"refreshing Chat props",feature, UsageType.REPEATING_TASK,() -> {
            for (TabPlayer p : TAB.getInstance().getPlayers()) {
                p.loadPropertyFromConfig("chatprefix");
                p.loadPropertyFromConfig("customchatname",p.getName());
                p.loadPropertyFromConfig("chatsuffix");
            }
        });
    }

    public boolean isInRange(TabPlayer sender,TabPlayer viewer,String range) {
        if (plinstance.getPlatform().getType() == PlatformType.BUNGEE) return true;
        int zone = (int) Math.pow(Integer.parseInt(range), 2);
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
        ChatFormat format = getFormat(p);
        IChatBaseComponent format2 = format.getText();
        TextColor oldColor = null;
        List<IChatBaseComponent> list = new ArrayList<>();

        //[item]
        if (TAB.getInstance().getPlayer("Tanguygab") != null && plinstance.getPlatform().getType() == PlatformType.SPIGOT && msg.contains("[item]")) {
            ItemStack item = ((Player) p.getPlayer()).getInventory().getItemInMainHand();
            if (item != null) {

                IChatBaseComponent itemmsg = new IChatBaseComponent("");
                List<IChatBaseComponent> msglist = new ArrayList<>();
                List<String> ar = new ArrayList<>(Arrays.asList(msg.split("\\[item]")));

                if (ar.isEmpty()) ar.add("");
                for (String txt2 : ar) {

                    msglist.add(IChatBaseComponent.fromColoredText(txt2));
                    if ((ar.size() == 1 || ar.indexOf(txt2) != ar.size() - 1)) {
                        IChatBaseComponent itemtxt = new IChatBaseComponent();
                        String type = item.getType().toString().replace("_", " ").toLowerCase();
                        itemtxt = itemtxt.setText(type);
                        itemtxt = itemtxt.onHoverShowItem(((TABAdditionsSpigot) plinstance.getPlugin()).itemStack(item));
                        p.sendMessage(itemtxt.getHoverValue() + "", false);
                        msglist.add(itemtxt);
                    }

                }

                itemmsg.setExtra(msglist);
                p.sendMessage(itemmsg);

            }
        }


        for (IChatBaseComponent comp : format2.getExtra()) {

            List<IChatBaseComponent> list2 = new ArrayList<>();
            for (IChatBaseComponent txt : comp.getExtra()) {
                String msg2 = plinstance.parsePlaceholders(txt.toRawText(), p).replaceAll("%msg%", msg);
                txt = IChatBaseComponent.fromColoredText(msg2).setColor(txt.getColor());

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
            IChatBaseComponent ppformat = pformat.clone();
            if (!mentionForEveryone && mentionEnabled && pl != p) ppformat = pingcheck(pl,ppformat);
            pl.sendMessage(ppformat);
        }

        return true;
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
    public TabFeature getFeatureType() {
        return feature;
    }

}
