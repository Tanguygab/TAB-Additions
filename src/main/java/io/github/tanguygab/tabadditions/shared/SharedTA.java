package io.github.tanguygab.tabadditions.shared;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.YamlConfigurationFile;

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
    public static boolean sneakhideEnabled;
    public static boolean layoutEnabled;

    public static void reload(File dataFolder) {

        try {
            config = new YamlConfigurationFile(SharedTA.class.getClassLoader().getResourceAsStream("config.yml"), new File(dataFolder, "config.yml"));
            titleConfig = new YamlConfigurationFile(SharedTA.class.getClassLoader().getResourceAsStream("titles.yml"), new File(dataFolder, "titles.yml"));
            actionbarConfig = new YamlConfigurationFile(SharedTA.class.getClassLoader().getResourceAsStream("actionbars.yml"), new File(dataFolder, "actionbars.yml"));
            chatConfig = new YamlConfigurationFile(SharedTA.class.getClassLoader().getResourceAsStream("chat.yml"), new File(dataFolder, "chat.yml"));
            layoutConfig = new YamlConfigurationFile(SharedTA.class.getClassLoader().getResourceAsStream("layout.yml"), new File(dataFolder, "layout.yml"));

            titlesEnabled = config.getBoolean("features.titles",false);
            actionbarsEnabled = config.getBoolean("features.actionbars",false);
            chatEnabled = config.getBoolean("features.chat",false);
            sneakhideEnabled = config.getBoolean("features.sneak-hide-nametags",false);
            layoutEnabled = config.getBoolean("features.layout",false);

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



        Layout.getInstance().removeAll();
        for (int i : Layout.getInstance().tasks.values()) {
        	platform.cancelTask(i);
        }
        Layout.getInstance().tasks.clear();
        if (layoutEnabled) {
            new Layout();
            Layout.getInstance().addAll();
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