package io.github.tanguygab.tabadditions.shared.commands;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutChat;

public class ActionBarCmd {

    public ActionBarCmd(String name, String[] args, String actionbar) {

        TABAdditions instance = TABAdditions.getInstance();
        TabPlayer p = null;
        if (args.length > 2)
            p = TAB.getInstance().getPlayer(args[2]);
        else if (!name.equals("~Console~"))
            p = TAB.getInstance().getPlayer(name);

        if (p == null) {
            instance.sendMessage(name,"&cThis player isn't connected");
            return;
        }
        TabFeature feature = TabAPI.getInstance().getFeatureManager().getFeature("&aActionBar&r");
        actionbar = instance.parsePlaceholders(actionbar,p);
        p.sendCustomPacket(new PacketPlayOutChat(IChatBaseComponent.optimizedComponent(actionbar), PacketPlayOutChat.ChatMessageType.GAME_INFO),feature);
    }
}
