package io.github.tanguygab.tabadditions.shared;

import java.io.File;
import java.io.IOException;
import java.util.*;

import io.github.tanguygab.tabadditions.shared.features.ActionBar;
import io.github.tanguygab.tabadditions.shared.features.Skins;
import io.github.tanguygab.tabadditions.shared.features.SneakHideNametag;
import io.github.tanguygab.tabadditions.shared.features.Title;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatManager;
import io.github.tanguygab.tabadditions.shared.features.layouts.LayoutManager;
import io.github.tanguygab.tabadditions.shared.features.rfps.RFPManager;
import io.github.tanguygab.tabadditions.spigot.Features.NametagInRange;
import io.github.tanguygab.tabadditions.spigot.Features.TablistNamesRadius;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.FeatureManager;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.YamlConfigurationFile;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.placeholders.Placeholder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.geysermc.floodgate.FloodgateAPI;

public class TABAdditions {

    private static TABAdditions instance;
    private final Object plugin;
    private final Platform platform;
    private final File dataFolder;
    private final Skins skins;
    private final Map<String,Integer> tasks = new HashMap<>();

    private YamlConfigurationFile config;
    private YamlConfigurationFile layoutConfig;
    private YamlConfigurationFile titleConfig;
    private YamlConfigurationFile actionbarConfig;
    private YamlConfigurationFile chatConfig;

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

    public TABAdditions(Platform platform, Object plugin,File dataFolder) {
        this.dataFolder = dataFolder;
    	this.platform = platform;
    	this.plugin = plugin;
    	skins = new Skins();
    }
    
    public static void setInstance(TABAdditions instance) {
    	TABAdditions.instance = instance;
    }
    
    public static TABAdditions getInstance() {
        return instance;
    }
    
    public Platform getPlatform() {
        return platform;
    }
    
    public Object getPlugin() {
        return plugin;
    }

    public Skins getSkins() {
        return skins;
    }

    public YamlConfigurationFile getConfig(ConfigType cfg) {
        switch (cfg) {
            case LAYOUT: return layoutConfig;
            case TITLE: return titleConfig;
            case ACTIONBAR: return actionbarConfig;
            case CHAT: return chatConfig;
            default: return config;
        }
    }

    public void load() {
        loadFiles();
        platform.reload();
    }

    public void loadFiles() {
        try {
            if (platform.getType() == PlatformType.BUNGEE)
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
            if (platform.getType() == PlatformType.SPIGOT) {
                sneakhideEnabled = config.getBoolean("features.sneak-hide-nametags", false);
                nametagInRange = config.getInt("features.nametag-in-range", 0);
                tablistNamesRadius = config.getInt("features.tablist-names-radius", 0);
            }
        } catch (IOException e) {
            platform.disable();
            e.printStackTrace();
        }
    }

    public void reload() {
        loadFiles();
        loadFeatures();
        for (TabPlayer p : TAB.getInstance().getPlayers()) {
            loadProps(p);
        }

        loadLists();
        loadTablistNamesRadius();
        loadPlaceholders();

        refresh();
    }

    public void disable() {
        if (RFPManager.getInstance() != null) RFPManager.getInstance().unload();
    }

    protected void loadProps(TabPlayer p) {
        p.loadPropertyFromConfig("title");
        p.loadPropertyFromConfig("actionbar");
        p.loadPropertyFromConfig("chatprefix");
        p.loadPropertyFromConfig("customchatname", p.getName());
        p.loadPropertyFromConfig("chatsuffix");

    }

    private void loadFeatures() {
        FeatureManager fm = TAB.getInstance().getFeatureManager();
        //ActionBar
        if (actionbarsEnabled)
            fm.registerFeature("ActionBar", new ActionBar(TabFeature.ADDON_FEATURE_1));
        //Title
        if (titlesEnabled)
            fm.registerFeature("Title", new Title(TabFeature.ADDON_FEATURE_2));
        //Chat
        if (chatEnabled)
            fm.registerFeature("Chat", new ChatManager(TabFeature.ADDON_FEATURE_3));
        //Layout ADDON_4
        if (layoutEnabled && TAB.getInstance().isPremium())
            fm.registerFeature("TAB+ Layout",new LayoutManager(TabFeature.ADDON_FEATURE_4));
        //RFP
        if (rfpEnabled)
            fm.registerFeature("Real Fake Players",new RFPManager(TabFeature.ADDON_FEATURE_5));
        //Sneak Hide Nametag
        if (sneakhideEnabled)
            fm.registerFeature("Sneak Hide Nametag", new SneakHideNametag(TabFeature.ADDON_FEATURE_6));
        //Nametag in Range
        if (nametagInRange != 0)
            fm.registerFeature("Nametag in Range",new NametagInRange(TabFeature.ADDON_FEATURE_7));
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

    private void loadTablistNamesRadius() {
        if (tablistNamesRadius != 0) {
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                for (Player p2 : Bukkit.getServer().getOnlinePlayers()) {
                    if (p != p2)
                        p.hidePlayer((Plugin) plugin,p2);
                }
            }
            if (!tasks.containsKey("Tablist-Names-Radius")) {
                tasks.put("Tablist-Names-Radius",new TablistNamesRadius().load());
            }
        } else if (tasks.containsKey("Tablist-Names-Radius")) {
            Bukkit.getServer().getScheduler().cancelTask(tasks.get("Tablist-Names-Radius"));
            tasks.remove("Tablist-Names-Radius");
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                for (Player p2 : Bukkit.getServer().getOnlinePlayers()) {
                    if (p != p2)
                        p.showPlayer((Plugin) plugin, p2);
                }
            }
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
    }

    private void refresh() {
        TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(500,"refreshing chat propreties",TabFeature.ADDON_FEATURE_3, UsageType.REPEATING_TASK,()-> {
            List<TabPlayer> list = new ArrayList<>(TAB.getInstance().getPlayers());
            List<String> props = new ArrayList<>(Arrays.asList("chatprefix","customchatname","chatsuffix"));
            for (TabPlayer p : list) {
                for (String prop : props) {
                    p.loadPropertyFromConfig(prop);
                    if (p.getProperty(prop) != null) {
                        p.getProperty(prop).update();
                    }
                }
            }
        });
    }

    public String parsePlaceholders(String str, TabPlayer p) {
        str = TAB.getInstance().getPlatform().replaceAllPlaceholders(str,p);
        str = TAB.getInstance().getPlatform().replaceAllPlaceholders(str,p);
        if (platform.getType() == PlatformType.BUNGEE)
            str = ChatColor.translateAlternateColorCodes('&',str);
        return str;
    }

    public void sendMessage(String name,String msg) {
        if (name.equals("~Console~"))
            TAB.getInstance().getPlatform().sendConsoleMessage(msg,true);
        else TAB.getInstance().getPlayer(name).sendMessage(msg,true);
    }

    public boolean checkBedrock(TabPlayer p) {
        if (!floodgate) return false;
        return FloodgateAPI.isBedrockPlayer(p.getUniqueId());
    }
}