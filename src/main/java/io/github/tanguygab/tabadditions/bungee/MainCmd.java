package io.github.tanguygab.tabadditions.bungee;

import io.github.tanguygab.tabadditions.shared.SharedTA;
import io.github.tanguygab.tabadditions.shared.features.commands.*;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.*;

public class MainCmd extends Command {

    public MainCmd(String name, String permission, String... aliases) {
        super(name,permission,aliases);
    }

    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) return;
        TabPlayer p = TAB.getInstance().getPlayer(sender.getName());
        if (args.length < 1 || args[0].equalsIgnoreCase("help"))
            new HelpCmd(p, ProxyServer.getInstance().getPluginManager().getPlugin("TAB-Additions").getDescription().getVersion());
        else
            switch (args[0].toLowerCase()) {
                case "reload": {
                	((TABAdditionsBungeeCord)SharedTA.plugin).reload();
                    p.sendMessage("&aConfig reloaded!",true);
                    break;
                }
                case "actionbar": {
                    if (!SharedTA.config.getBoolean("features.actionbars"))
                        p.sendMessage("&cActionbar feature is not enabled, therefore this command cannot be used",true);
                    else if (args.length < 2)
                        p.sendMessage("&cYou have to provide an actionbar!",true);
                    else {
                        Map<String,String> section = SharedTA.actionbarConfig.getConfigurationSection("bars");
                        if (!section.containsKey(args[1]))
                            p.sendMessage("&cThis actionbar doesn't exist!",true);
                        else
                            new ActionBarCmd(p, args, section.get(args[1]));
                    }
                    break;
                }
                case "title": {
                    if (!SharedTA.config.getBoolean("features.titles"))
                        p.sendMessage("&cTitle feature is not enabled, therefore this command cannot be used",true);
                    else if (args.length < 2)
                        p.sendMessage("&cYou have to provide a title!",true);
                    else {
                        Map<String,String> titleSection = SharedTA.titleConfig.getConfigurationSection("titles."+args[1]);
                        if (titleSection.keySet().isEmpty()) {
                            p.sendMessage("&cThis title doesn't exist!",true);
                        }
                        else {
                            List<Object> titleProperties = new ArrayList<>();
                            for (Object property : titleSection.keySet())
                                titleProperties.add(titleSection.get(property));
                            new TitleCmd(p, args, titleProperties);
                        }
                    }
                    break;
                }
                case "tags": {
                    new TagsCmd(p, args);
                    break;
                }
                case "test": {
                    p.sendMessage("&7Nothing to see here :D",true);
                }
            }
    }
}
