package io.github.tanguygab.tabadditions.shared;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import io.github.tanguygab.tabadditions.shared.commands.NametagCmd;
import io.github.tanguygab.tabadditions.shared.features.*;
import io.github.tanguygab.tabadditions.shared.features.actionbar.ActionBarManager;
import io.github.tanguygab.tabadditions.shared.features.advancedconditions.AdvancedConditions;
import io.github.tanguygab.tabadditions.shared.features.chat.Chat;
import io.github.tanguygab.tabadditions.shared.features.titles.TitleManager;
import lombok.Getter;
import lombok.Setter;
import me.neznamy.chat.ChatModifier;
import me.neznamy.chat.EnumChatFormat;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.chat.component.TextComponent;
import me.neznamy.tab.api.event.plugin.TabLoadEvent;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.FeatureManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.PropertyConfiguration;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.file.YamlConfigurationFile;
import me.neznamy.tab.shared.event.impl.TabPlaceholderRegisterEvent;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.features.types.UnLoadable;
import me.neznamy.tab.shared.placeholders.types.PlayerPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.types.RelationalPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.types.ServerPlaceholderImpl;
import me.neznamy.tab.shared.platform.TabPlayer;

import me.neznamy.tab.shared.placeholders.conditions.Condition;

public class TABAdditions {

    @Getter @Setter private static TABAdditions instance;
    private final TAB tab = TAB.getInstance();
    @Getter private final Object plugin;
    @Getter private final Platform platform;
    private final File dataFolder;
    @Getter private ConfigurationFile config;
    @Getter private ConfigurationFile chatConfig;
    @Getter private TranslationFile translation;
    public final List<String> features = new ArrayList<>();


    public TABAdditions(Platform platform, Object plugin, File dataFolder) {
        this.dataFolder = dataFolder;
    	this.platform = platform;
    	this.plugin = plugin;
    }
    
    public static void addProperties() {
        PropertyConfiguration.VALID_PROPERTIES.addAll(List.of("chatprefix",
                "customchatname",
                "chatsuffix",
                "join-actionbar",
                "join-title",
                "appearance-condition",
                "nametag-condition"
        ));
    }

    public void load() {
        loadFiles();
        if (tab.getEventBus() != null) tab.getEventBus().register(TabPlaceholderRegisterEvent.class,this::onPlaceholderRegister);
        reload();
        if (tab.getEventBus() != null) tab.getEventBus().register(TabLoadEvent.class,e->platform.runTask(this::reload));
    }

    public void loadFiles() {
        try {
            config = new YamlConfigurationFile(TABAdditions.class.getClassLoader().getResourceAsStream("config.yml"), new File(dataFolder, "config.yml"));
            chatConfig = new YamlConfigurationFile(TABAdditions.class.getClassLoader().getResourceAsStream("chat.yml"), new File(dataFolder, "chat.yml"));

            File translationFile = new File(dataFolder, "translation.yml");
            if (!translationFile.exists()) translationFile.createNewFile();
            translation = new TranslationFile(null, translationFile);
        } catch (IOException e) {
            platform.disable();
            throw new RuntimeException(e);
        }
    }


    public void reload() {
        platform.reload();
        loadFiles();
        loadPlaceholders();
        loadFeatures();
    }

    public void disable() {
        FeatureManager fm = tab.getFeatureManager();

        features.forEach(feature->{
            if (fm.isFeatureEnabled(feature) && fm.getFeature(feature) instanceof UnLoadable)
                ((UnLoadable)fm.getFeature(feature)).unload();
            fm.unregisterFeature(feature);
        });
        features.clear();
    }

    public void registerFeature(TabFeature feature) {
        FeatureManager fm = tab.getFeatureManager();
        features.add(feature.getFeatureName());
        fm.registerFeature(feature.getFeatureName(),feature);
    }

