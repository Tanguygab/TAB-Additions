package io.github.tanguygab.tabadditions.spigot.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class TitleCmd {
    public TitleCmd(CommandSender sender, String[] args, FileConfiguration config) {

        ConfigurationSection section = config.getConfigurationSection("titles." + args[1]);
        if (section == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThis title doesn't exist!"));
            return;
        }
        String title = section.getString("title", "");
        String subtitle = section.getString("subtitle", "");
        int fadeIn = section.getInt("fadein", 5);
        int stay = section.getInt("stay", 20);
        int fadeOut = section.getInt("fadeout", 5);

        String p = sender.getName();
        if (args.length > 2)
            p = args[2];

        if (Bukkit.getServer().getPlayerExact(p) == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThis player isn't connected"));
            return;
        }

        Bukkit.getServer().getPlayerExact(p).sendTitle(title, subtitle, fadeIn,stay,fadeOut);
    }

}
