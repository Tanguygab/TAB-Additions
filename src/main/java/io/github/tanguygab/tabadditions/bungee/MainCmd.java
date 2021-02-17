package io.github.tanguygab.tabadditions.bungee;

import io.github.tanguygab.tabadditions.shared.ConfigType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.commands.*;
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
        String name = "~Console~";
        TABAdditions instance = TABAdditions.getInstance();
        if (sender instanceof ProxiedPlayer) name = sender.getName();

        if (args.length < 1 || args[0].equalsIgnoreCase("help"))
            new HelpCmd(name, ProxyServer.getInstance().getPluginManager().getPlugin("TAB-Additions").getDescription().getVersion());
        else
            switch (args[0].toLowerCase()) {

                case "actionbar": {
                    if (!TABAdditions.getInstance().getConfig(ConfigType.MAIN).getBoolean("features.actionbars"))
                        instance.sendMessage(name,"&cActionbar feature is not enabled, therefore this command cannot be used");
                    else if (args.length < 2)
                        instance.sendMessage(name,"&cYou have to provide an actionbar!");
                    else {
                        Map<String,String> section = TABAdditions.getInstance().getConfig(ConfigType.ACTIONBAR).getConfigurationSection("bars");
                        if (!section.containsKey(args[1]))
                            instance.sendMessage(name,"&cThis actionbar doesn't exist!");
                        else
                            new ActionBarCmd(name, args, section.get(args[1]));
                    }
                    break;
                }
                case "title": {
                    if (!TABAdditions.getInstance().getConfig(ConfigType.MAIN).getBoolean("features.titles"))
                        instance.sendMessage(name,"&cTitle feature is not enabled, therefore this command cannot be used");
                    else if (args.length < 2)
                        instance.sendMessage(name,"&cYou have to provide a title!");
                    else {
                        Map<String,String> titleSection = TABAdditions.getInstance().getConfig(ConfigType.TITLE).getConfigurationSection("titles."+args[1]);
                        if (titleSection.keySet().isEmpty()) {
                            instance.sendMessage(name,"&cThis title doesn't exist!");
                        }
                        else {
                            List<Object> titleProperties = new ArrayList<>();
                            for (Object property : titleSection.keySet())
                                titleProperties.add(titleSection.get(property));
                            new TitleCmd(name, args, titleProperties);
                        }
                    }
                    break;
                }
                case "fp": {
                    if (TABAdditions.getInstance().rfpEnabled) {
                        if (args.length < 2)
                            instance.sendMessage(name,"You have to provide add, remove, edit, info or list.");
                        else if (!args[1].equalsIgnoreCase("list") && args.length < 3)
                            instance.sendMessage(name,"You have to provide a fake player name.");
                        else if (args[1].equalsIgnoreCase("edit") && args.length < 4)
                            instance.sendMessage(name,"You have to provide an action.");
                        else new RealFakePlayerCmd(name, args);
                    }
                    break;
                }
                case "tags": {
                    new TagsCmd(name, args);
                    break;
                }
                case "test": {
                    instance.sendMessage(name,"&7Nothing to see here :D");
                }
            }
    }
}