    private void loadFeatures() {
        if (config.getBoolean("actionbars.enabled",false)) registerFeature(new ActionBarManager());
        if (config.getBoolean("titles.enabled",false)) registerFeature(new TitleManager());
        if (chatConfig.getBoolean("enabled",false)) registerFeature(new Chat(chatConfig));
        if (config.getBoolean("conditional-nametags.enabled",false) && tab.getNameTagManager() != null)
            registerFeature(new ConditionalNametags(
                    config.getBoolean("conditional-nametags.show-by-default",true),
                    config.getBoolean("conditional-nametags.relational",false)));
        if (!platform.isProxy() && config.getBoolean("conditional-appearance.enabled",false))
            registerFeature(new ConditionalAppearance(plugin,config.getBoolean("conditional-appearance.show-by-default",true)));
        if (tab.getNameTagManager() != null) tab.getCommand().registerSubCommand(new NametagCmd(tab.getNameTagManager()));
    }

    private void loadPlaceholders() {
        PlaceholderManagerImpl pm = tab.getPlaceholderManager();
        platform.registerPlaceholders(pm);

        AdvancedConditions.clearConditions();
        Map<String, Map<String, String>> conditions = config.getMap("advanced-conditions");
        conditions.forEach(AdvancedConditions::new);
        AdvancedConditions.finishSetups();
    }

    private void onPlaceholderRegister(TabPlaceholderRegisterEvent e) {
        String identifier = e.getIdentifier();
        PlaceholderManagerImpl pm = tab.getPlaceholderManager();
        if (identifier.startsWith("%rel_viewer:")) {
            Placeholder placeholder = pm.getPlaceholder("%" + identifier.substring(12));
            if (placeholder instanceof RelationalPlaceholderImpl rel) {
                e.setRelationalPlaceholder((viewer, target) -> rel.getLastValue((TabPlayer) target, (TabPlayer) viewer));
                return;
            }
            if (placeholder instanceof PlayerPlaceholderImpl player) {
                e.setRelationalPlaceholder((viewer, target) -> player.getLastValue((TabPlayer) viewer));
            }
            return;
        }
        if (identifier.startsWith("%rel_condition:")) {
            String cond = identifier.substring(15,identifier.length()-1);
            Condition condition = Condition.getCondition(cond);
            e.setRelationalPlaceholder((viewer, target) -> viewer == null ? "" : parsePlaceholders(condition.getText((TabPlayer) viewer), (TabPlayer) target, (TabPlayer) viewer));
        }
    }

    public String parsePlaceholders(String str, TabPlayer p) {
        if (str == null) return "";
        if (!str.contains("%")) return EnumChatFormat.color(str);
        str = parsePlaceholders(str,p,null);
        return str;
    }

    public String parsePlaceholders(String str, TabPlayer sender, TabPlayer viewer) {
        return parsePlaceholders(str,sender,viewer, PlaceholderManagerImpl.detectPlaceholders(str));
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
    public List<UUID> loadData(String data, boolean enabled) {
        return enabled ? getPlayerData().getStringList(data, new ArrayList<>()).stream().map(UUID::fromString).collect(Collectors.toCollection(ArrayList::new)) : List.of();
    }
    public void unloadData(String data, List<UUID> list, boolean enabled) {
        if (enabled) getPlayerData().set(data, list.stream().map(UUID::toString).collect(Collectors.toList()));
    }

    public boolean toggleCmd(boolean toggleCmd, TabPlayer player, List<UUID> toggled, String on, String off) {
        return toggleCmd(toggleCmd,player,toggled,null,on,off,false);
    }
    public boolean toggleCmd(boolean toggleCmd, TabPlayer player, List<UUID> toggled, PlayerPlaceholderImpl placeholder, String on, String off, boolean invert) {
        if (!toggleCmd) return false;
        boolean contains = !toggled.contains(player.getUniqueId());
        if (!contains) toggled.remove(player.getUniqueId());
        else toggled.add(player.getUniqueId());
        if (placeholder != null) placeholder.updateValue(player,contains ? "Off" : "On");
        player.sendMessage(contains != invert ? off : on);
        return true;
    }

    public String toFlatText(TabComponent component) {
        if (!(component instanceof TextComponent text)) return component.toLegacyText();
        ChatModifier modifier = text.getModifier();
        StringBuilder builder = new StringBuilder();
        if (modifier.getColor() != null) builder.append("#").append(modifier.getColor().getHexCode());
        builder.append(modifier.getMagicCodes());
        builder.append(text.getText());
        component.getExtra().forEach(extra -> builder.append(toFlatText(extra)));
        return builder.toString();
    }

}