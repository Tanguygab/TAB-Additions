package io.github.tanguygab.tabadditions.shared.commands;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.actionbar.ActionBarManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.platform.TabPlayer;

public class ActionBarCmd extends SubCommand {

    private final ActionBarManager manager;

    public ActionBarCmd(ActionBarManager manager) {
        super("actionbar",null);
        this.manager = manager;
    }


    @Override
    public void execute(TabPlayer sender, String[] args) {
        TABAdditions instance = TABAdditions.getInstance();
        if (args.length < 1) {
            sendMessage(sender,"&cYou have to provide an actionbar!");
            return;
        }
        String actionbar = args[0].replace("_"," ");

        if (args.length > 1 && args[1].equals("*")) {
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers())
                manager.announceBar(player,actionbar);
            return;
        }

        TabPlayer player = args.length > 1 ? instance.getPlayer(args[1]) : sender;
        if (player == null) {
            sendMessage(sender,args.length > 1 ? getMessages().getPlayerNotFound(args[1]) : getMessages().getCommandOnlyFromGame());
            return;
        }
        manager.announceBar(player,actionbar);
    }

}
