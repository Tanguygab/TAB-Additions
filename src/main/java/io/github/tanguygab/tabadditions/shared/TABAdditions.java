package io.github.tanguygab.tabadditions.shared;

import java.io.File;
import java.io.IOException;
import java.util.*;

import io.github.tanguygab.tabadditions.shared.commands.NametagCmd;
import io.github.tanguygab.tabadditions.shared.features.*;
import io.github.tanguygab.tabadditions.shared.features.actionbar.ActionBarManager;
import io.github.tanguygab.tabadditions.shared.features.titles.TitleManager;
import me.neznamy.tab.api.event.plugin.TabLoadEvent;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.FeatureManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.file.YamlConfigurationFile;
import me.neznamy.tab.shared.event.impl.TabPlaceholderRegisterEvent;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.features.types.UnLoadable;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.RelationalPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.ServerPlaceholderImpl;
import me.neznamy.tab.shared.platform.TabPlayer;

import me.neznamy.tab.shared.placeholders.conditions.Condition;

public class TABAdditions {

    private static TABAdditions instance;
    private final Object plugin;
    private final Platform platform;
    public final File dataFolder;
    private final TAB tab;
    public final List<String> features = new ArrayList<>();

    private ConfigurationFile config;
    private TranslationFile translation;

    public TABAdditions(Platform platform, Object plugin, File dataFolder) {
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
        loadFiles();
        reload();
        tab.getEventBus().register(TabLoadEvent.class,e->platform.runTask(this::reload));
    }

    public void loadFiles() {
        try {
            config = new YamlConfigurationFile(TABAdditions.class.getClassLoader().getResourceAsStream("config.yml"), new File(dataFolder, "config.yml"));
            translation = new TranslationFile(TABAdditions.class.getClassLoader().getResourceAsStream("translation.yml"), new File(dataFolder, "translation.yml"));
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
    }

    public void registerFeature(TabFeature feature) {
        FeatureManager fm = tab.getFeatureManager();
        features.add(feature.getFeatureName());
        fm.registerFeature(feature.getFeatureName(),feature);
    }

    private void loadFeatures() {
        if (config.getBoolean("actionbars.enabled",false)) registerFeature(new ActionBarManager());
        if (config.getBoolean("titles.enabled",false)) registerFeature(new TitleManager());
        if (config.getBoolean("conditional-nametags.enabled",false) && tab.getTeamManager() != null)
            registerFeature(new ConditionalNametags(config.getBoolean("appearance-nametags.show-by-default",true)));
        if (config.getBoolean("conditional-appearance.enabled",false))
            registerFeature(new ConditionalAppearance(plugin,config.getBoolean("conditional-appearance.show-by-default",true)));
        if (tab.getTeamManager() != null) tab.getCommand().registerSubCommand(new NametagCmd(tab.getTeamManager()));

        if (platform.isProxy()) return;
        int nametagInRange = config.getInt("nametag-in-range", 0);
        if (nametagInRange != 0 && tab.getTeamManager() != null) registerFeature(new NametagInRange(nametagInRange));
        int tablistInRange = config.getInt("tabname-in-range", 0);
        if (tablistInRange != 0) registerFeature(new TabnameInRange(plugin,tablistInRange));
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
        return parsePlaceholders(str,sender,viewer,tab.getPlaceholderManager().detectPlaceholders(str));
    }
    public String parsePlaceholders(String str, TabPlayer sender, TabPlayer viewer, List<String> placeholders) {
        for (String pl : placeholders) {
            Placeholder placeholder = tab.getPlaceholderManager().getPlaceholder(pl);
            String output = pl;
            if (placeholder instanceof PlayerPlaceholderImpl) output = ((PlayerPlaceholderImpl) placeholder).getLastValue(sender);
            if (placeholder instanceof ServerPlaceholderImpl) output = ((ServerPlaceholderImpl) placeholder).getLastValue();
            if (placeholder instanceof RelationalPlaceholderImpl) output = viewer == null ? "" : ((RelationalPlaceholderImpl) placeholder).getLastValue(viewer, sender);
            str = str.replace(pl, output);
        }
        return EnumChatFormat.color(str);
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