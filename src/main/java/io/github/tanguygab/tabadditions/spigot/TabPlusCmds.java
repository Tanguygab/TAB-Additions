package io.github.tanguygab.tabadditions.spigot;

import io.github.tanguygab.tabadditions.shared.features.ActionBar;
import io.github.tanguygab.tabadditions.shared.features.Title;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatManager;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabFeature;
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
        if (command.getName().equals("toggletitle") && featureEnabled("Title")) {
            ((Title)getFeature("Title")).toggleTitle(sender.getName());
            return true;
        }
        if (command.getName().equals("toggleactionbar") && featureEnabled("ActionBar")) {
            ((ActionBar)getFeature("ActionBar")).toggleActionBar(sender.getName());
            return true;
        }
        if (sender instanceof Player && featureEnabled("Chat")) {
            ((ChatManager)getFeature("Chat")).cmds.execute(TabAPI.getInstance().getPlayer(sender.getName()),command.getName(),args);
            return true;
        }
        return true;
    }

    private boolean featureEnabled(String feature) {
        return TabAPI.getInstance().getFeatureManager().isFeatureEnabled(feature);
    }
    private TabFeature getFeature(String feature) {
        return TabAPI.getInstance().getFeatureManager().getFeature(feature);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (sender instanceof Player && featureEnabled("Chat"))
            return ((ChatManager)getFeature("Chat")).cmds.tabcomplete(TabAPI.getInstance().getPlayer(sender.getName()),command.getName(),args);
        return null;
    }
}
