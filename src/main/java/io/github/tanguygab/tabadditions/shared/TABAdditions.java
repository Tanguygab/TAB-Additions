package io.github.tanguygab.tabadditions.shared;

import java.io.File;
import java.io.IOException;
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

public class TABAdditions {

    private static TABAdditions instance;
    public TABAdditions() {
        instance = this;
    }

    private Object plugin;
    private Platform platform;
    private final Map<String,Integer> tasks = new HashMap<>();

    private YamlConfigurationFile config;
    public YamlConfigurationFile layoutConfig;
    public YamlConfigurationFile titleConfig;
    public YamlConfigurationFile actionbarConfig;
    public YamlConfigurationFile chatConfig;

    public List<String> titles;
    public List<String> actionbars;
    public List<String> chatformats;

    public boolean titlesEnabled;
    public boolean actionbarsEnabled;
    public boolean chatEnabled;
    public boolean layoutEnabled;
    public boolean sneakhideEnabled = false;
    public int nametagInRange = 0;
    public int tablistNamesRadius = 0;
    public boolean rfpEnabled;

    public boolean floodgate = false;

    public static TABAdditions getInstance() {
        return instance;
    }
    public Platform getPlatform() {
        return platform;
    }
    public Object getPlugin() {
        return plugin;
    }
    public void setPlatform(Platform platform) {
        this.platform = platform;
    }
    public void setPlugin(Object plugin) {
        this.plugin = plugin;
    }
    public YamlConfigurationFile getConfig(String cfg) {
        switch (cfg) {
            case "layout": return layoutConfig;
            case "title": return titleConfig;
            case "actionbar": return actionbarConfig;
            case "chat": return chatConfig;
            default: return config;
        }
    }

    public void reload(File dataFolder) {
        try {
            if (platform.type().equals("Bungee"))
                config = new YamlConfigurationFile(TABAdditions.class.getClassLoader().getResourceAsStream("bungeeconfig.yml"), new File(dataFolder, "config.yml"));
            else
                config = new YamlConfigurationFile(TABAdditions.class.getClassLoader().getResourceAsStream("config.yml"), new File(dataFolder, "config.yml"));
            titleConfig = new YamlConfigurationFile(TABAdditions.class.getClassLoader().getResourceAsStream("titles.yml"), new File(dataFolder, "titles.yml"));
            actionbarConfig = new YamlConfigurationFile(TABAdditions.class.getClassLoader().getResourceAsStream("actionbars.yml"), new File(dataFolder, "actionbars.yml"));
            chatConfig = new YamlConfigurationFile(TABAdditions.class.getClassLoader().getResourceAsStream("chat.yml"), new File(dataFolder, "chat.yml"));
            layoutConfig = new YamlConfigurationFile(TABAdditions.class.getClassLoader().getResourceAsStream("layout.yml"), new File(dataFolder, "layout.yml"));

            titlesEnabled = config.getBoolean("features.titles",false);
            actionbarsEnabled = config.getBoolean("features.actionbars",false);
            layoutEnabled = config.getBoolean("features.layout",false);
            rfpEnabled = config.getBoolean("features.real-fake-players",false);
            chatEnabled = config.getBoolean("features.chat",false);
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
            platform.disable();
            e.printStackTrace();
        }
    }

    protected void loadProps(TabPlayer p) {
        p.loadPropertyFromConfig("title");
        p.loadPropertyFromConfig("actionbar");
        p.loadPropertyFromConfig("chatprefix");
        p.loadPropertyFromConfig("customchatname", p.getName());
        p.loadPropertyFromConfig("chatsuffix");
        p.loadPropertyFromConfig("moreheader");
        p.loadPropertyFromConfig("morefooter");

    }

    private void loadLists() {
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
    private void loadLayout() {
        if (LayoutManager.getInstance() != null) {
            LayoutManager.getInstance().unregister();
        }
        if (layoutEnabled && TAB.getInstance().isPremium()) {
            new LayoutManager();
            LayoutManager.getInstance().showLayoutAll();
        }
    }
    private void loadChat() {
        if (chatEnabled) {
            new ChatManager();
        }
    }
    private void loadNametagInRange() {
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
    private void loadTablistNamesRadius() {
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
    private void loadPlaceholders() {
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
        List<String> moreprops = new ArrayList<>(Arrays.asList("moreheader","morefooter"));
        for (String prop : moreprops) {
            TAB.getInstance().getPlaceholderManager().registerPlaceholder(new Placeholder("%"+prop+"%",100) {
                @Override
                public String getLastValue(TabPlayer p) {
                    return getMoreProps(p,prop);
                }
            });
        }
    }
    private void loadFakePlayers() {
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

    private String getMoreProps(TabPlayer p, String prop) {
        if (p.getProperty(prop) != null) {
            String more = p.getProperty(prop).updateAndGet();
            if (more.startsWith("[") && more.endsWith("]")) {
                more = more.substring(1).substring(0, more.length()-2);

                List<String> lines = Arrays.asList(more.split(", "));

                String result = "";
                for (String line : lines) {
                    if (lines.indexOf(line) == lines.size() - 1)
                        result = result + "\n" + '\u00a7' + "r";
                    result = result + line;
                }
                return result;
            }
            return "\n"+'\u00a7'+"r"+p.getProperty(prop).updateAndGet();
        }
        return "5";
    }

    private void refresh() {
        tasks.put("Global-Refresh",platform.AsyncTask(()-> {
            List<TabPlayer> list = new ArrayList<>(TAB.getInstance().getPlayers());
            List<String> props = new ArrayList<>(Arrays.asList("chatprefix","customchatname","chatsuffix","moreheader","morefooter"));
            for (TabPlayer p : list) {
                for (String prop : props) {
                    p.loadPropertyFromConfig(prop);
                    if (p.getProperty(prop) != null) {
                        p.getProperty(prop).update();
                    }
                }
            }
        },0L,500L));
    }

    public String parsePlaceholders(String str, TabPlayer p) {
        str = TAB.getInstance().getPlatform().replaceAllPlaceholders(str,p);
        str = TAB.getInstance().getPlatform().replaceAllPlaceholders(str,p);
        return str;
    }

    public boolean checkBedrock(TabPlayer p) {
        if (!floodgate) return false;
        return FloodgateAPI.isBedrockPlayer(p.getUniqueId());
    }
}