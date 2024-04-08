package io.github.tanguygab.tabadditions.shared.features.actionbar;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.commands.ActionBarCmd;
import lombok.Getter;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ActionBarManager extends TabFeature implements UnLoadable, CommandListener, JoinListener, Refreshable {

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

        Map<String,Map<String,String>> barsConfig = config.getConfigurationSection("actionbars.bars");
        barsConfig.forEach((bar,cfg)->{
            String text = cfg.get("text");
            if (text != null) addUsedPlaceholders(tab.getPlaceholderManager().detectPlaceholders(text));
            actionBars.put(bar,new ActionBarLine(text,cfg.containsKey("condition") ? Condition.getCondition(cfg.get("condition")) : null));
        });

        tab.getCPUManager().startRepeatingMeasuredTask(2000,featureName,"handling ActionBar",()->{
            for (TabPlayer p : tab.getOnlinePlayers())
                refresh(p,false);
        });
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
        plugin.getPlatform().sendActionbar(player, plugin.toFlatText(TabComponent.optimized(text)));
    }

    public ActionBarLine getActionBar(TabPlayer player) {
        for (ActionBarLine bar : actionBars.values())
            if (bar.isConditionMet(player))
                return bar;
        return null;
    }

    public void announceBar(TabPlayer player, String actionbar) {
        addUsedPlaceholders(TAB.getInstance().getPlaceholderManager().detectPlaceholders(actionbar));
        announcedBars.put(player,actionbar);
        refresh(player,true);
        TAB.getInstance().getCPUManager().runTaskLater(2000,featureName,"handling ActionBar on join for "+player.getName(),()->{
            if (actionbar.equals(announcedBars.get(player))) announcedBars.remove(player);
        });
    }

    @Override
    public void onJoin(TabPlayer player) {
        player.loadPropertyFromConfig(this,"join-actionbar");
        String prop = player.getProperty("join-actionbar").getCurrentRawValue();
        if (prop.isEmpty()) return;
        announceBar(player,prop);
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
