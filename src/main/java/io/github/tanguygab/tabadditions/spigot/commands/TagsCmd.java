package io.github.tanguygab.tabadditions.spigot.commands;

import me.neznamy.tab.api.TABAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class TagsCmd {
    public TagsCmd(CommandSender p, String[] args) {
        if (args.length < 2) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou have to specify &4hide&c, &4show &cor &4toggle &cand a player."));
            return;
        }
        if (args.length < 3) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&',"&cYou didn't provide a player!"));
            return;
        }

        Server server = Bukkit.getServer();

        if (server.getPlayerExact(args[2]) == null) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThis player isn't connected"));
            return;
        }

        UUID uuid = server.getPlayerExact(args[2]).getUniqueId();

        switch (args[1]) {
            case "show": {
                TABAPI.showNametag(uuid);
                break;
            }
            case "hide": {
                TABAPI.hideNametag(uuid);
                break;
            }
            case "toggle": {
                if (TABAPI.hasHiddenNametag(uuid)) TABAPI.showNametag(uuid);
                else TABAPI.hideNametag(uuid);
                break;
            }
        }
    }
}
