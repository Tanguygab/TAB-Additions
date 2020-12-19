package io.github.tanguygab.tabadditions.bungee;

import io.github.tanguygab.tabadditions.shared.SharedTA;
import io.github.tanguygab.tabadditions.shared.commands.*;
import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.api.TabPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainCmd extends Command {

    public MainCmd(String name, String permission, String... aliases) {
        super(name,permission,aliases);
    }

    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) return;
        TabPlayer pTAB = TABAPI.getPlayer(sender.getName());
        if (args.length < 1 || args[0].toLowerCase().equals("help"))
            new HelpCmd(pTAB, ProxyServer.getInstance().getPluginManager().getPlugin("TAB-Additions").getDescription().getVersion());
        else
            switch (args[0].toLowerCase()) {
                case "reload": {
                    try {TABAdditionsBungeeCord.class.getMethod("reload");}
                    catch (NoSuchMethodException e) {e.printStackTrace();}
                    pTAB.sendMessage("&aConfig reloaded!",true);
                    break;
                }
                case "actionbar": {
                    if (!SharedTA.config.getBoolean("features.actionbars"))
                        pTAB.sendMessage("&cActionbar feature is not enabled, therefore this command cannot be used",true);
                    else if (args.length < 2)
                        pTAB.sendMessage("&cYou have to provide an actionbar!",true);
                    else {
                        Map<String,String> section = SharedTA.actionbarConfig.getConfigurationSection("bars");
                        if (!section.containsKey(args[1]))
                            pTAB.sendMessage("&cThis actionbar doesn't exist!",true);
                        else
                            new ActionBarCmd(pTAB, args, section.get(args[1]));
                    }
                    break;
                }
                case "title": {
                    if (!SharedTA.config.getBoolean("features.titles"))
                        pTAB.sendMessage("&cTitle feature is not enabled, therefore this command cannot be used",true);
                    else if (args.length < 2)
                        pTAB.sendMessage("&cYou have to provide a title!",true);
                    else {
                        Map<String,String> titleSection = SharedTA.titleConfig.getConfigurationSection("titles."+args[1]);
                        if (titleSection.keySet().isEmpty()) {
                            pTAB.sendMessage("&cThis title doesn't exist!",true);
                        }
                        else {
                            List<Object> titleProperties = new ArrayList<>();
                            for (Object property : titleSection.keySet())
                                titleProperties.add(titleSection.get(property));
                            new TitleCmd(pTAB, args, titleProperties);
                        }
                    }
                    break;
                }
                case "tags": {
                    new TagsCmd(pTAB, args);
                    break;
                }
                case "test": {
                    pTAB.sendMessage("&7Nothing to see here :D",true);
                }
            }
    }
}
