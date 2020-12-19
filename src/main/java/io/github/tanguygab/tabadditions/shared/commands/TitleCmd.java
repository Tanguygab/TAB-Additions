package io.github.tanguygab.tabadditions.shared.commands;

import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.packets.PacketPlayOutTitle;

import java.util.List;

public class TitleCmd {
    public TitleCmd(TabPlayer sender, String[] args, List<Object> properties) {

        String title = properties.get(0)+"";
        String subtitle = properties.get(1)+"";
        int fadeIn = (int) properties.get(2);
        int stay = (int) properties.get(3);
        int fadeOut = (int) properties.get(4);

        TabPlayer p = sender;
        if (args.length > 2)
            p = TABAPI.getPlayer(args[2]);

        if (p == null) {
            sender.sendMessage("&cThis player isn't connected!",true);
            return;
        }
        title = Shared.platform.replaceAllPlaceholders(title,p);
        subtitle = Shared.platform.replaceAllPlaceholders(subtitle,p);
        p.sendCustomPacket(PacketPlayOutTitle.TITLE(title));
        p.sendCustomPacket(PacketPlayOutTitle.SUBTITLE(subtitle));
        p.sendCustomPacket(PacketPlayOutTitle.TIMES(fadeIn,stay,fadeOut));
    }

}
