package io.github.tanguygab.tabadditions.shared.commands;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.team.TeamManager;

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

        TabPlayer p = TabAPI.getInstance().getPlayer(args[2]);

        if (p == null) {
            instance.sendMessage(name,"&cThis player isn't connected");
            return;
        }

        TeamManager tm = TabAPI.getInstance().getTeamManager();
        switch (args[1]) {
            case "show": {
                tm.showNametag(p);
                break;
            }
            case "hide": {
                tm.hideNametag(p);
                break;
            }
            case "toggle": {
                if (tm.hasHiddenNametag(p)) tm.showNametag(p);
                else tm.hideNametag(p);
                break;
            }
        }
    }
}
