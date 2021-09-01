package io.github.tanguygab.tabadditions.spigot;

import io.github.tanguygab.tabadditions.shared.features.TAFeature;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatManager;
import me.neznamy.tab.shared.TAB;
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
        if (sender instanceof Player && TAB.getInstance().getFeatureManager().isFeatureEnabled(TAFeature.CHAT.toString())) {
            ((ChatManager)TAB.getInstance().getFeatureManager().getFeature(TAFeature.CHAT.toString())).cmds.execute(TAB.getInstance().getPlayer(sender.getName()),alias,args);
            return true;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (sender instanceof Player && TAB.getInstance().getFeatureManager().isFeatureEnabled(TAFeature.CHAT.toString()))
            return ((ChatManager)TAB.getInstance().getFeatureManager().getFeature(TAFeature.CHAT.toString())).cmds.tabcomplete(TAB.getInstance().getPlayer(sender.getName()),alias,args);
        return null;
    }
}
