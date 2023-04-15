package io.github.tanguygab.tabadditions.shared;

import java.io.File;
import java.io.IOException;
import java.util.*;

import io.github.tanguygab.tabadditions.shared.features.*;
import io.github.tanguygab.tabadditions.shared.features.actionbar.ActionBarManager;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatManager;
import io.github.tanguygab.tabadditions.shared.features.titles.TitleManager;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import me.neznamy.tab.api.event.plugin.TabLoadEvent;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.FeatureManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.config.ConfigurationFile;
import me.neznamy.tab.shared.config.YamlConfigurationFile;
import me.neznamy.tab.shared.event.impl.TabPlaceholderRegisterEvent;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.features.types.UnLoadable;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.RelationalPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.ServerPlaceholderImpl;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.bukkit.entity.Player;

import me.neznamy.tab.shared.placeholders.conditions.Condition;

public class TABAdditions {

    private static TABAdditions instance;
    private final Object plugin;
    private final Platform platform;
    public final File dataFolder;
    private final TAB tab;
    public final List<String> features = new ArrayList<>();
    public boolean enabled;

    private ConfigurationFile config;
    private ConfigurationFile chatConfig;
    private TranslationFile translation;

    public boolean chatEnabled;
    public boolean sithideEnabled = false;
    public boolean sneakhideEnabled = false;
    public int nametagInRange = 0;
    public int tablistNamesRadius = 0;
    public boolean onlyyou = false;
    public boolean condNametagsEnabled;
    private boolean condAppearenceEnabled;

    public TABAdditions(Platform platform, Object plugin,File dataFolder) {
        this.dataFolder = dataFolder;
    	this.platform = platform;
    	this.plugin = plugin;
    	tab = TAB.getInstance();
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

    public ConfigurationFile getConfig() {
        return config;
    }

    public TranslationFile getMsgs() {
        return translation;
    }

    public void load() {
        enabled = true;
        loadFiles();
        reload();
        tab.getEventBus().register(TabLoadEvent.class,e->platform.runTask(this::reload));
    }

    public void loadFiles() {
        try {
            if (platform.getType() == PlatformType.BUNGEE)
                config = new YamlConfigurationFile(TABAdditions.class.getClassLoader().getResourceAsStream("bungeeconfig.yml"), new File(dataFolder, "config.yml"));
            else
                config = new YamlConfigurationFile(TABAdditions.class.getClassLoader().getResourceAsStream("config.yml"), new File(dataFolder, "config.yml"));
            chatConfig = new YamlConfigurationFile(TABAdditions.class.getClassLoader().getResourceAsStream("chat.yml"), new File(dataFolder, "chat.yml"));
            translation = new TranslationFile(TABAdditions.class.getClassLoader().getResourceAsStream("translation.yml"), new File(dataFolder, "translation.yml"));

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
            if (fm.isFeatureEnabled(feature) && fm.getFeature(feature) instanceof UnLoadable)
                ((UnLoadable)fm.getFeature(feature)).unload();
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
        if (config.getBoolean("actionbars.enabled",false))
            registerFeature(new ActionBarManager());
        //Title
        if (config.getBoolean("titles.enabled",false))
            registerFeature(new TitleManager());
        //Chat
        if (chatEnabled)
            registerFeature(new ChatManager(chatConfig));
        //ConditionalNametags
        if (condNametagsEnabled)
            registerFeature(new ConditionalNametags());
        //ConditionalAppearance
        if (condAppearenceEnabled)
            registerFeature(new ConditionalAppearance());

        platform.loadFeatures();

    }

    private void loadPlaceholders() {
        PlaceholderManagerImpl pm = tab.getPlaceholderManager();
        platform.registerPlaceholders(pm);
        tab.getEventBus().register(TabPlaceholderRegisterEvent.class,this::onPlaceholderRegister);
    }

    private void onPlaceholderRegister(TabPlaceholderRegisterEvent e) {
        String identifier = e.getIdentifier();
        PlaceholderManagerImpl pm = tab.getPlaceholderManager();
        if (identifier.startsWith("%rel_viewer:")) {
            Placeholder placeholder = pm.getPlaceholder("%" + identifier.substring(12));
            Placeholder viewerPlaceholder = null;
            if (placeholder instanceof RelationalPlaceholderImpl) {
                RelationalPlaceholderImpl rel = (RelationalPlaceholderImpl) placeholder;
                viewerPlaceholder = pm.registerRelationalPlaceholder(identifier, placeholder.getRefresh(), (viewer, target) -> rel.getLastValue((TabPlayer) target, (TabPlayer) viewer));
            }
            if (placeholder instanceof PlayerPlaceholderImpl) {
                PlayerPlaceholderImpl player = (PlayerPlaceholderImpl) placeholder;
                viewerPlaceholder = pm.registerRelationalPlaceholder(identifier, placeholder.getRefresh(), (viewer, target) -> player.getLastValue((TabPlayer) viewer));
            }
            e.setPlaceholder(viewerPlaceholder);
        }
        if (identifier.startsWith("%rel_condition:")) {
            String cond = identifier.substring(15,identifier.length()-1);
            Condition condition = Condition.getCondition(cond);
            Placeholder placeholder = pm.registerRelationalPlaceholder(identifier, pm.getDefaultRefresh(), (viewer, target) -> viewer == null ? "" : parsePlaceholders(condition.getText((TabPlayer) viewer), (TabPlayer) target, (TabPlayer) viewer));
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
            if (placeholder instanceof PlayerPlaceholderImpl) output = ((PlayerPlaceholderImpl) placeholder).getLastValue(sender);
            if (placeholder instanceof ServerPlaceholderImpl) output = ((ServerPlaceholderImpl) placeholder).getLastValue();
            if (placeholder instanceof RelationalPlaceholderImpl) output = viewer == null ? "" : ((RelationalPlaceholderImpl) placeholder).getLastValue(viewer, sender);
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
                if (condition != null && !condition.isMet((me.neznamy.tab.shared.platform.TabPlayer) (checkForViewer ? viewer : sender)))
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

    public ConfigurationFile getPlayerData() {
        return tab.getConfiguration().getPlayerDataFile();
    }

    public String toFlatText(IChatBaseComponent component) {
        StringBuilder builder = new StringBuilder();
        if (component.getModifier().getColor() != null) builder.append("#").append(component.getModifier().getColor().getHexCode());
        builder.append(component.getModifier().getMagicCodes());
        if (component.getText() != null) builder.append(component.getText());
        component.getExtra().forEach(child->builder.append(toFlatText(child)));
        return builder.toString();
    }
}