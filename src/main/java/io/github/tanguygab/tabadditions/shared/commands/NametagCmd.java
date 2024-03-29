package io.github.tanguygab.tabadditions.shared.commands;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.nametag.NameTagManager;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NametagCmd extends SubCommand {

    private final NameTagManager manager;

    public NametagCmd(NameTagManager manager) {
        super("nametag+",null);
        this.manager = manager;
    }

    @Override
    public void execute(TabPlayer player, String[] args) {
        TABAdditions instance = TABAdditions.getInstance();

        if (args.length < 1 || !List.of("hide","show","toggle").contains(args[0])) {
            sendMessage(player,"&cYou have to specify &4hide&c, &4show &cor &4toggle &cand a player.");
            return;
        }
        if (args.length < 2) {
            sendMessage(player,"&cYou didn't provide a player!");
            return;
        }
        String action = args[0].toLowerCase();
        TabPlayer target = instance.getPlayer(args[1]);
        if (target == null) {
            sendMessage(player,"&cThis player isn't connected");
            return;
        }
        TabPlayer viewer = args.length > 2 ? args[2].equals("me") ? player : instance.getPlayer(args[2]) : null;


        boolean toggle = action.equals("toggle")
                ? !(viewer == null ? manager.hasHiddenNameTag(target) : manager.hasHiddenNameTag(target,viewer))
                : action.equals("hide");
        toggle(target,viewer,toggle);
        sendMessage(player,(toggle ? "&cHiding " : "&aShowing ")
                +target.getName()+"'s nametag "
                +(toggle ? "from " : "to ")
                +(viewer == null ? "everyone" : viewer.getName()));
    }

    private void toggle(TabPlayer p, TabPlayer viewer, boolean hide) {
        if (viewer == null) {
            if (hide) manager.hideNameTag(p);
            else manager.showNameTag(p);
            return;
        }
        if (hide) manager.hideNameTag(p,viewer);
        else manager.showNameTag(p,viewer);
    }

    @Override
    public @NotNull List<String> complete(TabPlayer sender, String[] arguments) {
        return arguments.length == 1 ? getStartingArgument(List.of("hide", "show", "toggle"),arguments[0]) : getOnlinePlayers(arguments[1]);
    }
}
