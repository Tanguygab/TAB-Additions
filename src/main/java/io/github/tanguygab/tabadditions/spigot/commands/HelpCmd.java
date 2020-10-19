package io.github.tanguygab.tabadditions.spigot.commands;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;

public class HelpCmd {

    public HelpCmd(CommandSender p, String version) {
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&m                                        \n"
                + "&a[TAB-Additions] &7" + version + "\n"
                + " - &3/tab+ [help]\n"
                + "   &8| &aDefault help page\n"
                + " - &3/tab+ actionbar <name> [player]\n"
                + "   &8| &aSends the specfied actionbar to the player\n"
                + " - &3/tab+ title <name> [player]\n"
                + "   &8| &aSends the specfied title to the player\n"
                + " - &3/tab+ tags <hide/show/toggle> <player>\n"
                + "   &8| &aHides/shows/toggles the name of the specified player \n"
                + " - &3/tab+ reload\n"
                + "   &8| &aReloads the configuration file\n"
                + "&m                                        "));
    }
}
