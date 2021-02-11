package io.github.tanguygab.tabadditions.shared;

import io.github.tanguygab.tabadditions.shared.features.chat.ChatFormat;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatManager;
import io.github.tanguygab.tabadditions.shared.features.commands.TitleCmd;
import io.github.tanguygab.tabadditions.shared.features.layouts.LayoutManager;
import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.rgb.TextColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SharedEvents {

    public static void JoinEvent(String name) {
    	TABAdditions.getInstance().getPlatform().AsyncTask(()->{
            TabPlayer p = TABAPI.getPlayer(name);
            TABAdditions.getInstance().loadProps(p);

            if (TABAdditions.getInstance().actionbarsEnabled) {
                String actionbar = TABAdditions.getInstance().actionbarConfig.getString("bars." + p.getProperty("actionbar").get(),"");
                actionbar = TABAdditions.getInstance().parsePlaceholders(actionbar,p);
                p.sendCustomPacket(new PacketPlayOutChat(actionbar, PacketPlayOutChat.ChatMessageType.GAME_INFO));
            }
            if (TABAdditions.getInstance().titlesEnabled) {
                Map<String,String> tSection = TABAdditions.getInstance().titleConfig.getConfigurationSection("titles." + p.getProperty("title").get());
                if (tSection != null) {
                    List<Object> titleProperties = new ArrayList<>();
                    for (Object property : tSection.keySet())
                        titleProperties.add(tSection.get(property));
                    new TitleCmd(p, new String[]{}, titleProperties);
                }
            }
            if (TABAdditions.getInstance().layoutEnabled && !TABAdditions.getInstance().checkBedrock(p)) {
                LayoutManager lm = LayoutManager.getInstance();
                lm.toAdd.put(p,lm.getLayout(p));
            }
            if (TABAdditions.getInstance().nametagInRange != 0)
                for (TabPlayer p2 : TAB.getInstance().getPlayers()) {
                    p.hideNametag(p2.getUniqueId());
                    p2.hideNametag(p.getUniqueId());
                }
            if (TABAdditions.getInstance().tablistNamesRadius != 0)
                for (TabPlayer p2 : TAB.getInstance().getPlayers()) {
                    if (p != p2) {
                        p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, new PacketPlayOutPlayerInfo.PlayerInfoData(p2.getUniqueId())));
                        p2.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, new PacketPlayOutPlayerInfo.PlayerInfoData(p.getUniqueId())));
                    }
                }
        }, 50);

    }

    public static void ChatEvent(TabPlayer p, String msg) {
        ChatFormat format = ChatManager.getInstance().getFormat(p);
        IChatBaseComponent format2 = format.getText();
        TextColor oldColor = null;
        for (IChatBaseComponent comp : format2.getExtra()) {

            for (IChatBaseComponent txt : comp.getExtra()) {
                int pos = comp.getExtra().indexOf(txt);
                String msg2 = TABAdditions.getInstance().parsePlaceholders(txt.getText(), p).replaceAll("%msg%", msg);
                txt = IChatBaseComponent.fromColoredText(msg2).setColor(txt.getColor());

                List<IChatBaseComponent> colors = new ArrayList<>(txt.getExtra());
                Collections.reverse(colors);
                TextColor color = null;
                for (IChatBaseComponent c : colors) {
                    if (c.getColor() != null) {
                        color = c.getColor();
                    }
                    else if (txt.getColor() != null) {
                        color = txt.getColor();
                    }
                    //TAB.getInstance().getPlayer("Tanguygab").sendMessage(c.toString(),false);
                }
                if (oldColor != null) {
                    txt.setColor(oldColor);
                    //TAB.getInstance().getPlayer("Tanguygab").sendMessage(oldColor.toString(true), false);
                }
                oldColor = color;

                if (color != null)
                    //TAB.getInstance().getPlayer("Tanguygab").sendMessage(color.toString(true),false);

                comp.getExtra().set(pos, txt);
            }
            if (comp.getHoverValue() != null) {
                String txt = TABAdditions.getInstance().parsePlaceholders(comp.getHoverValue()+"", p).replaceAll("%msg%", msg);
                IChatBaseComponent hover = IChatBaseComponent.fromColoredText(txt);
                comp.onHoverShowText(hover);
            }
            if (comp.getClickValue() != null) {
                String txt = TABAdditions.getInstance().parsePlaceholders(comp.getClickValue()+"", p).replaceAll("%msg%", msg);
                comp.onClickSuggestCommand(txt);
            }
        }
        //TAB.getInstance().getPlayer("Tanguygab").sendMessage(format2.toString(),false);

        for (TabPlayer pl : TAB.getInstance().getPlayers())
        	pl.sendMessage(format2);
    }
}
