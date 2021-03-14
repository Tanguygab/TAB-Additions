package io.github.tanguygab.tabadditions.shared;

import java.io.File;
import java.io.IOException;
import java.util.*;

import io.github.tanguygab.tabadditions.shared.features.*;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatManager;
import io.github.tanguygab.tabadditions.shared.features.layouts.LayoutManager;
import io.github.tanguygab.tabadditions.shared.features.rfps.RFPManager;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.FeatureManager;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.YamlConfigurationFile;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.placeholders.Placeholder;
import org.geysermc.floodgate.FloodgateAPI;

public class TABAdditions {

    private static TABAdditions instance;
    private final Object plugin;
    private final Platform platform;
    public final File dataFolder;
    private final Skins skins;
    public boolean enabled;

    private YamlConfigurationFile config;
    private YamlConfigurationFile layoutConfig;
    private YamlConfigurationFile titleConfig;
    private YamlConfigurationFile actionbarConfig;
    private YamlConfigurationFile chatConfig;
    private YamlConfigurationFile skinsFile;

    public boolean titlesEnabled;
    public boolean actionbarsEnabled;
    public boolean chatEnabled;
    public boolean layoutEnabled;
    public boolean sneakhideEnabled = false;
    public int nametagInRange = 0;
    public int tablistNamesRadius = 0;
    public boolean rfpEnabled;
    public boolean onlyyou = false;

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
            case SKINS: return skinsFile;
            default: return config;
        }
    }

    public void load() {
        enabled = true;
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
            skinsFile = new YamlConfigurationFile(TABAdditions.class.getClassLoader().getResourceAsStream("skins.yml"), new File(dataFolder, "skins.yml"));
            if (TAB.getInstance().isPremium())
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
                onlyyou = config.getBoolean("features.only-you",false);
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

        loadPlaceholders();
    }

    public void disable() {
        FeatureManager fm = TAB.getInstance().getFeatureManager();

        //ActionBar
        fm.getFeature("ActionBar");

        //Title
        fm.unregisterFeature("Title");

        //Chat
        if (fm.isFeatureEnabled("Chat"))
            ((ChatManager)fm.getFeature("Chat")).unload();
        fm.unregisterFeature("Chat");

        //Layout
        if (fm.isFeatureEnabled("TAB+ Layout"))
            ((LayoutManager)fm.getFeature("TAB+ Layout")).unload();
        fm.unregisterFeature("TAB+ Layout");
        //RFP
        if (fm.isFeatureEnabled("Real Fake Players"))
            ((RFPManager)fm.getFeature("Real Fake Players")).unload();
        fm.unregisterFeature("Real Fake Players");

        //Sneak Hide Nametag
        fm.unregisterFeature("Sneak Hide Nametag");

        //Nametag in Range
        if (fm.isFeatureEnabled("Nametag in Range"))
            ((NametagInRange)fm.getFeature("Nametag in Range")).unload();
        fm.unregisterFeature("Nametag in Range");

        //Tablist Names Radius
        if (fm.isFeatureEnabled("Tablist Names Radius"))
            ((TablistNamesRadius)fm.getFeature("Tablist Names Radius")).unload();
        fm.unregisterFeature("Tablist Names Radius");

        //Only You
        fm.unregisterFeature("Only You");

        enabled = false;
        TAB.getInstance().unload();
        TAB.getInstance().load();
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
        //Layout
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
        //Tablist Names Radius
        if (tablistNamesRadius != 0)
            fm.registerFeature("Tablist Names Radius",new TablistNamesRadius(TabFeature.ADDON_FEATURE_8));
        //Only You
        if (onlyyou)
            fm.registerFeature("Only You",new OnlyYou(TabFeature.ADDON_FEATURE_9));
    }

    private void loadPlaceholders() {
        List<Object> props = new ArrayList<>(Arrays.asList("tabprefix","tabsuffix","customtabname",
                "tagprefix","tagsuffix","customtagname",
                "chatprefix","chatsuffix","customchatname",
                "abovename","belowname","title","actionbar"));
        if (TAB.getInstance().isPremium()) {
            props.addAll(TAB.getInstance().getConfiguration().premiumconfig.getStringList("unlimited-nametag-mode-dynamic-lines"));
            props.addAll(TAB.getInstance().getConfiguration().premiumconfig.getConfigurationSection("unlimited-nametag-mode-static-lines").keySet());
        }
        for (Object prop : props) {
            TAB.getInstance().getPlaceholderManager().registerPlaceholder(new Placeholder("%prop-"+prop+"%", 100) {
                @Override
                public String getLastValue(TabPlayer p) {
                    Property property = p.getProperty(prop+"");
                    if (property != null)
                        return property.updateAndGet();
                    return "";
                }
            });
        }
    }

    public String parsePlaceholders(String str, TabPlayer p) {
        str = TAB.getInstance().getPlatform().replaceAllPlaceholders(str,p);
        str = TAB.getInstance().getPlatform().replaceAllPlaceholders(str,p);
        str = TAB.getInstance().getPlaceholderManager().color(str);
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