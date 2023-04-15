package io.github.tanguygab.tabadditions.shared.features.titles;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.commands.TitleCmd;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.config.ConfigurationFile;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.platform.TabPlayer;

import java.util.*;
import java.util.stream.Collectors;

public class TitleManager extends TabFeature implements UnLoadable, Refreshable, CommandListener, JoinListener {

    private final TABAdditions plugin;
    private final Map<String, Title> titles = new HashMap<>();
    private final Map<TabPlayer, String> announcedTitles = new HashMap<>();
    private final List<UUID> toggled = new ArrayList<>();
    private final boolean toggleCmd;
    public TitleManager() {
        plugin = TABAdditions.getInstance();
        TAB tab = TAB.getInstance();
        tab.getCommand().registerSubCommand(new TitleCmd(this));

        ConfigurationFile config = plugin.getConfig();
        toggleCmd = config.getBoolean("titles./toggletitle",true);
        if (toggleCmd) {
            toggled.addAll(plugin.getPlayerData().getStringList("title-off", new ArrayList<>()).stream().map(UUID::fromString).collect(Collectors.toList()));
            plugin.getPlatform().registerCommand("toggletitle",true);
        }

        Map<String, Map<String,String>> titlesConfig = config.getConfigurationSection("titles.titles");
        titlesConfig.forEach((name,cfg)->{
            String title = cfg.get("title");
            String subtitle = cfg.get("subtitle");
            if (title != null) addUsedPlaceholders(tab.getPlaceholderManager().detectPlaceholders(title));
            if (subtitle != null) addUsedPlaceholders(tab.getPlaceholderManager().detectPlaceholders(subtitle));
            titles.put(title,new Title(name,title,subtitle));
        });

        tab.getCPUManager().startRepeatingMeasuredTask(2000,this,"handling Title",()->{
            for (TabPlayer p : tab.getOnlinePlayers())
                refresh(p,false);
        });
    }
    @Override
    public void unload() {
        if (toggleCmd) plugin.getPlayerData().set("title-off", toggled.stream().map(UUID::toString).collect(Collectors.toList()));
    }

    @Override
    public String getFeatureName() {
        return "Title";
    }

    @Override
    public String getRefreshDisplayName() {
        return "&aTitle&r";
    }

    @Override
    public void refresh(TabPlayer player, boolean force) {
        if (!announcedTitles.containsKey(player) || toggled.contains(player.getUniqueId())) return;
        String name = announcedTitles.get(player);
        String title,subtitle;
        if (titles.containsKey(name)) {
            title = titles.get(name).getTitle();
            subtitle = titles.get(name).getSubtitle();
        } else {
            String[] str = name.split("\n");
            title = str[0];
            subtitle = str.length > 1 ? str[1] : "";
        }
        plugin.getPlatform().sendTitle(player, parse(player,title),parse(player,subtitle),20, 60, 20);
    }

    private String parse(TabPlayer player, String text) {
        return plugin.toFlatText(IChatBaseComponent.optimizedComponent(plugin.parsePlaceholders(text,player)));
    }
    public void announceTitle(TabPlayer player, String title) {
        addUsedPlaceholders(TAB.getInstance().getPlaceholderManager().detectPlaceholders(title));
        announcedTitles.put(player,title);
        refresh(player,true);
        TAB.getInstance().getCPUManager().runTaskLater(2000,this,"handling Title on join for "+player.getName(),()->{
            if (title.equals(announcedTitles.get(player))) announcedTitles.remove(player);
        });
    }

    @Override
    public void onJoin(TabPlayer player) {
        player.loadPropertyFromConfig(this,"join-title");
        String prop = player.getProperty("join-title").getCurrentRawValue();
        if (prop.equals("")) return;
        announceTitle(player,prop);
    }

    @Override
    public boolean onCommand(TabPlayer player, String msg) {
        if (!toggleCmd || !msg.equals("/toggletitle")) return false;
        if (toggled.contains(player.getUniqueId()))
            toggled.remove(player.getUniqueId());
        else toggled.add(player.getUniqueId());
        player.sendMessage(toggled.contains(player.getUniqueId()) ? plugin.getMsgs().titleOff : plugin.getMsgs().titleOn,true);
        refresh(player,true);
        return true;
    }
}
