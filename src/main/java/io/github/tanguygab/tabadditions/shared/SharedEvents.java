package io.github.tanguygab.tabadditions.shared;

import io.github.tanguygab.tabadditions.shared.commands.TitleCmd;
import io.github.tanguygab.tabadditions.shared.layouts.LayoutManager;
import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SharedEvents {

    public static void JoinEvent(String name) {
    	SharedTA.platform.AsyncTask(()->{
            TabPlayer p = TABAPI.getPlayer(name);
            SharedTA.loadProps(p);

            if (SharedTA.actionbarsEnabled) {
                String actionbar = SharedTA.actionbarConfig.getString("bars." + p.getProperty("actionbar").get(),"");
                actionbar = Shared.platform.replaceAllPlaceholders(actionbar,p);
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
            if (SharedTA.layoutEnabled) {
                LayoutManager lm = LayoutManager.getInstance();
                lm.toAdd.put(p,lm.getLayout(p));
            }
            if (SharedTA.nametagInRange != 0)
                for (TabPlayer p2 : Shared.getPlayers()) {
                    p.hideNametag(p2.getUniqueId());
                    p2.hideNametag(p.getUniqueId());
                }
        }, 1);

    }
}
