package io.github.tanguygab.tabadditions.shared.features.actionbar;

import io.github.tanguygab.tabadditions.shared.ConfigType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.commands.ActionBarCmd;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.config.ConfigurationFile;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;

import java.util.*;
import java.util.stream.Collectors;

public class ActionBarManager extends TabFeature implements UnLoadable, CommandListener, JoinListener, Refreshable {

    private final TABAdditions plugin;
    private final Map<String, ActionBarLine> bars = new LinkedHashMap<>();
    private final Map<TabPlayer, String> announcedBars = new HashMap<>();
    private final List<UUID> toggled = new ArrayList<>();
    private final boolean toggleCmd;

    public ActionBarManager() {
        plugin = TABAdditions.getInstance();
        TAB tab = TAB.getInstance();
        tab.getCommand().registerSubCommand(new ActionBarCmd(this));

        ConfigurationFile config = plugin.getConfig(ConfigType.MAIN);
        toggleCmd = config.getBoolean("actionbars./toggleactionbar",true);
        if (toggleCmd) toggled.addAll(plugin.getPlayerData().getStringList("actionbar-off", new ArrayList<>()).stream().map(UUID::fromString).collect(Collectors.toList()));

        Map<String,Map<String,String>> barsConfig = config.getConfigurationSection("actionbars.bars");
        barsConfig.forEach((bar,cfg)->{
            String text = cfg.get("text");
            if (text != null) addUsedPlaceholders(tab.getPlaceholderManager().detectPlaceholders(text));
            bars.put(bar,new ActionBarLine(bar,text,cfg.containsKey("condition") ? Condition.getCondition(cfg.get("condition")) : null));
        });

        tab.getCPUManager().startRepeatingMeasuredTask(2000,this,"handling ActionBar",()->{
            for (TabPlayer p : tab.getOnlinePlayers())
                refresh(p,false);
        });
    }

    @Override
    public void unload() {
        if (toggleCmd) plugin.getPlayerData().set("toggleactionbar", toggled);
    }

    @Override
    public String getFeatureName() {
        return "ActionBar";
    }

    @Override
    public String getRefreshDisplayName() {
        return "&aActionBar&r";
    }

    @Override
    public void refresh(TabPlayer player, boolean force) {
        String text = announcedBars.containsKey(player) ? announcedBars.get(player) : getActionBar(player).getText();
        if (toggled.contains(player.getUniqueId()) || text.equals("")) return;

        text = plugin.parsePlaceholders(text,player);
        plugin.getPlatform().sendActionbar(player, plugin.toFlatText(IChatBaseComponent.optimizedComponent(text)));
    }

    public ActionBarLine getActionBar(TabPlayer player) {
        for (ActionBarLine bar : bars.values())
            if (bar.isConditionMet(player))
                return bar;
        return null;
    }

    public void announceBar(TabPlayer player, String actionbar) {
        announcedBars.put(player,actionbar);
        refresh(player,true);
        TAB.getInstance().getCPUManager().runTaskLater(2000,this,"handling ActionBar on join for "+player.getName(),()->{
            if (actionbar.equals(announcedBars.get(player))) announcedBars.remove(player);
        });
    }

    @Override
    public void onJoin(TabPlayer player) {
        player.loadPropertyFromConfig(this,"join-actionbar");
        String prop = player.getProperty("join-actionbar").getCurrentRawValue();
        if (prop.equals("")) return;
        announceBar(player,prop);
    }

    @Override
    public boolean onCommand(TabPlayer player, String msg) {
        if (!toggleCmd || !msg.equals("/toggleactionbar")) return false;
        toggled.add(player.getUniqueId());
        refresh(player,true);
        return true;
    }
}
