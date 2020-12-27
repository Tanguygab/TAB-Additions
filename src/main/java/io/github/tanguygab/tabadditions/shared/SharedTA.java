package io.github.tanguygab.tabadditions.shared;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.tanguygab.tabadditions.shared.layouts.LayoutManager;
import io.github.tanguygab.tabadditions.spigot.Features.NametagInRange;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.YamlConfigurationFile;
import org.bukkit.Bukkit;

public class SharedTA {

    public static Platform platform;
    public static Object plugin;
    public static YamlConfigurationFile config;
    public static YamlConfigurationFile layoutConfig;
    public static YamlConfigurationFile titleConfig;
    public static YamlConfigurationFile actionbarConfig;
    public static YamlConfigurationFile chatConfig;

    public static List<String> titles;
    public static List<String> actionbars;
    public static List<String> chatformats;

    public static boolean titlesEnabled;
    public static boolean actionbarsEnabled;
    public static boolean chatEnabled;
    public static boolean layoutEnabled;
    public static boolean sneakhideEnabled = false;
    public static int nametagInRange = 0;
    public static int nametagInRangeTask = -1;

    public static void reload(File dataFolder) {

        try {
            if (platform.type().equals("Bungee"))
                config = new YamlConfigurationFile(SharedTA.class.getClassLoader().getResourceAsStream("bungeeconfig.yml"), new File(dataFolder, "config.yml"));
            else
                config = new YamlConfigurationFile(SharedTA.class.getClassLoader().getResourceAsStream("config.yml"), new File(dataFolder, "config.yml"));
            titleConfig = new YamlConfigurationFile(SharedTA.class.getClassLoader().getResourceAsStream("titles.yml"), new File(dataFolder, "titles.yml"));
            actionbarConfig = new YamlConfigurationFile(SharedTA.class.getClassLoader().getResourceAsStream("actionbars.yml"), new File(dataFolder, "actionbars.yml"));
            chatConfig = new YamlConfigurationFile(SharedTA.class.getClassLoader().getResourceAsStream("chat.yml"), new File(dataFolder, "chat.yml"));
            layoutConfig = new YamlConfigurationFile(SharedTA.class.getClassLoader().getResourceAsStream("layout.yml"), new File(dataFolder, "layout.yml"));

            titlesEnabled = config.getBoolean("features.titles",false);
            actionbarsEnabled = config.getBoolean("features.actionbars",false);
            layoutEnabled = config.getBoolean("features.layout",false);
            chatEnabled = config.getBoolean("features.chat",false);
            if (platform.type().equals("Spigot")) {
                sneakhideEnabled = config.getBoolean("features.sneak-hide-nametags", false);
                nametagInRange = config.getInt("features.nametag-in-range", 0);
            }

            List<String> temp = new ArrayList<>();
            for (Object key : actionbarConfig.getConfigurationSection("bars").keySet())
                temp.add(key.toString());
            actionbars = new ArrayList<>(temp);

            temp.clear();
            for (Object key : titleConfig.getConfigurationSection("titles").keySet())
                temp.add(key.toString());
            titles = new ArrayList<>(temp);

            temp.clear();
            for (Object key : titleConfig.getConfigurationSection("chat").keySet())
                temp.add(key.toString());
            chatformats = new ArrayList<>(temp);

            for (TabPlayer p : Shared.getPlayers()) {
                loadProps(p);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        if (LayoutManager.getInstance() != null) {
            LayoutManager.getInstance().unregister();
        }
        if (layoutEnabled) {
            new LayoutManager();
            LayoutManager.getInstance().showLayoutAll();
        }

        if (nametagInRange != 0) {
            for (TabPlayer p : Shared.getPlayers())
                for (TabPlayer p2 : Shared.getPlayers())
                    if (p != p2)
                        p.hideNametag(p2.getUniqueId());
            if (nametagInRangeTask == -1) {
                nametagInRangeTask = new NametagInRange().load();
            }
        } else if (nametagInRangeTask != -1) {
            Bukkit.getServer().getScheduler().cancelTask(nametagInRangeTask);
            nametagInRangeTask = -1;
            for (TabPlayer p : Shared.getPlayers())
                for (TabPlayer p2 : Shared.getPlayers())
                    if (p != p2)
                        p.showNametag(p2.getUniqueId());
        }
    }

    public static void loadProps(TabPlayer p) {
        p.loadPropertyFromConfig("title");
        p.loadPropertyFromConfig("actionbar");
        p.loadPropertyFromConfig("chatprefix");
        p.loadPropertyFromConfig("customchatname", p.getName());
        p.loadPropertyFromConfig("chatsuffix");
    }
}