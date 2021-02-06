package io.github.tanguygab.tabadditions.shared;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import io.github.tanguygab.tabadditions.shared.features.Skins;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatManager;
import io.github.tanguygab.tabadditions.shared.features.layouts.LayoutManager;
import io.github.tanguygab.tabadditions.spigot.Features.NametagInRange;
import io.github.tanguygab.tabadditions.spigot.Features.TablistNamesRadius;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.YamlConfigurationFile;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.placeholders.Placeholder;
import org.bukkit.Bukkit;
import org.geysermc.floodgate.FloodgateAPI;

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
    public static int tablistNamesRadius = 0;
    public static boolean rfpEnabled;
    public static boolean floodgate = false;
    public static Map<String,Integer> tasks = new HashMap<>();

    public static boolean isCompatible() {
        try {
            YamlConfigurationFile.class.getConstructor(InputStream.class, File.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void reload(File dataFolder) {
        if (!isCompatible()) {
            platform.disable();
            return;
        }
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
            rfpEnabled = config.getBoolean("features.real-fake-players",false);
            if (platform.type().equals("Spigot")) {
                sneakhideEnabled = config.getBoolean("features.sneak-hide-nametags", false);
                nametagInRange = config.getInt("features.nametag-in-range", 0);
                tablistNamesRadius = config.getInt("features.tablist-names-radius", 0);
            }

            for (TabPlayer p : TAB.getInstance().getPlayers()) {
                loadProps(p);
            }
            loadLists();
            loadLayout();
            loadChat();
            loadNametagInRange();
            loadTablistNamesRadius();
            loadPlaceholders();
            loadFakePlayers();

            refresh();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static void loadProps(TabPlayer p) {
        p.loadPropertyFromConfig("title");
        p.loadPropertyFromConfig("actionbar");
        p.loadPropertyFromConfig("chatprefix");
        p.loadPropertyFromConfig("customchatname", p.getName());
        p.loadPropertyFromConfig("chatsuffix");
        p.loadPropertyFromConfig("moreheader");
        p.loadPropertyFromConfig("morefooter");

        Property footer = p.getProperty("footer");
        Property header = p.getProperty("header");

        if (header != null) {
            String moreheader = p.getProperty("moreheader").getCurrentRawValue();
            if (moreheader.length() > 2) {
                moreheader = moreheader.substring(1).substring(0, moreheader.length()-1);

                List<String> lines = Arrays.asList(moreheader.split(", "));

                String result = "";
                for (String line : lines) {
                    if (lines.indexOf(line) == lines.size() - 1)
                        result = result + "\n" + '\u00a7' + "r";
                    result = result + line;
                }
                header.setTemporaryValue(header.getCurrentRawValue() + "\n" + result);
            }
        }
        if (footer != null) {
            String morefooter = p.getProperty("morefooter").getCurrentRawValue();
            if (morefooter.length() > 2) {
                morefooter = morefooter.substring(1).substring(0, morefooter.length()-1);

                List<String> lines = Arrays.asList(morefooter.split(", "));

                String result = "";
                for (String line : lines) {
                    if (lines.indexOf(line) == lines.size() - 1)
                        result = result + "\n" + '\u00a7' + "r";
                    result = result + line;
                }
                footer.setTemporaryValue(footer.getCurrentRawValue() + "\n" + result);
            }
        }
    }

    private static void loadLists() {
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
    }
    private static void loadLayout() {
        if (LayoutManager.getInstance() != null) {
            LayoutManager.getInstance().unregister();
        }
        if (layoutEnabled && TAB.getInstance().isPremium()) {
            new LayoutManager();
            LayoutManager.getInstance().showLayoutAll();
        }
    }
    private static void loadChat() {
        if (chatEnabled) {
            new ChatManager();
        }
    }
    private static void loadNametagInRange() {
        if (nametagInRange != 0) {
            for (TabPlayer p : TAB.getInstance().getPlayers()) {
                for (TabPlayer p2 : TAB.getInstance().getPlayers()) {
                    if (p != p2)
                        p.hideNametag(p2.getUniqueId());
                }
            }
            if (!tasks.containsKey("Nametag-In-Range")) {
                tasks.put("Nametag-In-Range",new NametagInRange().load());
            }
        } else if (tasks.containsKey("Nametag-In-Range")) {
            Bukkit.getServer().getScheduler().cancelTask(tasks.get("Nametag-In-Range"));
            tasks.remove("Nametag-In-Range");
            for (TabPlayer p : TAB.getInstance().getPlayers())
                for (TabPlayer p2 : TAB.getInstance().getPlayers())
                    if (p != p2)
                        p.showNametag(p2.getUniqueId());
        }
    }
    private static void loadTablistNamesRadius() {
        if (tablistNamesRadius != 0) {
            if (tablistNamesRadius < 100)
                tablistNamesRadius = 100;
            for (TabPlayer p : TAB.getInstance().getPlayers()) {
                for (TabPlayer p2 : TAB.getInstance().getPlayers()) {
                    if (p != p2)
                        p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, new PacketPlayOutPlayerInfo.PlayerInfoData(p2.getUniqueId())));
                }
            }
            if (!tasks.containsKey("Tablist-Names-Radius")) {
                tasks.put("Tablist-Names-Radius",new TablistNamesRadius().load());
            }
        } else if (tasks.containsKey("Tablist-Names-Radius")) {
            Bukkit.getServer().getScheduler().cancelTask(tasks.get("Tablist-Names-Radius"));
            tasks.remove("Tablist-Names-Radius");
            for (TabPlayer p : TAB.getInstance().getPlayers())
                for (TabPlayer p2 : TAB.getInstance().getPlayers())
                    if (p != p2)
                        p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, new PacketPlayOutPlayerInfo.PlayerInfoData(p2.getUniqueId())));
        }

    }
    private static void loadPlaceholders() {
        List<String> props = Arrays.asList("tabprefix","tabsuffix","customtabname",
                "tagprefix","tagsuffix","customtagname",
                "chatprefix","chatsuffix","customchatname",
                "abovename","belowname","title","actionbar");
        for (String prop : props) {
            TAB.getInstance().getPlaceholderManager().registerPlaceholder(new Placeholder("%prop-"+prop+"%", 100) {
                @Override
                public String getLastValue(TabPlayer p) {
                    Property property = p.getProperty(prop);
                    if (property != null)
                        return property.updateAndGet();
                    return "";
                }
            });
        }
    }
    private static void loadFakePlayers() {
        if (rfpEnabled) {
            for (TabPlayer p : TAB.getInstance().getPlayers()) {
                List<PacketPlayOutPlayerInfo.PlayerInfoData> fps = new ArrayList<>();
                for (Object fp : config.getConfigurationSection("fakeplayers").keySet()) {
                    PacketPlayOutPlayerInfo.PlayerInfoData rfp = new PacketPlayOutPlayerInfo.PlayerInfoData(UUID.fromString(config.getString("fakeplayers." + fp + ".uuid")));
                    rfp.name = parsePlaceholders(config.getString("fakeplayers." + fp + ".name", fp + ""), p);
                    rfp.skin = Skins.getIcon(config.getString("fakeplayers." + fp + ".skin", ""), p);
                    int lat = config.getInt("fakeplayers." + fp + ".latency", 0);
                    if (lat <= 1)
                        rfp.latency = 1000;
                    if (lat == 2)
                        rfp.latency = 600;
                    if (lat == 3)
                        rfp.latency = 300;
                    if (lat >= 4)
                        rfp.latency = 200;

                    fps.add(rfp);
                }
                p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, fps));
            }
        }
    }

    private static void refresh() {
        tasks.put("Global-Refresh",platform.AsyncTask(()-> {
            List<TabPlayer> list = new ArrayList<>(TAB.getInstance().getPlayers());
            List<String> chatprops = new ArrayList<>(Arrays.asList("chatprefix","customchatname","chatsuffix"));
            for (TabPlayer p : list) {
                for (String prop : chatprops)
                    if (p.getProperty(prop) != null)
                        p.getProperty(prop).update();
                    else p.loadPropertyFromConfig(prop);
            }
        },0L,500L));
    }

    public static String parsePlaceholders(String str, TabPlayer p) {
        str = TAB.getInstance().getPlatform().replaceAllPlaceholders(str,p);
        str = TAB.getInstance().getPlatform().replaceAllPlaceholders(str,p);
        return str;
    }

    public static boolean checkBedrock(TabPlayer p) {
        if (!floodgate) return false;
        return FloodgateAPI.isBedrockPlayer(p.getUniqueId());
    }
}