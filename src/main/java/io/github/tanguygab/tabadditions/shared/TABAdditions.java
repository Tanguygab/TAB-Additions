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
import me.neznamy.tab.api.event.plugin.TabLoadEvent;
import me.neznamy.tab.api.placeholder.*;
import me.neznamy.tab.shared.event.impl.TabPlaceholderRegisterEvent;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.placeholders.TabPlaceholder;
import org.bukkit.entity.Player;

import me.neznamy.tab.shared.features.layout.skin.SkinManager;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

public class TABAdditions {

    private static TABAdditions instance;
    private final Object plugin;
    private final Platform platform;
    public final File dataFolder;
    private final TabAPI tab;
    public final List<String> features = new ArrayList<>();
    public boolean enabled;

    private ConfigurationFile config;
    private ConfigurationFile titleConfig;
    private ConfigurationFile actionbarConfig;
    private ConfigurationFile chatConfig;
    private TranslationFile translation;
    private SkinManager skins;

    public boolean titlesEnabled;
    public boolean actionbarsEnabled;
    public boolean chatEnabled;
    public boolean sithideEnabled = false;
    public boolean sneakhideEnabled = false;
    public int nametagInRange = 0;
    public int tablistNamesRadius = 0;
    public boolean rfpEnabled;
    public boolean onlyyou = false;
    public boolean condNametagsEnabled;
    private boolean condAppearenceEnabled;

    public TABAdditions(Platform platform, Object plugin,File dataFolder) {
        this.dataFolder = dataFolder;
    	this.platform = platform;
    	this.plugin = plugin;
    	tab = TabAPI.getInstance();
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

    public SkinManager getSkins() {
        return skins;
    }

    public ConfigurationFile getConfig(ConfigType cfg) {
        switch (cfg) {
            case TITLE: return titleConfig;
            case ACTIONBAR: return actionbarConfig;
            case CHAT: return chatConfig;
            default: return config;
        }
    }

    public TranslationFile getMsgs() {
        return translation;
    }

    public void load() {
        enabled = true;
        loadFiles();
        reload();
        TabAPI.getInstance().getEventBus().register(TabLoadEvent.class,e->reload());
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
            translation = new TranslationFile(TABAdditions.class.getClassLoader().getResourceAsStream("translation.yml"), new File(dataFolder, "translation.yml"));
            skins = new SkinManager("texture:f3d5e43de5d4177c4baf2f44161554473a3b0be5430998b5fcd826af943afe3");

            titlesEnabled = config.getBoolean("features.titles",false);
            actionbarsEnabled = config.getBoolean("features.actionbars",false);
            rfpEnabled = config.getBoolean("features.real-fake-players",false);
            chatEnabled = config.getBoolean("features.chat",false);
            condNametagsEnabled = config.getBoolean("features.conditional-nametags",false);
            if (platform.getType() == PlatformType.SPIGOT) {
                sithideEnabled = config.getBoolean("features.sit-hide-nametags", false);
                sneakhideEnabled = config.getBoolean("features.sneak-hide-nametags", false);
                nametagInRange = config.getInt("features.nametag-in-range", 0);
                tablistNamesRadius = config.getInt("features.tablist-names-radius", 0);
                onlyyou = config.getBoolean("features.only-you",false);
                condAppearenceEnabled = config.getBoolean("features.conditional-appearance",false);
            }
        } catch (IOException e) {
            platform.disable();
            e.printStackTrace();
        }
    }

    public void reload() {
        tab.getEventBus().unregister(TabPlaceholderRegisterEvent.class);
        platform.reload();
        loadFiles();
        loadFeatures();
        loadPlaceholders();
    }

    public void disable() {
        FeatureManager fm = tab.getFeatureManager();

        features.forEach(feature->{
            if (fm.isFeatureEnabled(feature))
                fm.getFeature(feature).unload();
            fm.unregisterFeature(feature);
        });

        enabled = false;
    }

