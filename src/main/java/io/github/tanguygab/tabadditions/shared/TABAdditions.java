package io.github.tanguygab.tabadditions.shared;

import java.io.File;
import java.io.IOException;
import java.util.*;

import io.github.tanguygab.tabadditions.shared.features.*;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatManager;
import io.github.tanguygab.tabadditions.shared.features.layouts.LayoutManager;
import io.github.tanguygab.tabadditions.shared.features.rfps.RFPManager;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.FeatureManager;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.YamlConfigurationFile;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;
import me.neznamy.tab.shared.placeholders.ServerPlaceholder;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

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
    public boolean sithideEnabled = false;
    public boolean sneakhideEnabled = false;
    public int nametagInRange = 0;
    public int tablistNamesRadius = 0;
    public boolean rfpEnabled;
    public boolean onlyyou = false;
    public boolean unlimitedItemLines = false;

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
                sithideEnabled = config.getBoolean("features.sit-hide-nametags", false);
                sneakhideEnabled = config.getBoolean("features.sneak-hide-nametags", false);
                nametagInRange = config.getInt("features.nametag-in-range", 0);
                tablistNamesRadius = config.getInt("features.tablist-names-radius", 0);
                onlyyou = config.getBoolean("features.only-you",false);
                if (config.hasConfigOption("features.unlimited-item-lines") && Bukkit.getServer().getVersion().split(" ")[2].equals("1.17)"))
                    unlimitedItemLines = config.getBoolean("features.unlimited-item-lines");

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

        for (TAFeature feature : TAFeature.values()) {
            if (fm.isFeatureEnabled(feature.toString()) && fm.getFeature(feature.toString()) instanceof Loadable)
                ((Loadable) fm.getFeature(feature.toString())).unload();
            fm.unregisterFeature(feature.toString());
        }
        skins.unload();

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
            fm.registerFeature(TAFeature.ACTIONBAR.toString(), new ActionBar());
        //Title
        if (titlesEnabled)
            fm.registerFeature(TAFeature.TITLE.toString(), new Title());
        //Chat
        if (chatEnabled)
            fm.registerFeature(TAFeature.CHAT.toString(), new ChatManager());
        //Layout
        if (layoutEnabled && TAB.getInstance().isPremium())
            fm.registerFeature(TAFeature.TA_LAYOUT.toString(), new LayoutManager());
        //RFP
        if (rfpEnabled)
            fm.registerFeature(TAFeature.RFP.toString(), new RFPManager());
        //Sneak Hide Nametag
        if (sneakhideEnabled)
            fm.registerFeature(TAFeature.SNEAK_HIDE_NAMETAG.toString(), new SneakHideNametag());
        //Sneak Hide Nametag
        if (sithideEnabled)
            fm.registerFeature(TAFeature.SIT_HIDE_NAMETAG.toString(), new SitHideNametag());
        //Nametag in Range
        if (nametagInRange != 0)
            fm.registerFeature(TAFeature.NAMETAG_IN_RANGE.toString(), new NametagInRange());
        //Tablist Names Radius
        if (tablistNamesRadius != 0)
            fm.registerFeature(TAFeature.TABLIST_NAMES_RADIUS.toString(), new TablistNamesRadius());
        //Only You
        if (onlyyou)
            fm.registerFeature(TAFeature.ONLY_YOU.toString(), new OnlyYou());
        //Unlimited Item Lines
        if (TAB.getInstance().getFeatureManager().isFeatureEnabled("nametagx") && unlimitedItemLines)
            fm.registerFeature(TAFeature.UNLIMITED_ITEM_LINES.toString(), new UnlimitedItemLines());

        PlaceholderManager pm = TAB.getInstance().getPlaceholderManager();
        for (ConfigType cfg : ConfigType.values())
            if (cfg != ConfigType.SKINS)
                pm.findAllUsed(TABAdditions.getInstance().getConfig(cfg).getValues());
        pm.registerPlaceholders();
        pm.refreshPlaceholderUsage();
    }

    private void loadPlaceholders() {
        List<Object> props = new ArrayList<>(Arrays.asList("tabprefix","tabsuffix","customtabname",
                "tagprefix","tagsuffix","customtagname",
                "chatprefix","chatsuffix","customchatname",
                "abovename","belowname","title","actionbar"));
        if (TAB.getInstance().isPremium()) {
            props.addAll(TAB.getInstance().getConfiguration().getPremiumConfig().getStringList("unlimited-nametag-mode-dynamic-lines"));
            props.addAll(TAB.getInstance().getConfiguration().getPremiumConfig().getConfigurationSection("unlimited-nametag-mode-static-lines").keySet());
        }
        PlaceholderManager pm = TAB.getInstance().getPlaceholderManager();
        for (Object prop : props) {
            pm.registerPlaceholder(new PlayerPlaceholder("%prop-"+prop+"%", 100) {
                @Override
                public String get(TabPlayer p) {
                    Property property = p.getProperty(prop+"");
                    if (property != null)
                        return property.updateAndGet();
                    return "";
                }
            });
        }
        pm.registerPlaceholder(new ServerPlaceholder("%onlinerfp%",1000) {
            @Override
            public String get() {
                int count = TAB.getInstance().getPlayers().size();
                if (TAB.getInstance().getFeatureManager().isFeatureEnabled(TAFeature.RFP.toString()))
                    count = count+((RFPManager)TAB.getInstance().getFeatureManager().getFeature(TAFeature.RFP.toString())).getRFPS().size();
                return count+"";
            }
        });
        pm.registerPlaceholder(new PlayerPlaceholder("%canseeonlinerfp%",1000) {
            @Override
            public String get(TabPlayer p) {
                int count = TAB.getInstance().getPlayers().size();
                try {count = Integer.parseInt(parsePlaceholders("%canseeonline%",p));}
                catch (NumberFormatException ignored) {}
                if (TAB.getInstance().getFeatureManager().isFeatureEnabled(TAFeature.RFP.toString()))
                    count = count + ((RFPManager) TAB.getInstance().getFeatureManager().getFeature(TAFeature.RFP.toString())).getRFPS().size();
                return count+"";
            }
        });
        String world = platform.getType() == PlatformType.SPIGOT ? "world" : "server";
        pm.registerPlaceholder(new PlayerPlaceholder("%cansee"+world+"online%", 1000) {
            @Override
            public String get(TabPlayer p) {
                int count = 0;
                for (TabPlayer all : TAB.getInstance().getPlayers())
                    if (all.getWorldName().equals(p.getWorldName()) && ((Player) p.getPlayer()).canSee((Player) all.getPlayer()))
                        count++;
                return count+"";
            }
        });
        pm.registerPlaceholder(new PlayerPlaceholder("%cansee"+world+"onlinerfp%",1000) {
            @Override
            public String get(TabPlayer p) {
                int count = 0;
                try {count = Integer.parseInt(parsePlaceholders("%cansee"+world+"online%",p));}
                catch (NumberFormatException ignored) {}
                if (TAB.getInstance().getFeatureManager().isFeatureEnabled(TAFeature.RFP.toString()))
                    count = count + ((RFPManager) TAB.getInstance().getFeatureManager().getFeature(TAFeature.RFP.toString())).getRFPS().size();
                return count+"";
            }
        });
        platform.registerPlaceholders();
    }

    public String parsePlaceholders(String str, TabPlayer sender, TabPlayer viewer, TabPlayer def) {

        List<String> list = TAB.getInstance().getPlaceholderManager().detectAll(str);
        for (String pl : list) {
            if (pl.startsWith("%rel_")) {
                TabPlayer def2 = def == viewer ? sender : viewer;
                str = str.replace(pl, new Property(def, pl).getFormat(def2));
            }
            if (pl.startsWith("%sender:") && sender != null) {
                String pl2 = pl.replace("%sender:", "%");
                if (pl2.startsWith("%rel_"))
                    str = str.replace(pl,new Property(sender, pl2).getFormat(viewer));
                else str = str.replace(pl, parsePlaceholders(pl2, sender));
            }
            else if (pl.startsWith("%viewer:") && viewer != null) {
                String pl2 = pl.replace("%viewer:", "%");
                if (pl2.startsWith("%rel_"))
                    str = str.replace(pl,new Property(viewer, pl2).getFormat(sender));
                else str = str.replace(pl, parsePlaceholders(pl2, viewer));
            }
        }
        str = parsePlaceholders(str,def);

        return str;
    }

    public String parsePlaceholders(String str, TabPlayer p) {
        if (str == null) return "";
        if (p == null) return str;
        PlaceholderManager pm = TAB.getInstance().getPlaceholderManager();
        List<String> placeholders = pm.detectAll(str);
            for (String pl : placeholders) {
                str = str.replace(pl, new Property(p,pl).getFormat(p));
            }
            str = pm.color(str);
        return str;
    }

    public boolean isConditionMet(String str, TabPlayer p) {
        if (str == null || str.equals("null")) return true;
        String conditionname = TABAdditions.getInstance().parsePlaceholders(str,p);
        for (String cond : conditionname.split(";")) {
            String fcond = cond;
            if (fcond.startsWith("!"))
                fcond = fcond.replaceFirst("!", "");
            Condition condition = Condition.getCondition(fcond);
            if (condition != null) {
                if (cond.startsWith("!") && condition.isMet(p)) return false;
                if (!cond.startsWith("!") && !condition.isMet(p)) return false;
            }
        }
        return true;
    }

    public boolean isConditionMet(String str, TabPlayer sender, TabPlayer viewer, TabPlayer conditionPlayer) {
        if (sender == null || viewer == null) return false;
        String conditionname = TABAdditions.getInstance().parsePlaceholders(str,sender,viewer,viewer);
        for (String cond : conditionname.split(";")) {
            if (cond.startsWith("!inRange:") || cond.startsWith("inRange:")) {
                try {
                    int range = Integer.parseInt(cond.replace("!", "").replace("inRange:", ""));
                    boolean result = isInRange(sender, viewer, range);
                    if (cond.startsWith("!") && result) return false;
                    if (!cond.startsWith("!") && !result) return false;
                } catch (NumberFormatException ignored) {}
            } else {
                Condition condition = Condition.getCondition(cond.replace("!",""));
                if (condition != null) {
                    if (cond.startsWith("!") && condition.isMet(conditionPlayer)) return false;
                    if (!cond.startsWith("!") && !condition.isMet(conditionPlayer)) return false;
                }
            }
        }
        return true;
    }

    public boolean isInRange(TabPlayer sender,TabPlayer viewer,int range) {
        if (TABAdditions.getInstance().getPlatform().getType() == PlatformType.BUNGEE) return true;
        int zone = (int) Math.pow(range, 2);
        return sender.getWorldName().equals(viewer.getWorldName()) && ((Player) sender.getPlayer()).getLocation().distanceSquared(((Player) viewer.getPlayer()).getLocation()) < zone;
    }

    public void sendMessage(String name,String msg) {
        if (name.equals("~Console~"))
            TAB.getInstance().getPlatform().sendConsoleMessage(msg,true);
        else TAB.getInstance().getPlayer(name).sendMessage(msg,true);
    }
    public void sendMessage(String name, IChatBaseComponent msg) {
        if (name.equals("~Console~"))
            TAB.getInstance().getPlatform().sendConsoleMessage(msg.getText(),true);
        else TAB.getInstance().getPlayer(name).sendMessage(msg);
    }

    public boolean checkBedrock(TabPlayer p) {
        if (!platform.isPluginEnabled("Floodgate")) return false;
        return FloodgateApi.getInstance().isFloodgatePlayer(p.getUniqueId());
    }

    public boolean isMuted(TabPlayer p) {
        if (platform.isPluginEnabled("AdvancedBan")) {
            PunishmentManager punish = PunishmentManager.get();

            if (UUIDManager.get().getMode() != UUIDManager.FetcherMode.DISABLED)
                return punish.isMuted(p.getUniqueId().toString().replace("-", ""));
            return punish.isMuted(p.getName().toLowerCase());
        }
        return false;
    }
}