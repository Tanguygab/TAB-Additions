package io.github.tanguygab.tabadditions.shared.commands;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

public class TagsCmd {

    private final String name;

    public TagsCmd(String name, String[] args) {
        this.name = name;

        TABAdditions instance = TABAdditions.getInstance();

        if (args.length < 2) {
            instance.sendMessage(name,"&cYou have to specify &4hide&c, &4show &cor &4toggle &cand a player.");
            return;
        }
        if (args.length < 3) {
            instance.sendMessage(name,"&cYou didn't provide a player!");
            return;
        }

        TabPlayer viewer = args.length > 3 ? (args[3].equals("_self_") ? instance.getPlayer(name) : instance.getPlayer(args[3])) : null;

        if (args[2].equals("*")) {
            TAB.getInstance().getPlayers().forEach(p2 -> toggleTag(p2, args[1], viewer,false));
            String action = args[1].toLowerCase()
                    .replace("hide","cHid")
                    .replace("show","aShow")
                    .replace("toggle","7Toggling");
            sendMsg("&"+action+"ing everyone's nametag"+(viewer != null ?" for "+viewer.getName() : ""));
            return;
        }


        TabPlayer p = instance.getPlayer(args[2]);

        if (p == null) {
            instance.sendMessage(name,"&cThis player isn't connected");
            return;
        }

        toggleTag(p,args[1],viewer,true);

    }

    public void sendMsg(String msg) {
        TABAdditions.getInstance().sendMessage(name,msg);
    }

    public void toggleTag(TabPlayer p, String action, TabPlayer viewer, boolean sendmsg) {
        switch (action) {
            case "show": {
                if (viewer != null) {
                    p.showNametag(viewer.getUniqueId());
                    if (sendmsg) sendMsg("&aShowing "+p.getName()+"'s nametag for "+viewer.getName());
                }
                else {
                    p.showNametag();
                    if (sendmsg) sendMsg("&aShowing "+p.getName()+"'s nametag for everyone");
                }

                return;
            }
            case "hide": {
                if (viewer != null) {
                    p.hideNametag(viewer.getUniqueId());
                    if (sendmsg) sendMsg("&cHiding "+p.getName()+"'s nametag for "+viewer.getName());
                }
                else {
                    p.hideNametag();
                    if (sendmsg) sendMsg("&cHiding "+p.getName()+"'s nametag for everyone");
                }
                return;
            }
            case "toggle": {
                if (viewer != null) {
                    if (p.hasHiddenNametag(viewer.getUniqueId())) {
                        p.showNametag(viewer.getUniqueId());
                        if (sendmsg) sendMsg("&aToggling on "+p.getName()+"'s nametag for "+viewer.getName());
                    }
                    else {
                        p.hideNametag(viewer.getUniqueId());
                        if (sendmsg) sendMsg("&cToggling off "+p.getName()+"'s nametag for "+viewer.getName());
                    }
                    return;
                }

                if (p.hasHiddenNametag()) {
                    p.showNametag();
                    if (sendmsg) sendMsg("&aToggling on "+p.getName()+"'s nametag for everyone");
                }
                else {
                    p.hideNametag();
                    if (sendmsg) sendMsg("&cToggling off "+p.getName()+"'s nametag for everyone");
                }
            }
        }
    }


}
