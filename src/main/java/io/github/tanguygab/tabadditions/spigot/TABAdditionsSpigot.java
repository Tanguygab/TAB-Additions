package io.github.tanguygab.tabadditions.spigot;

import io.github.tanguygab.tabadditions.shared.SharedTA;
import io.github.tanguygab.tabadditions.shared.Layout;
import io.github.tanguygab.tabadditions.shared.commands.*;
import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.api.TabPlayer;

import org.bukkit.Bukkit;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;


public class TABAdditionsSpigot extends JavaPlugin implements CommandExecutor, TabCompleter {

    @Override
    public void onEnable() {
        SharedTA.platform = "Bukkit";
        SharedTA.plugin = this;
        reload();
    }

    @Override
    public void onDisable() {
        Layout.removeAll();
    }

    public void reload() {

        SharedTA.reload(getDataFolder());

        HandlerList.unregisterAll(this);
        Bukkit.getServer().getPluginManager().registerEvents(new BukkitEvents(), this);


        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new TABAdditionsExpansion(this).register();
        }

    }



    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        TabPlayer p = TABAPI.getPlayer(sender.getName());
        if (args.length < 1 || args[0].toLowerCase().equals("help"))
            new HelpCmd(p, this.getDescription().getVersion());
        else
            switch (args[0].toLowerCase()) {
                case "reload": {
                    reload();
                    p.sendMessage("&aConfig reloaded!",true);
                    break;
                }
                case "actionbar": {
                    if (!SharedTA.config.getBoolean("features.actionbars"))
                        p.sendMessage("&cActionbar feature is not enabled, therefore this command cannot be used",true);
                    else if (args.length < 2)
                        p.sendMessage("&cYou have to provide an actionbar!",true);
                    else {
                        Map<String,String> section = SharedTA.actionbarConfig.getConfigurationSection("bars.");
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
                        if (titleSection.isEmpty()) {
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
                    break;
                }
            }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1)
            return new ArrayList<>(Arrays.asList("help","reload","actionbar","title","tags"));
        if (args.length >= 2) {
            switch (args[0]) {
                case "actionbar":
                    if (args.length == 2)
                        return SharedTA.actionbars;
                case "tags": {
                    if (args.length == 2)
                        return new ArrayList<>(Arrays.asList("hide","show","toggle"));
                }
                case "title": {
                    if (args.length == 2)
                        return SharedTA.titles;
                }
            }
        }
        return null;
    }
}
