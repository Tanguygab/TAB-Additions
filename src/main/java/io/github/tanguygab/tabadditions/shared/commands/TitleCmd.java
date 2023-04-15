package io.github.tanguygab.tabadditions.shared.commands;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.titles.TitleManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.platform.TabPlayer;

public class TitleCmd extends SubCommand {

    private final TitleManager manager;

    public TitleCmd(TitleManager manager) {
        super("title",null);
        this.manager = manager;
    }


    @Override
    public void execute(TabPlayer sender, String[] args) {
        TABAdditions instance = TABAdditions.getInstance();
        if (args.length < 1) {
            sendMessage(sender,"&cYou have to provide a title!");
            return;
        }
        String title = args[0].replace("_"," ");

        if (args.length > 1 && args[1].equals("*")) {
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers())
                manager.announceTitle(player,title);
            return;
        }

        TabPlayer player = args.length > 1 ? instance.getPlayer(args[1]) : sender;
        if (player == null) {
            sendMessage(sender,args.length > 1 ? getMessages().getPlayerNotFound(args[1]) : getMessages().getCommandOnlyFromGame());
            return;
        }
        manager.announceTitle(player,title);
    }

}
