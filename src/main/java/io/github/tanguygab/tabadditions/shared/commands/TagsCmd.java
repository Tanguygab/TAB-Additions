package io.github.tanguygab.tabadditions.shared.commands;

import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;
import net.md_5.bungee.api.ChatColor;

public class TagsCmd {
    public TagsCmd(TabPlayer sender, String[] args) {
        if (args.length < 2) {
            sender.sendCustomPacket(new PacketPlayOutChat(ChatColor.translateAlternateColorCodes('&', "&cYou have to specify &4hide&c, &4show &cor &4toggle &cand a player."), PacketPlayOutChat.ChatMessageType.CHAT));
            return;
        }
        if (args.length < 3) {
            sender.sendCustomPacket(new PacketPlayOutChat(ChatColor.translateAlternateColorCodes('&', "&cYou didn't provide a player!"), PacketPlayOutChat.ChatMessageType.CHAT));
            return;
        }

        TabPlayer p = TABAPI.getPlayer(args[2]);

        if (p == null) {
            sender.sendCustomPacket(new PacketPlayOutChat(ChatColor.translateAlternateColorCodes('&', "&cThis player isn't connected"), PacketPlayOutChat.ChatMessageType.CHAT));
            return;
        }

        switch (args[1]) {
            case "show": {
                p.showNametag();
                break;
            }
            case "hide": {
                p.hideNametag();
                break;
            }
            case "toggle": {
                if (p.hasHiddenNametag()) p.showNametag();
                else p.hideNametag();
                break;
            }
        }
    }
}