    public void registerFeature(TabFeature feature) {
        FeatureManager fm = tab.getFeatureManager();
        features.add(feature.getFeatureName());
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
        //ConditionalNametags
        if (condNametagsEnabled)
            registerFeature(new ConditionalNametags());
        //ConditionalAppearance
        if (condAppearenceEnabled)
            registerFeature(new ConditionalAppearance());

        platform.loadFeatures();

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
                if (tab.getFeatureManager().isFeatureEnabled("Real Fake Players"))
                    count = count+((RFPManager)tab.getFeatureManager().getFeature("Real Fake Players")).getRFPS().size();
                return count+"";
        });
        pm.registerPlayerPlaceholder("%canseeonlinerfp%",1000,p->{
                int count = tab.getOnlinePlayers().length;
                try {count = Integer.parseInt(parsePlaceholders("%canseeonline%",p));}
                catch (NumberFormatException ignored) {}
                if (tab.getFeatureManager().isFeatureEnabled("Real Fake Players"))
                    count = count + ((RFPManager) tab.getFeatureManager().getFeature("Real Fake Players")).getRFPS().size();
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
                if (tab.getFeatureManager().isFeatureEnabled("Real Fake Players"))
                    count = count + ((RFPManager) tab.getFeatureManager().getFeature("Real Fake Players")).getRFPS().size();
                return count+"";
        });
        platform.registerPlaceholders(pm);
        tab.getEventBus().register(TabPlaceholderRegisterEvent.class,this::onPlaceholderRegister);
    }

    private void onPlaceholderRegister(TabPlaceholderRegisterEvent e) {
        String identifier = e.getIdentifier();
        PlaceholderManager pm = tab.getPlaceholderManager();
        if (identifier.startsWith("%rel_viewer:")) {
            Placeholder placeholder = pm.getPlaceholder("%" + identifier.substring(12));
            Placeholder viewerPlaceholder = null;
            if (placeholder instanceof RelationalPlaceholder) {
                RelationalPlaceholder rel = (RelationalPlaceholder) placeholder;
                viewerPlaceholder = pm.registerRelationalPlaceholder(identifier, placeholder.getRefresh(), (viewer, target) -> rel.getLastValue(target, viewer));
            }
            if (placeholder instanceof PlayerPlaceholder) {
                PlayerPlaceholder player = (PlayerPlaceholder) placeholder;
                viewerPlaceholder = pm.registerRelationalPlaceholder(identifier, placeholder.getRefresh(), (viewer, target) -> player.getLastValue(viewer));
            }
            e.setPlaceholder(viewerPlaceholder);
        }
        if (identifier.startsWith("%rel_condition:")) {
            String cond = identifier.substring(15,identifier.length()-1);
            Condition condition = Condition.getCondition(cond);
            TabPlaceholder placeholder = (TabPlaceholder) pm.registerRelationalPlaceholder(identifier, ((PlaceholderManagerImpl)pm).getDefaultRefresh(), (viewer, target) -> parsePlaceholders(condition.getText(viewer),target,viewer));
            e.setPlaceholder(placeholder);
        }
    }

    public String parsePlaceholders(String str, TabPlayer p) {
        if (str == null) return "";
        if (!str.contains("%")) return EnumChatFormat.color(str);
        str = parsePlaceholders(str,p,null);
        return EnumChatFormat.color(str);
    }

    public String parsePlaceholders(String str, TabPlayer sender, TabPlayer viewer) {
        List<String> list = tab.getPlaceholderManager().detectPlaceholders(str);
        for (String pl : list) {
            Placeholder placeholder = tab.getPlaceholderManager().getPlaceholder(pl);
            String output = pl;
            if (placeholder instanceof PlayerPlaceholder) output = ((PlayerPlaceholder) placeholder).getLastValue(sender);
            if (placeholder instanceof ServerPlaceholder) output = ((ServerPlaceholder) placeholder).getLastValue();
            if (placeholder instanceof RelationalPlaceholder) output = ((RelationalPlaceholder) placeholder).getLastValue(viewer,sender);
            str = str.replace(pl, output);
        }
        return EnumChatFormat.color(str);
    }



    //so, I have no clue what I did here, but don't worry, I'll change it, someday...
    public boolean isConditionMet(String str, TabPlayer sender, TabPlayer viewer, boolean checkForViewer) {
        if (sender == null || viewer == null) return false;
        String conditionname = TABAdditions.getInstance().parsePlaceholders(str,sender,viewer);
        for (String cond : conditionname.split(";")) {
            if (cond.startsWith("!inRange:") || cond.startsWith("inRange:")) {
                try {
                    int range = Integer.parseInt(cond.replace("!", "").replace("inRange:", ""));
                    boolean result = isInRange(sender, viewer, range);
                    if (cond.startsWith("!") && result) return false;
                    if (!cond.startsWith("!") && !result) return false;
                } catch (NumberFormatException ignored) {}
            } else {
                Condition condition = Condition.getCondition(cond);
                if (condition != null && !condition.isMet(checkForViewer ? viewer : sender))
                    return false;
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