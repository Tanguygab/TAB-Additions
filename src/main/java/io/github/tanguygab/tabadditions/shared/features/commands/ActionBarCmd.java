package io.github.tanguygab.tabadditions.shared.features.commands;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;

public class ActionBarCmd {

    public ActionBarCmd(TabPlayer sender, String[] args, String actionbar) {

        TabPlayer p = sender;
        if (args.length > 2)
            p = TABAPI.getPlayer(args[2]);

        if (p == null) {
            sender.sendMessage("&cThis player isn't connected",true);
            return;
        }
        actionbar = TABAdditions.getInstance().parsePlaceholders(actionbar,p);
        p.sendCustomPacket(new PacketPlayOutChat(actionbar, PacketPlayOutChat.ChatMessageType.GAME_INFO));
    }
}
