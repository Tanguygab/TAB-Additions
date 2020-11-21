package io.github.tanguygab.tabadditions.bungee;

import io.github.tanguygab.tabadditions.shared.commands.*;
import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.api.TabPlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

import java.util.ArrayList;
import java.util.List;

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
                    if (!TABAdditionsBungeeCord.config.getBoolean("features.actionbars"))
                        pTAB.sendMessage("&cActionbar feature is not enabled, therefore this command cannot be used",true);
                    else if (args.length < 2)
                        pTAB.sendMessage("&cYou have to provide an actionbar!",true);
                    else {
                        Configuration section = TABAdditionsBungeeCord.actionbarConfig.getSection("bars");
                        if (!section.contains(args[1]))
                            pTAB.sendMessage("&cThis actionbar doesn't exist!",true);
                        else
                            new ActionBarCmd(pTAB, args, ChatColor.translateAlternateColorCodes('&', section.getString(args[1])));
                    }
                    break;
                }
                case "title": {
                    if (!TABAdditionsBungeeCord.config.getBoolean("features.titles"))
                        pTAB.sendMessage("&cTitle feature is not enabled, therefore this command cannot be used",true);
                    else if (args.length < 2)
                        pTAB.sendMessage("&cYou have to provide a title!",true);
                    else {
                        Configuration titleSection = TABAdditionsBungeeCord.titleConfig.getSection("titles."+args[1]);
                        if (titleSection.getKeys().isEmpty()) {
                            pTAB.sendMessage("&cThis title doesn't exist!",true);
                        }
                        else {
                            List<String> titleProperties = new ArrayList<>();
                            String title = titleSection.getString("title");
                            String subtitle = titleSection.getString("subtitle");
                            int fadein = titleSection.getInt("fadein");
                            int stay = titleSection.getInt("stay");
                            int fadeout = titleSection.getInt("fadeout");
                            titleProperties.add(ChatColor.translateAlternateColorCodes('&', title));
                            titleProperties.add(ChatColor.translateAlternateColorCodes('&', subtitle));
                            titleProperties.add(fadein+"");
                            titleProperties.add(stay+"");
                            titleProperties.add(fadeout+"");
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
