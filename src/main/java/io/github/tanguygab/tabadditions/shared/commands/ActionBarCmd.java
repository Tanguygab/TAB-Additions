package io.github.tanguygab.tabadditions.shared.commands;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;

public class ActionBarCmd {

    public ActionBarCmd(String name, String[] args, String actionbar) {

        TABAdditions instance = TABAdditions.getInstance();

        if (name.equals("*")) {
            for (TabPlayer p : TAB.getInstance().getPlayers())
                p.sendCustomPacket(new PacketPlayOutChat(IChatBaseComponent.fromColoredText(instance.parsePlaceholders(actionbar,p)), PacketPlayOutChat.ChatMessageType.GAME_INFO));
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
        actionbar = instance.parsePlaceholders(actionbar,p);
        p.sendCustomPacket(new PacketPlayOutChat(IChatBaseComponent.fromColoredText(actionbar), PacketPlayOutChat.ChatMessageType.GAME_INFO));
    }
}
