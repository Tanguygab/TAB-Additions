package io.github.tanguygab.tabadditions.shared.commands;

import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.api.TabPlayer;

public class TagsCmd {
    public TagsCmd(TabPlayer sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("&cYou have to specify &4hide&c, &4show &cor &4toggle &cand a player.",true);
            return;
        }
        if (args.length < 3) {
            sender.sendMessage("&cYou didn't provide a player!",true);
            return;
        }

        TabPlayer p = TABAPI.getPlayer(args[2]);

        if (p == null) {
            sender.sendMessage("&cThis player isn't connected",true);
            return;
        }

        switch (args[1]) {
            case "show": {
                p.showNametag();
                break;
            }
            case "hide": {
                p.hideNametag();
                break;
            }
            case "toggle": {
                if (p.hasHiddenNametag()) p.showNametag();
                else p.hideNametag();
                break;
            }
        }
    }
}
