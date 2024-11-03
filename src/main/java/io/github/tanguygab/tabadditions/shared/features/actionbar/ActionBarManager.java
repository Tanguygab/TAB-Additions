package io.github.tanguygab.tabadditions.shared.features.actionbar;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.commands.ActionBarCmd;
import lombok.Getter;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import me.neznamy.tab.shared.cpu.CpuManager;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ActionBarManager extends RefreshableFeature implements UnLoadable, CommandListener, JoinListener {

    private static final StringToComponentCache cache = new StringToComponentCache("ActionBar", 100);

    @Getter private final String featureName = "ActionBar";
    @Getter private final String refreshDisplayName = "&aActionBar&r";
    @Getter private final String command = "/toggleactionbar";

    private final TABAdditions plugin;
    @Getter private final Map<String, ActionBarLine> actionBars = new LinkedHashMap<>();
    private final Map<TabPlayer, String> announcedBars = new HashMap<>();
    private final List<UUID> toggled;
    private final boolean toggleCmd;

    public ActionBarManager() {
        plugin = TABAdditions.getInstance();
        TAB tab = TAB.getInstance();
        tab.getCommand().registerSubCommand(new ActionBarCmd(this));

        ConfigurationFile config = plugin.getConfig();
        toggleCmd = config.getBoolean("actionbars./toggleactionbar",true);
        if (toggleCmd) plugin.getPlatform().registerCommand("toggleactionbar");
        toggled = plugin.loadData("actionbar-off",toggleCmd);

        ConfigurationSection barsConfig = config.getConfigurationSection("actionbars.bars");
        barsConfig.getKeys().forEach(key -> {
            String bar = key.toString();
            ConfigurationSection section = barsConfig.getConfigurationSection(bar);

            String text = section.getString("text");
            String condition = section.getString("condition");

            if (text != null) addUsedPlaceholders(PlaceholderManagerImpl.detectPlaceholders(text));
            actionBars.put(bar,new ActionBarLine(text,condition == null ? null : Condition.getCondition(condition)));
        });

        tab.getCPUManager().getProcessingThread().repeatTask(new TimedCaughtTask(tab.getCPUManager(), ()->{
            for (TabPlayer p : tab.getOnlinePlayers())
                refresh(p,false);
        },featureName,"handling ActionBar"),2000);
    }

    @Override
    public void unload() {
        plugin.unloadData("actionbar-off",toggled,toggleCmd);
    }

    @Override
    public void refresh(@NotNull TabPlayer player, boolean force) {
        String text;
        if (announcedBars.containsKey(player)) {
            text = announcedBars.get(player);
            if (actionBars.containsKey(text))
                text = actionBars.get(text).getText();
        } else {
            ActionBarLine bar = getActionBar(player);
            if (bar == null) return;
            text = bar.getText();
        }
        if (toggled.contains(player.getUniqueId())) return;

        text = plugin.parsePlaceholders(text,player);
        plugin.getPlatform().sendActionbar(player, plugin.toFlatText(cache.get(text)));
    }

    public ActionBarLine getActionBar(TabPlayer player) {
        for (ActionBarLine bar : actionBars.values())
            if (bar.isConditionMet(player))
                return bar;
        return null;
    }

    public void announceBar(TabPlayer player, String actionbar) {
        addUsedPlaceholders(PlaceholderManagerImpl.detectPlaceholders(actionbar));
        announcedBars.put(player,actionbar);
        refresh(player,true);

        CpuManager cpu = TAB.getInstance().getCPUManager();
        cpu.getProcessingThread().executeLater(new TimedCaughtTask(cpu, () -> {
            if (actionbar.equals(announcedBars.get(player))) announcedBars.remove(player);
        }, featureName, "handling ActionBar on join for "+player.getName()),2000);
    }

    @Override
    public void onJoin(TabPlayer player) {
        String property = player.loadPropertyFromConfig(this,"join-actionbar", "").getCurrentRawValue();
        if (property.isEmpty()) return;
        announceBar(player,property);
    }

    @Override
    public boolean onCommand(@NotNull TabPlayer player, String msg) {
        if (msg.equals(command) && plugin.toggleCmd(toggleCmd,player,toggled,plugin.getTranslation().actionBarOn,plugin.getTranslation().actionBarOff)) {
            refresh(player,true);
            return true;
        }
        return false;
    }
}
