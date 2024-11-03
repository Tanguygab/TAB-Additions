package io.github.tanguygab.tabadditions.shared.features.titles;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.commands.TitleCmd;
import lombok.Getter;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import me.neznamy.tab.shared.cpu.CpuManager;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TitleManager extends RefreshableFeature implements UnLoadable, CommandListener, JoinListener {

    private static final StringToComponentCache cache = new StringToComponentCache("Title", 100);

    @Getter private final String featureName = "Title";
    @Getter private final String refreshDisplayName = "&aTitle&r";
    @Getter private final String command = "/toggletitle";

    private final TABAdditions plugin;
    @Getter private final Map<String, Title> titles = new HashMap<>();
    private final Map<TabPlayer, String> announcedTitles = new HashMap<>();
    private final List<UUID> toggled;
    private final boolean toggleCmd;
    public TitleManager() {
        plugin = TABAdditions.getInstance();
        TAB tab = TAB.getInstance();
        tab.getCommand().registerSubCommand(new TitleCmd(this));

        ConfigurationFile config = plugin.getConfig();
        toggleCmd = config.getBoolean("titles./toggletitle",true);
        if (toggleCmd) plugin.getPlatform().registerCommand("toggletitle");
        toggled = plugin.loadData("title-off",toggleCmd);

        ConfigurationSection titlesConfig = config.getConfigurationSection("titles.titles");
        titlesConfig.getKeys().forEach(key -> {
            String name = key.toString();
            ConfigurationSection section = titlesConfig.getConfigurationSection(name);

            String title = section.getString("title");
            String subtitle = section.getString("subtitle");
            if (title != null) addUsedPlaceholders(PlaceholderManagerImpl.detectPlaceholders(title));
            if (subtitle != null) addUsedPlaceholders(PlaceholderManagerImpl.detectPlaceholders(subtitle));
            titles.put(name,new Title(title,subtitle));
        });

        tab.getCPUManager().getProcessingThread().repeatTask(new TimedCaughtTask(tab.getCPUManager(), ()->{
            for (TabPlayer p : tab.getOnlinePlayers())
                refresh(p,false);
        }, featureName, "handling Title"), 2000);
    }
    @Override
    public void unload() {
        plugin.unloadData("title-off",toggled,toggleCmd);
    }

    @Override
    public void refresh(@NotNull TabPlayer player, boolean force) {
        if (!announcedTitles.containsKey(player) || toggled.contains(player.getUniqueId())) return;
        sendTitle(player, true);
    }

    private void sendTitle(TabPlayer player, boolean refresh) {
        String name = announcedTitles.get(player);
        String title,subtitle;
        if (titles.containsKey(name)) {
            title = titles.get(name).title();
            subtitle = titles.get(name).subtitle();
        } else {
            String[] str = name.split("\\n");
            title = str[0];
            subtitle = str.length > 1 ? str[1] : "";
        }
        plugin.getPlatform().sendTitle(player, parse(player,title),parse(player,subtitle),refresh ? 0 : 20, 60, 20);
    }

    private String parse(TabPlayer player, String text) {
        return plugin.toFlatText(cache.get(plugin.parsePlaceholders(text,player)));
    }
    public void announceTitle(TabPlayer player, String title) {
        if (toggled.contains(player.getUniqueId())) return;
        addUsedPlaceholders(PlaceholderManagerImpl.detectPlaceholders(title));
        announcedTitles.put(player,title);
        sendTitle(player,false);

        CpuManager cpu = TAB.getInstance().getCPUManager();
        cpu.getProcessingThread().executeLater(new TimedCaughtTask(cpu, () -> {
            if (title.equals(announcedTitles.get(player))) announcedTitles.remove(player);
        }, featureName, "handling Title on join for "+player.getName()),2000);

    }

    @Override
    public void onJoin(TabPlayer player) {
        String property = player.loadPropertyFromConfig(this,"join-title", "").getCurrentRawValue();
        if (property.isEmpty()) return;
        announceTitle(player,property);
    }

    @Override
    public boolean onCommand(@NotNull TabPlayer player, String msg) {
        if (msg.equals(command) && plugin.toggleCmd(toggleCmd,player,toggled,plugin.getTranslation().titleOn,plugin.getTranslation().titleOff)) {
            refresh(player,true);
            return true;
        }
        return false;
    }
}
