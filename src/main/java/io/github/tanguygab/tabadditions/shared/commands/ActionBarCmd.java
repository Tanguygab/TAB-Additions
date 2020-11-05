package io.github.tanguygab.tabadditions.shared.commands;

import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;
import net.md_5.bungee.api.ChatColor;

public class ActionBarCmd {

    public ActionBarCmd(TabPlayer sender, String[] args, String actionbar) {

        TabPlayer p = sender;
        if (args.length > 2)
            p = TABAPI.getPlayer(args[2]);

        if (p == null) {
            sender.sendCustomPacket(new PacketPlayOutChat(ChatColor.translateAlternateColorCodes('&', "&cThis player isn't connected"), PacketPlayOutChat.ChatMessageType.CHAT));
            return;
        }
        actionbar = Shared.platform.replaceAllPlaceholders(actionbar, p);
        p.sendCustomPacket(new PacketPlayOutChat(actionbar, PacketPlayOutChat.ChatMessageType.GAME_INFO));
    }
}
