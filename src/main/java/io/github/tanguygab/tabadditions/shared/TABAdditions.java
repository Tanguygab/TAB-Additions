package io.github.tanguygab.tabadditions.shared;

import java.io.File;
import java.io.IOException;
import java.util.*;

import io.github.tanguygab.tabadditions.shared.features.*;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatManager;
import io.github.tanguygab.tabadditions.shared.features.rfps.RFPManager;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import me.neznamy.tab.api.*;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.api.config.YamlConfigurationFile;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.RelationalPlaceholder;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

public class TABAdditions {

    private static TABAdditions instance;
    private final Object plugin;
    private final Platform platform;
    public final File dataFolder;
    private final Skins skins;
    private final TabAPI tab;
    public final Map<String, TabFeature> features = new HashMap<>();
    public boolean enabled;

    private YamlConfigurationFile config;
    private YamlConfigurationFile titleConfig;
    private YamlConfigurationFile actionbarConfig;
    private YamlConfigurationFile chatConfig;
    private YamlConfigurationFile skinsFile;

    public boolean titlesEnabled;
    public boolean actionbarsEnabled;
    public boolean chatEnabled;
    public boolean sithideEnabled = false;
    public boolean sneakhideEnabled = false;
    public int nametagInRange = 0;
    public int tablistNamesRadius = 0;
    public boolean rfpEnabled;
    public boolean onlyyou = false;

