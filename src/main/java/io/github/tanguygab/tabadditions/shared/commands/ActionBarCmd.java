package io.github.tanguygab.tabadditions.shared.commands;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutChat;

public class ActionBarCmd {

    public ActionBarCmd(String name, String[] args, String actionbar) {

        TabAPI tab = TabAPI.getInstance();
        TABAdditions instance = TABAdditions.getInstance();
        TabFeature feature = tab.getFeatureManager().getFeature("&aActionBar&r");

        if (args.length > 2 && args[2].equals("*")) {
            for (TabPlayer p : tab.getOnlinePlayers())
                p.sendCustomPacket(new PacketPlayOutChat(IChatBaseComponent.optimizedComponent(instance.parsePlaceholders(actionbar,p,feature)), PacketPlayOutChat.ChatMessageType.GAME_INFO),feature);
            return;
        }

        TabPlayer p = null;
        if (args.length > 2)
            p = instance.getPlayer(args[2]);
        else if (!name.equals("~Console~"))
            p = instance.getPlayer(name);

        if (p == null) {
            instance.sendMessage(name,"&cThis player isn't connected");
            return;
        }
        p.sendCustomPacket(new PacketPlayOutChat(IChatBaseComponent.optimizedComponent(instance.parsePlaceholders(actionbar,p,feature)), PacketPlayOutChat.ChatMessageType.GAME_INFO),feature);
    }
}
