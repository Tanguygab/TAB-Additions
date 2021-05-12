package io.github.tanguygab.tabadditions.bungee;

import io.github.tanguygab.tabadditions.shared.ConfigType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.commands.*;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.*;

public class MainCmd extends Command implements TabExecutor {

    public MainCmd(String name, String permission, String... aliases) {
        super(name,permission,aliases);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String name = "~Console~";
        if (sender instanceof ProxiedPlayer) name = sender.getName();
        Cmd.getMain(name,args);
    }


    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (sender.hasPermission("tabadditions.admin"))
            return Cmd.getTabComplete(args);
        return null;
    }
}
