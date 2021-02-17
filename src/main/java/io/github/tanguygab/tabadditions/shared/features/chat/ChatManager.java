package io.github.tanguygab.tabadditions.shared.features.chat;

import io.github.tanguygab.tabadditions.shared.ConfigType;
import io.github.tanguygab.tabadditions.shared.PlatformType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.spigot.TABAdditionsSpigot;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.event.ChatEventListener;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.rgb.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ChatManager implements ChatEventListener, Loadable, JoinEventListener {

    private TABAdditions plinstance;
    private static ChatManager instance;
    private final TabFeature feature;
    private final Map<String,ChatFormat> formats = new HashMap<>();
    public final Map<String,Boolean> conditions = new HashMap<>();

    public ChatManager(TabFeature feature) {
        feature.setDisplayName("Chat");
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
    public boolean isInRange(TabPlayer sender,TabPlayer viewer,String range) {
        if (plinstance.getPlatform().getType() == PlatformType.BUNGEE) return true;
        int zone = (int) Math.pow(Integer.parseInt(range), 2);
        return sender.getWorldName().equals(viewer.getWorldName()) && ((Player) sender.getPlayer()).getLocation().distanceSquared(((Player) viewer.getPlayer()).getLocation()) < zone;
    }

    public ChatFormat defFormat() {
        Map<String,Object> map = new HashMap<>();
        Map<String,Object> components = new HashMap<>();
        Map<String,Object> text = new HashMap<>();

        text.put("text","%tab_chatprefix% %tab_customchatname% %tab_chatsuffix%&7» &r%msg%");
        components.put("text",text);
        map.put("components",components);
        return new ChatFormat("default", map);
    }

    @Override
    public boolean onChat(TabPlayer p, String msg) {
        ChatFormat format = getFormat(p);
        IChatBaseComponent format2 = format.getText();
        TextColor oldColor = null;
        List<IChatBaseComponent> list = new ArrayList<>();

        //[item]
        if (plinstance.getPlatform().getType() == PlatformType.SPIGOT && msg.contains("[item]")) {
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

        TAB.getInstance().getPlatform().sendConsoleMessage(format2+"", true);
        TAB.getInstance().getPlatform().sendConsoleMessage(format2.toLegacyText(), true);
        for (TabPlayer pl : TAB.getInstance().getPlayers())
            pl.sendMessage(format2);
        return true;
    }


    @Override
    public void load() {
        plinstance = TABAdditions.getInstance();
        for (Object format : plinstance.getConfig(ConfigType.CHAT).getConfigurationSection("chat-formats").keySet())
            formats.put(format.toString(),new ChatFormat(format.toString(), plinstance.getConfig(ConfigType.CHAT).getConfigurationSection("chat-formats."+format)));

        for (TabPlayer p : TAB.getInstance().getPlayers()) {
            p.loadPropertyFromConfig("chatprefix");
            p.loadPropertyFromConfig("customchatname",p.getName());
            p.loadPropertyFromConfig("chatsuffix");
        }
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
