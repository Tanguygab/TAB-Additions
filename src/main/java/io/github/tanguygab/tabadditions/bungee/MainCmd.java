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
                    sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',"&aConfig reloaded!")));
                    break;
                }
                case "actionbar": {
                    if (!TABAdditionsBungeeCord.config.getBoolean("features.actionbars"))
                        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',"&cActionbar feature is not enabled, therefore this command cannot be used")));
                    else if (args.length < 2)
                        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', "&cYou have to provide an actionbar!")));
                    else {
                        Configuration section = TABAdditionsBungeeCord.actionbarConfig.getSection("bars");
                        if (!section.contains(args[1]))
                            sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', "&cThis actionbar doesn't exist!")));
                        else
                            new ActionBarCmd(pTAB, args, ChatColor.translateAlternateColorCodes('&', section.getString(args[1])));
                    }
                    break;
                }
                case "title": {
                    if (!TABAdditionsBungeeCord.config.getBoolean("features.titles"))
                        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',"&cTitle feature is not enabled, therefore this command cannot be used")));
                    else if (args.length < 2)
                        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', "&cYou have to provide a title!")));
                    else {
                        Configuration titleSection = TABAdditionsBungeeCord.titleConfig.getSection("titles."+args[1]);
                        if (titleSection.getKeys().isEmpty()) {
                            sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', "&cThis title doesn't exist!")));
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
                    sender.sendMessage(new TextComponent("Nothing to see here :D"));
                }
            }
    }
}
