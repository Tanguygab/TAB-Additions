package io.github.tanguygab.tabadditions.spigot.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class ActionBarCmd {

    public ActionBarCmd(CommandSender sender, String[] args, FileConfiguration config) {

        String actionbar = config.getString("bars." + args[1]);
        if (actionbar == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThis actionbar doesn't exist!"));
            return;
        }

        String p = sender.getName();
        if (args.length > 2)
            p = args[2];

        if (Bukkit.getServer().getPlayerExact(p) == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThis player isn't connected"));
            return;
        }
        Bukkit.getServer().getPlayer(p).spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(actionbar).create());
    }
}
