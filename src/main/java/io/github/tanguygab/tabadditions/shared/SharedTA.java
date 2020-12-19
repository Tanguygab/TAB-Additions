package io.github.tanguygab.tabadditions.shared;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.YamlConfigurationFile;
import net.md_5.bungee.api.ProxyServer;

public class SharedTA {

    public static String platform;
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
            SharedTA.config = new YamlConfigurationFile(SharedTA.class.getClassLoader().getResourceAsStream("config.yml"), new File(dataFolder, "config.yml"));
            SharedTA.titleConfig = new YamlConfigurationFile(SharedTA.class.getClassLoader().getResourceAsStream("titles.yml"), new File(dataFolder, "titles.yml"));
            SharedTA.actionbarConfig = new YamlConfigurationFile(SharedTA.class.getClassLoader().getResourceAsStream("actionbars.yml"), new File(dataFolder, "actionbars.yml"));
            SharedTA.chatConfig = new YamlConfigurationFile(SharedTA.class.getClassLoader().getResourceAsStream("chat.yml"), new File(dataFolder, "chat.yml"));
            SharedTA.layoutConfig = new YamlConfigurationFile(SharedTA.class.getClassLoader().getResourceAsStream("layout.yml"), new File(dataFolder, "layout.yml"));

            titlesEnabled = config.getBoolean("features.titles",false);
            actionbarsEnabled = config.getBoolean("features.actionbars",false);
            chatEnabled = config.getBoolean("features.chat",false);
            sneakhideEnabled = config.getBoolean("features.sneak-hide-nametags",false);
            layoutEnabled = config.getBoolean("features.layout",false);

            List<String> temp = new ArrayList<>();
            for (Object key : SharedTA.actionbarConfig.getConfigurationSection("bars").keySet())
                temp.add(key.toString());
            SharedTA.actionbars = new ArrayList<>(temp);

            temp.clear();
            for (Object key : SharedTA.titleConfig.getConfigurationSection("titles").keySet())
                temp.add(key.toString());
            SharedTA.titles = new ArrayList<>(temp);

            temp.clear();
            for (Object key : SharedTA.titleConfig.getConfigurationSection("chat").keySet())
                temp.add(key.toString());
            SharedTA.chatformats = new ArrayList<>(temp);

            for (TabPlayer p : Shared.getPlayers()) {
                SharedTA.loadProps(p);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }



        Layout.getInstance().removeAll();
        for (int i : Layout.getInstance().tasks.values()) {
            if (platform.equals("Bukkit"))
                Bukkit.getServer().getScheduler().cancelTask(i);
            else if (platform.equals("Bungee"))
                ProxyServer.getInstance().getScheduler().cancel(i);
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

    public static int AsyncTask(Runnable r, long delay, long period) {
        if (platform.equals("Bukkit"))
            return Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask((Plugin) plugin, r, delay, period);
        else if (platform.equals("Bungee"))
            return ProxyServer.getInstance().getScheduler().schedule((net.md_5.bungee.api.plugin.Plugin) plugin,r,delay/20,period/20, TimeUnit.SECONDS).getId();
        return -1;
    }
    public static void AsyncTask(Runnable r, long delay) {
        if (platform.equals("Bukkit"))
            Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask((Plugin) plugin,r,delay);
        else if (platform.equals("Bungee"))
            ProxyServer.getInstance().getScheduler().schedule((net.md_5.bungee.api.plugin.Plugin) plugin,r,delay/20,TimeUnit.SECONDS);
    }


}
