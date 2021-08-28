package io.github.tanguygab.tabadditions.spigot;

import io.github.tanguygab.tabadditions.shared.features.chat.ChatManager;
import me.neznamy.tab.api.TabAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TabPlusCmds implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (sender instanceof Player && TabAPI.getInstance().getFeatureManager().isFeatureEnabled("&aChat&r")) {
            ((ChatManager)TabAPI.getInstance().getFeatureManager().getFeature("&aChat&r")).cmds.execute(TabAPI.getInstance().getPlayer(sender.getName()),alias,args);
            return true;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (sender instanceof Player && TabAPI.getInstance().getFeatureManager().isFeatureEnabled("&aChat&r"))
            return ((ChatManager)TabAPI.getInstance().getFeatureManager().getFeature("&aChat&r")).cmds.tabcomplete(TabAPI.getInstance().getPlayer(sender.getName()),alias,args);
        return null;
    }
}