    public TABAdditions(Platform platform, Object plugin,File dataFolder) {
        this.dataFolder = dataFolder;
    	this.platform = platform;
    	this.plugin = plugin;
    	tab = TabAPI.getInstance();
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
            case TITLE: return titleConfig;
            case ACTIONBAR: return actionbarConfig;
            case CHAT: return chatConfig;
            case SKINS: return skinsFile;
            default: return config;
        }
    }

    // I'll have to change that later
    public ConfigurationFile getTranslation() {
        return TAB.getInstance().getConfiguration().getTranslation();
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

            titlesEnabled = config.getBoolean("features.titles",false);
            actionbarsEnabled = config.getBoolean("features.actionbars",false);
            rfpEnabled = config.getBoolean("features.real-fake-players",false);
            chatEnabled = config.getBoolean("features.chat",false);
            if (platform.getType() == PlatformType.SPIGOT) {
                sithideEnabled = config.getBoolean("features.sit-hide-nametags", false);
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
        loadPlaceholders();
    }

    public void disable() {
        FeatureManager fm = tab.getFeatureManager();

        for (String feature : features.keySet()) {
            if (fm.isFeatureEnabled(feature))
                fm.getFeature(feature).unload();
            fm.unregisterFeature(feature);
        }
        skins.unload();

        enabled = false;
    }

    public void registerFeature(TabFeature feature) {
        FeatureManager fm = tab.getFeatureManager();
        fm.registerFeature(feature.getFeatureName(),feature);
    }

    private void loadFeatures() {
        //ActionBar
        if (actionbarsEnabled)
            registerFeature(new ActionBar());
        //Title
        if (titlesEnabled)
            registerFeature(new Title());
        //Chat
        if (chatEnabled)
            registerFeature(new ChatManager());
        //RFP
        if (rfpEnabled)
            registerFeature(new RFPManager());

    }

    private void loadPlaceholders() {
        Set<Object> props = new HashSet<>(Arrays.asList("tabprefix","tabsuffix","customtabname",
                "tagprefix","tagsuffix","customtagname",
                "chatprefix","chatsuffix","customchatname",
                "abovename","belowname","title","actionbar"));
        ConfigurationFile cfg = tab.getConfig();
        if (cfg.getStringList("scoreboard-teams.unlimited-nametag-mode.dynamic-lines") != null)
            props.addAll(cfg.getStringList("scoreboard-teams.unlimited-nametag-mode.dynamic-lines"));
        if (cfg.getConfigurationSection("scoreboard-teams.unlimited-nametag-mode.static-lines") != null)
        props.addAll(cfg.getConfigurationSection("scoreboard-teams.unlimited-nametag-mode.static-lines").keySet());
        PlaceholderManager pm = tab.getPlaceholderManager();
        for (Object prop : props) {
            pm.registerPlayerPlaceholder("%prop-"+prop+"%", 100,p->{
                    Property property = p.getProperty(prop+"");
                    if (property != null)
                        return property.updateAndGet();
                    return "";
            });
        }
        pm.registerServerPlaceholder("%onlinerfp%",1000,()->{
                int count = tab.getOnlinePlayers().length;
                if (tab.getFeatureManager().isFeatureEnabled("&aReal Fake Players&r"))
                    count = count+((RFPManager)tab.getFeatureManager().getFeature("&aReal Fake Players&r")).getRFPS().size();
                return count+"";
        });
        pm.registerPlayerPlaceholder("%canseeonlinerfp%",1000,p->{
                int count = tab.getOnlinePlayers().length;
                try {count = Integer.parseInt(parsePlaceholders("%canseeonline%",p));}
                catch (NumberFormatException ignored) {}
                if (tab.getFeatureManager().isFeatureEnabled("&aReal Fake Players&r"))
                    count = count + ((RFPManager) tab.getFeatureManager().getFeature("&aReal Fake Players&r")).getRFPS().size();
                return count+"";
        });
        String world = platform.getType() == PlatformType.SPIGOT ? "world" : "server";
        pm.registerPlayerPlaceholder("%cansee"+world+"online%", 1000,p->{
                int count = 0;
                for (TabPlayer all : tab.getOnlinePlayers())
                    if (all.getWorld().equals(p.getWorld()) && ((Player) p.getPlayer()).canSee((Player) all.getPlayer()))
                        count++;
                return count+"";
        });
        pm.registerPlayerPlaceholder("%cansee"+world+"onlinerfp%",1000,p->{
                int count = 0;
                try {count = Integer.parseInt(parsePlaceholders("%cansee"+world+"online%",p));}
                catch (NumberFormatException ignored) {}
                if (tab.getFeatureManager().isFeatureEnabled("&aReal Fake Players&r"))
                    count = count + ((RFPManager) tab.getFeatureManager().getFeature("&aReal Fake Players&r")).getRFPS().size();
                return count+"";
        });
        platform.registerPlaceholders();
    }

    public String parsePlaceholders(String str, TabPlayer sender, TabPlayer viewer, TabPlayer def, TabFeature feature) {
        List<String> list = tab.getPlaceholderManager().detectPlaceholders(str);
        TabPlayer def2 = def == viewer ? sender : viewer;
        for (String pl : list) {
            if (pl.startsWith("%sender:") && sender != null) {
                String pl2 = pl.replace("%sender:", "%");
                str = str.replace(pl,getLastPlaceholderValue(pl2,sender,viewer,feature));
                continue;
            }
            else if (pl.startsWith("%viewer:") && viewer != null) {
                String pl2 = pl.replace("%viewer:", "%");
                str = str.replace(pl,getLastPlaceholderValue(pl2,viewer,sender,feature));
                continue;
            }
            str = str.replace(pl,getLastPlaceholderValue(pl,def,def2,feature));
        }
        return EnumChatFormat.color(str);
    }

    private String getLastPlaceholderValue(String str, TabPlayer p, TabPlayer viewer, TabFeature feature) {
        if (feature != null)
            tab.getPlaceholderManager().addUsedPlaceholder(str,feature);
        Placeholder pl = TAB.getInstance().getPlaceholderManager().getPlaceholder(str);
        if (pl instanceof RelationalPlaceholder) {
            if (p == null || viewer == null) return str;
            return ((RelationalPlaceholder) pl).getLastValue(p, viewer);
        }
        String value = pl.getLastValue(p);
        String newValue = tab.getPlaceholderManager().findReplacement(pl.getIdentifier(), value);
        if (newValue.contains("%value%"))
            newValue = newValue.replace("%value%", value);

        return newValue;
    }

    public String parsePlaceholders(String str, TabPlayer p) {
        return parsePlaceholders(str,p,null);
    }

    public String parsePlaceholders(String str, TabPlayer p, TabFeature feature) {
        if (str == null) return "";
        //if (p == null) return str;
        if (!str.contains("%")) return EnumChatFormat.color(str);
        for (String pl : tab.getPlaceholderManager().detectPlaceholders(str))
            str = str.replace(pl,getLastPlaceholderValue(pl,p,null,feature));
        return EnumChatFormat.color(str);
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

    public boolean isConditionMet(String str, TabPlayer sender, TabPlayer viewer, TabPlayer conditionPlayer, TabFeature feature) {
        if (sender == null || viewer == null) return false;
        String conditionname = TABAdditions.getInstance().parsePlaceholders(str,sender,viewer,conditionPlayer,feature);
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
        return sender.getWorld().equals(viewer.getWorld()) && ((Player) sender.getPlayer()).getLocation().distanceSquared(((Player) viewer.getPlayer()).getLocation()) < zone;
    }

    public void sendMessage(String name,String msg) {
        if (name.equals("~Console~"))
            tab.sendConsoleMessage(msg,true);
        else tab.getPlayer(name).sendMessage(msg,true);
    }
    public void sendMessage(String name, IChatBaseComponent msg) {
        if (name.equals("~Console~"))
            tab.sendConsoleMessage(msg.getText(),true);
        else tab.getPlayer(name).sendMessage(msg);
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

    public TabPlayer getPlayer(String name) {
        for (TabPlayer p : tab.getOnlinePlayers()) {
            if (p.getName().equalsIgnoreCase(name))
                return p;
        }
        return null;
    }
}