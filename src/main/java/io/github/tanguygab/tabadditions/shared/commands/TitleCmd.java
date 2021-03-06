package io.github.tanguygab.tabadditions.shared.commands;

import io.github.tanguygab.tabadditions.shared.PlatformType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bukkit.entity.Player;

import java.util.List;

public class TitleCmd {
    public TitleCmd(String name, String[] args, List<Object> properties) {

        TABAdditions instance = TABAdditions.getInstance();

        String title = properties.get(0)+"";
        String subtitle = properties.get(1)+"";
        int fadeIn = (int) properties.get(2);
        int stay = (int) properties.get(3);
        int fadeOut = (int) properties.get(4);

        TabPlayer p = null;
        if (args.length > 2)
            p = TAB.getInstance().getPlayer(args[2]);
        else if (!name.equals("~Console~"))
            p = TAB.getInstance().getPlayer(name);

        if (p == null) {
            instance.sendMessage(name,"&cThis player isn't connected!");
            return;
        }
        title = instance.parsePlaceholders(title,p);
        subtitle = instance.parsePlaceholders(subtitle,p);

        instance.getPlatform().sendTitle(p,title,subtitle,fadeIn,stay,fadeOut);
    }

}
