package io.github.tanguygab.tabadditions.shared.commands;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

public class TagsCmd {
    public TagsCmd(String name, String[] args) {

        TABAdditions instance = TABAdditions.getInstance();

        if (args.length < 2) {
            instance.sendMessage(name,"&cYou have to specify &4hide&c, &4show &cor &4toggle &cand a player.");
            return;
        }
        if (args.length < 3) {
            instance.sendMessage(name,"&cYou didn't provide a player!");
            return;
        }

        TabPlayer p = TAB.getInstance().getPlayer(args[2]);

        if (p == null) {
            instance.sendMessage(name,"&cThis player isn't connected");
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
