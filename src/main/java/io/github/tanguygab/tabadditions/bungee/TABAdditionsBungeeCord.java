package io.github.tanguygab.tabadditions.bungee;

import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public final class TABAdditionsBungeeCord extends Plugin {

    public static Configuration config;
    public static Configuration titleConfig;
    public static Configuration actionbarConfig;

    private final File configFile = new File(getDataFolder(), "config.yml");
    private final File titleFile = new File(getDataFolder(), "titles.yml");
    private final File actionbarFile = new File(getDataFolder(), "actionbars.yml");

    private final List<String> titles = new ArrayList<>();
    private final List<String> actionbars = new ArrayList<>();

    @Override
    public void onEnable() {
        try {reload();}
        catch (IOException e) {e.printStackTrace();}
        getProxy().getPluginManager().registerListener(this, new BungeeEvents(config,titleConfig,actionbarConfig));
        getProxy().getPluginManager().registerCommand(this, new MainCmd("btabadditions","tabadditions.admin","btab+","btaba","tabaddon","tabaddition"));

    }

    public void reload() throws IOException {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                try (InputStream is = getResourceAsStream("config.yml");
                     OutputStream os = new FileOutputStream(configFile)) {
                    ByteStreams.copy(is, os);
                }
            }
            catch (IOException e) {e.printStackTrace();}
        }
        if (!titleFile.exists()) {
            try {
                titleFile.createNewFile();
                try (InputStream is = getResourceAsStream("titles.yml");
                     OutputStream os = new FileOutputStream(titleFile)) {
                    ByteStreams.copy(is, os);
                }
            }
            catch (IOException e) {e.printStackTrace();}
        }
        if (!actionbarFile.exists()) {
            try {
                actionbarFile.createNewFile();
                try (InputStream is = getResourceAsStream("actionbars.yml");
                     OutputStream os = new FileOutputStream(actionbarFile)) {
                    ByteStreams.copy(is, os);
                }
            }
            catch (IOException e) {e.printStackTrace();}
        }

        config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        titleConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "titles.yml"));
        actionbarConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "actionbars.yml"));
    }


    @Override
    public void onDisable() {}

}
