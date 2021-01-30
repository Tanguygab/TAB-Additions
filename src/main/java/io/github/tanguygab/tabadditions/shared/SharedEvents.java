package io.github.tanguygab.tabadditions.shared;

import io.github.tanguygab.tabadditions.shared.features.chat.ChatFormat;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatManager;
import io.github.tanguygab.tabadditions.shared.features.commands.TitleCmd;
import io.github.tanguygab.tabadditions.shared.features.layouts.LayoutManager;
import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
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
    	SharedTA.platform.AsyncTask(()->{
            TabPlayer p = TABAPI.getPlayer(name);
            SharedTA.loadProps(p);

            if (SharedTA.actionbarsEnabled) {
                String actionbar = SharedTA.actionbarConfig.getString("bars." + p.getProperty("actionbar").get(),"");
                actionbar = TAB.getInstance().getPlatform().replaceAllPlaceholders(actionbar,p);
                p.sendCustomPacket(new PacketPlayOutChat(actionbar, PacketPlayOutChat.ChatMessageType.GAME_INFO));
            }
            if (SharedTA.titlesEnabled) {
                Map<String,String> tSection = SharedTA.titleConfig.getConfigurationSection("titles." + p.getProperty("title").get());
                if (tSection != null) {
                    List<Object> titleProperties = new ArrayList<>();
                    for (Object property : tSection.keySet())
                        titleProperties.add(tSection.get(property));
                    new TitleCmd(p, new String[]{}, titleProperties);
                }
            }
            if (SharedTA.layoutEnabled && !SharedTA.checkBedrock(p)) {
                LayoutManager lm = LayoutManager.getInstance();
                lm.toAdd.put(p,lm.getLayout(p));
            }
            if (SharedTA.nametagInRange != 0)
                for (TabPlayer p2 : TAB.getInstance().getPlayers()) {
                    p.hideNametag(p2.getUniqueId());
                    p2.hideNametag(p.getUniqueId());
                }
            if (SharedTA.tablistNamesRadius != 0)
                for (TabPlayer p2 : TAB.getInstance().getPlayers()) {
                    if (p != p2) {
                        p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, new PacketPlayOutPlayerInfo.PlayerInfoData(p2.getUniqueId())));
                        p2.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, new PacketPlayOutPlayerInfo.PlayerInfoData(p.getUniqueId())));
                    }
                }
        }, 1);

    }

    public static void ChatEvent(TabPlayer p, String msg) {
        ChatFormat format = ChatManager.getInstance().getFormat(p);
        IChatBaseComponent format2 = format.getText();
        TextColor oldColor = null;
        for (IChatBaseComponent comp : format2.getExtra()) {

            for (IChatBaseComponent txt : comp.getExtra()) {
                int pos = comp.getExtra().indexOf(txt);
                String msg2 = TAB.getInstance().getPlatform().replaceAllPlaceholders(txt.getText(), p).replaceAll("%msg%", msg);
                txt = IChatBaseComponent.fromColoredText(msg2).setColor(txt.getColor());
                txt.toString(ProtocolVersion.v1_16_4);


/*                List<IChatBaseComponent> colors = new ArrayList<>(txt.getExtra());
                Collections.reverse(colors);
                TextColor color = null;
                for (IChatBaseComponent c : colors) {
                    if (c.getColor() != null) {
                        color = c.getColor();
                        break;
                    }
                    else if (txt.getColor() != null) {
                        color = txt.getColor();
                        break;
                    }
                }
                if (oldColor != null)
                    txt.setColor(oldColor);
                oldColor = color;

                if (oldColor != null)
                    TAB.getInstance().getPlayer("Tanguygab").sendMessage(oldColor.toString(true),false);
                if (color != null)
                    TAB.getInstance().getPlayer("Tanguygab").sendMessage(color.toString(true),false);
*/
                comp.getExtra().set(pos, txt);
            }
            if (comp.getHoverValue() != null) {
                String txt = TAB.getInstance().getPlatform().replaceAllPlaceholders(comp.getHoverValue()+"", p).replaceAll("%msg%", msg);
                IChatBaseComponent hover = IChatBaseComponent.fromColoredText(txt);
                hover.toString(ProtocolVersion.v1_16_4);
                comp.onHoverShowText(hover);
            }
            if (comp.getClickValue() != null) {
                String txt = TAB.getInstance().getPlatform().replaceAllPlaceholders(comp.getClickValue()+"", p).replaceAll("%msg%", msg);
                comp.onClickSuggestCommand(txt);
            }
        }
        //TAB.getInstance().getPlayer("Tanguygab").sendMessage(format2.toString(ProtocolVersion.v1_16_4),false);

        for (TabPlayer pl : TAB.getInstance().getPlayers())
            pl.sendCustomPacket(new PacketPlayOutChat(format2, PacketPlayOutChat.ChatMessageType.CHAT));
    }
}
