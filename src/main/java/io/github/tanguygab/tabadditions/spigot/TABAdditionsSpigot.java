package io.github.tanguygab.tabadditions.spigot;

import io.github.tanguygab.tabadditions.spigot.commands.ActionBarCmd;
import io.github.tanguygab.tabadditions.spigot.commands.HelpCmd;
import io.github.tanguygab.tabadditions.spigot.commands.TagsCmd;
import io.github.tanguygab.tabadditions.spigot.commands.TitleCmd;
import me.neznamy.tab.api.TABAPI;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import org.bukkit.configuration.file.FileConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class TABAdditionsSpigot extends JavaPlugin implements CommandExecutor, TabCompleter {

    public FileConfiguration config;
    public FileConfiguration titleConfig;
    public FileConfiguration actionbarConfig;
    File titleFile = new File(getDataFolder(), "titles.yml");
    File actionbarFile = new File(getDataFolder(), "actionbars.yml");

    List<String> titles = new ArrayList<>();
    List<String> actionbars = new ArrayList<>();

    @Override
    public void onEnable() {reload();}

    public void reload() {
        saveDefaultConfig();
        reloadConfig();
        config = getConfig();
        if (!titleFile.exists())
            saveResource("titles.yml", false);
        if (!actionbarFile.exists())
            saveResource("actionbars.yml", false);

        titleConfig = new YamlConfiguration();
        actionbarConfig = new YamlConfiguration();
        try {
            titleConfig.load(titleFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        try {
            actionbarConfig.load(actionbarFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        titles.clear();
        titles.addAll(titleConfig.getConfigurationSection("titles").getKeys(false));
        actionbars.clear();
        actionbars.addAll(actionbarConfig.getConfigurationSection("bars").getKeys(false));
        HandlerList.unregisterAll(this);
        Bukkit.getServer().getPluginManager().registerEvents(new BukkitEvents(config,titleConfig,actionbarConfig), this);


    }

    @Override
    public void onDisable() {}

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1 || args[0].toLowerCase().equals("help"))
            new HelpCmd(sender, this.getDescription().getVersion());
        else
            switch (args[0].toLowerCase()) {
                case "reload": {
                    reload();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"Config reloaded!"));
                    break;
                }
                case "actionbar": {
                    new ActionBarCmd(sender, args, actionbarConfig);
                    break;
                }
                case "title": {
                    new TitleCmd(sender, args, titleConfig);
                    break;
                }
                case "tags": {
                    new TagsCmd(sender, args);
                    break;
                }
                case "test": {
                    sender.sendMessage("Nothing to see there :D");
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
                    return actionbars;
                case "tags": {
                    if (args.length == 2)
                        return new ArrayList<>(Arrays.asList("hide","show","toggle"));
                }
                case "title": {
                    if (args.length == 2)
                        return titles;
                }
            }
        }
        return null;
    }
}
