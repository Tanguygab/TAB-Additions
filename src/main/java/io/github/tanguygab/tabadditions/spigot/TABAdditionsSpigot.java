package io.github.tanguygab.tabadditions.spigot;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.commands.*;
import io.github.tanguygab.tabadditions.shared.features.layouts.LayoutManager;
import io.github.tanguygab.tabadditions.spigot.Features.BukkitEvents;
import me.neznamy.tab.api.TabPlayer;

import me.neznamy.tab.shared.TAB;
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
        new TABAdditions();
        TABAdditions.getInstance().setPlatform(new SpigotTA(this));
        TABAdditions.getInstance().setPlugin(this);
        reload();
    }

    @Override
    public void onDisable() {
        if (LayoutManager.getInstance() != null) LayoutManager.getInstance().unregister();
    }

    public void reload() {
        TABAdditions.getInstance().reload(getDataFolder());

        HandlerList.unregisterAll(this);
        Bukkit.getServer().getPluginManager().registerEvents(new BukkitEvents(), this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
            new TABAdditionsExpansion(this).register();
        TABAdditions.getInstance().floodgate = Bukkit.getPluginManager().getPlugin("Floodgate") != null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        TabPlayer p = TAB.getInstance().getPlayer(sender.getName());
        if (args.length < 1 || args[0].equalsIgnoreCase("help"))
            new HelpCmd(p, this.getDescription().getVersion());
        else
            switch (args[0].toLowerCase()) {
                case "reload": {
                    reload();
                    p.sendMessage("&aConfig reloaded!",true);
                    break;
                }
                case "actionbar": {
                    if (!TABAdditions.getInstance().getConfig("").getBoolean("features.actionbars"))
                        p.sendMessage("&cActionbar feature is not enabled, therefore this command cannot be used",true);
                    else if (args.length < 2)
                        p.sendMessage("&cYou have to provide an actionbar!",true);
                    else {
                        Map<String,String> section = TABAdditions.getInstance().actionbarConfig.getConfigurationSection("bars.");
                        if (!section.containsKey(args[1]))
                            p.sendMessage("&cThis actionbar doesn't exist!",true);
                        else
                            new ActionBarCmd(p, args, section.get(args[1]));
                    }
                    break;
                }
                case "title": {
                    if (!TABAdditions.getInstance().getConfig("").getBoolean("features.titles"))
                        p.sendMessage("&cTitle feature is not enabled, therefore this command cannot be used",true);
                    else if (args.length < 2)
                        p.sendMessage("&cYou have to provide a title!",true);
                    else {
                        Map<String,String> titleSection = TABAdditions.getInstance().titleConfig.getConfigurationSection("titles."+args[1]);
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
                case "fp": {
                    if (TABAdditions.getInstance().rfpEnabled) {
                        if (args.length < 2)
                            p.sendMessage("You have to provide add, remove, edit or list.", false);
                        else if (!args[1].equalsIgnoreCase("list") && args.length < 3)
                            p.sendMessage("You have to provide a fake player name.", false);
                        else if (args[1].equalsIgnoreCase("edit") && args.length < 4)
                            p.sendMessage("You have to provide an action.", false);
                        else new RealFakePlayer(p, args);
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
            return new ArrayList<>(Arrays.asList("help","reload","actionbar","title","tags","fp"));
        if (args.length >= 2) {
            switch (args[0]) {
                case "actionbar":
                    if (args.length == 2)
                        return TABAdditions.getInstance().actionbars;
                case "tags": {
                    if (args.length == 2)
                        return new ArrayList<>(Arrays.asList("hide","show","toggle"));
                }
                case "fp": {
                    if (args.length == 2)
                        return new ArrayList<>(Arrays.asList("add","remove","edit","list"));
                    if (!args[1].equalsIgnoreCase("list") && args.length == 3)
                        return new ArrayList<>(Collections.singletonList("<name>"));
                    if (args[1].equalsIgnoreCase("edit") && args.length == 4)
                        return new ArrayList<>(Arrays.asList("name","skin","latency","group"));
                }
                case "title": {
                    if (args.length == 2)
                        return TABAdditions.getInstance().titles;
                }
            }
        }
        return null;
    }
}
