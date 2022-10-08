package io.github.tanguygab.tabadditions.bungee;

import io.github.tanguygab.tabadditions.shared.features.ActionBar;
import io.github.tanguygab.tabadditions.shared.features.Title;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatManager;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabFeature;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
public class TabPlusCmds extends Command implements TabExecutor {

    public TabPlusCmds(String name) {
        super(name);
    }

    public TabPlusCmds(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {}

    private boolean featureEnabled(String feature) {
        return TabAPI.getInstance().getFeatureManager().isFeatureEnabled(feature);
    }
    private TabFeature getFeature(String feature) {
        return TabAPI.getInstance().getFeatureManager().getFeature(feature);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer && featureEnabled("Chat"))
            return ((ChatManager)getFeature("Chat")).cmds.tabcomplete(TabAPI.getInstance().getPlayer(sender.getName()),getName(),args);
        return null;
    }
}
