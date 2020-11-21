package io.github.tanguygab.tabadditions.spigot;

import io.github.tanguygab.tabadditions.shared.commands.*;
import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.api.TabPlayer;

import org.bukkit.Bukkit;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
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

    private FileConfiguration config;
    private FileConfiguration titleConfig;
    private FileConfiguration actionbarConfig;
    private FileConfiguration chatConfig;
    private final File titleFile = new File(getDataFolder(), "titles.yml");
    private final File actionbarFile = new File(getDataFolder(), "actionbars.yml");
    private final File chatFile = new File(getDataFolder(), "chat.yml");

    private final List<String> titles = new ArrayList<>();
    private final List<String> actionbars = new ArrayList<>();
    private final List<String> chatformats = new ArrayList<>();

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
        if (!chatFile.exists())
            saveResource("chat.yml", false);

        titleConfig = new YamlConfiguration();
        actionbarConfig = new YamlConfiguration();;
        chatConfig = new YamlConfiguration();
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
        try {
            chatConfig.load(chatFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        titles.clear();
        titles.addAll(titleConfig.getConfigurationSection("titles").getKeys(false));
        actionbars.clear();
        actionbars.addAll(actionbarConfig.getConfigurationSection("bars").getKeys(false));
        chatformats.clear();
        chatformats.addAll(chatConfig.getConfigurationSection("chat-formats").getKeys(false));
        HandlerList.unregisterAll(this);
        Bukkit.getServer().getPluginManager().registerEvents(new BukkitEvents(this, config, titleConfig, actionbarConfig, chatConfig), this);
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new TABAdditionsExpansion(this).register();
        }

        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            TabPlayer pTAB = TABAPI.getPlayer(p.getUniqueId());
            loadProps(pTAB);
        }

    }
    public void loadProps(TabPlayer pTAB) {
        pTAB.loadPropertyFromConfig("title");
        pTAB.loadPropertyFromConfig("actionbar");
        pTAB.loadPropertyFromConfig("chatprefix");
        pTAB.loadPropertyFromConfig("customchatname", pTAB.getName());
        pTAB.loadPropertyFromConfig("chatsuffix");
    }



    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        TabPlayer pTAB = TABAPI.getPlayer(sender.getName());
        if (args.length < 1 || args[0].toLowerCase().equals("help"))
            new HelpCmd(pTAB, this.getDescription().getVersion());
        else
            switch (args[0].toLowerCase()) {
                case "reload": {
                    reload();
                    pTAB.sendMessage("&aConfig reloaded!",true);
                    break;
                }
                case "actionbar": {
                    if (!config.getBoolean("features.actionbars"))
                        pTAB.sendMessage("&cActionbar feature is not enabled, therefore this command cannot be used",true);
                    else if (args.length < 2)
                        pTAB.sendMessage("&cYou have to provide an actionbar!",true);
                    else {
                        ConfigurationSection section = actionbarConfig.getConfigurationSection("bars.");
                        if (!section.contains(args[1]))
                            pTAB.sendMessage("&cThis actionbar doesn't exist!",true);
                        else
                            new ActionBarCmd(pTAB, args, section.getString(args[1]));
                    }
                    break;
                }
                case "title": {
                    if (!config.getBoolean("features.titles"))
                        pTAB.sendMessage("&cTitle feature is not enabled, therefore this command cannot be used",true);
                    else if (args.length < 2)
                        pTAB.sendMessage("&cYou have to provide a title!",true);
                    else {
                        ConfigurationSection titleSection = titleConfig.getConfigurationSection("titles."+args[1]);
                        if (titleSection == null) {
                            pTAB.sendMessage("&cThis title doesn't exist!",true);
                        }
                        else {
                            List<String> titleProperties = new ArrayList<>();
                            for (String property : titleSection.getKeys(false))
                                titleProperties.add(titleSection.getString(property));
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
