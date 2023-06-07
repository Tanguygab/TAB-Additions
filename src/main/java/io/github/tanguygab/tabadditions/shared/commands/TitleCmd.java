package io.github.tanguygab.tabadditions.shared.commands;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.titles.TitleManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
            sendMessage(sender,"&cYou have to provide a player!");
            return;
        }
        if (args.length < 2) {
            sendMessage(sender,"&cYou have to provide a title!");
            return;
        }

        String name = args[0];
        String title = args[1];

        if (name.equals("*")) {
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers())
                manager.announceTitle(player,title);
            return;
        }

        boolean self = name.equalsIgnoreCase("me");
        TabPlayer player = self ? sender : instance.getPlayer(name);
        if (player == null) {
            sendMessage(sender,self ? getMessages().getCommandOnlyFromGame() : getMessages().getPlayerNotFound(name));
            return;
        }
        manager.announceTitle(player,title);
    }

    @Override
    public @NotNull List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
        if (arguments.length == 1) {
            List<String> players = getOnlinePlayers(arguments[0]);
            players.add("*");
            players.add("me");
            return players;
        }
        return getStartingArgument(manager.getTitles().keySet(),arguments[1]);
    }
}
