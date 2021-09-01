package io.github.tanguygab.tabadditions.bungee;

import io.github.tanguygab.tabadditions.shared.features.TAFeature;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatManager;
import me.neznamy.tab.shared.TAB;
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
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer && TAB.getInstance().getFeatureManager().isFeatureEnabled(TAFeature.CHAT.toString()))
            ((ChatManager)TAB.getInstance().getFeatureManager().getFeature(TAFeature.CHAT.toString())).cmds.execute(TAB.getInstance().getPlayer(sender.getName()),getName(),args);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer && TAB.getInstance().getFeatureManager().isFeatureEnabled(TAFeature.CHAT.toString()))
            return ((ChatManager)TAB.getInstance().getFeatureManager().getFeature(TAFeature.CHAT.toString())).cmds.tabcomplete(TAB.getInstance().getPlayer(sender.getName()),getName(),args);
        return null;
    }
}
