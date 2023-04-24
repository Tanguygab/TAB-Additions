package io.github.tanguygab.tabadditions.shared.features.titles;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.commands.TitleCmd;
import lombok.Getter;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.platform.TabPlayer;

import java.util.*;

public class TitleManager extends TabFeature implements UnLoadable, Refreshable, CommandListener, JoinListener {

    @Getter private final String featureName = "Title";
    @Getter private final String refreshDisplayName = "&aTitle&r";

    private final TABAdditions plugin;
    private final Map<String, Title> titles = new HashMap<>();
    private final Map<TabPlayer, String> announcedTitles = new HashMap<>();
    private final List<UUID> toggled;
    private final boolean toggleCmd;
    public TitleManager() {
        plugin = TABAdditions.getInstance();
        TAB tab = TAB.getInstance();
        tab.getCommand().registerSubCommand(new TitleCmd(this));

        ConfigurationFile config = plugin.getConfig();
        toggleCmd = config.getBoolean("titles./toggletitle",true);
        toggled = plugin.loadData("title-off",toggleCmd);

        Map<String, Map<String,String>> titlesConfig = config.getConfigurationSection("titles.titles");
        titlesConfig.forEach((name,cfg)->{
            String title = cfg.get("title");
            String subtitle = cfg.get("subtitle");
            if (title != null) addUsedPlaceholders(tab.getPlaceholderManager().detectPlaceholders(title));
            if (subtitle != null) addUsedPlaceholders(tab.getPlaceholderManager().detectPlaceholders(subtitle));
            titles.put(title,new Title(title,subtitle));
        });

        tab.getCPUManager().startRepeatingMeasuredTask(2000,this,"handling Title",()->{
            for (TabPlayer p : tab.getOnlinePlayers())
                refresh(p,false);
        });
    }
    @Override
    public void unload() {
        plugin.unloadData("title-off",toggled,toggleCmd);
    }

    @Override
    public void refresh(TabPlayer player, boolean force) {
        if (!announcedTitles.containsKey(player) || toggled.contains(player.getUniqueId())) return;
        sendTitle(player, true);
    }

    private void sendTitle(TabPlayer player, boolean refresh) {
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
        plugin.getPlatform().sendTitle(player, parse(player,title),parse(player,subtitle),refresh ? 0 : 20, 60, 20);
    }

    private String parse(TabPlayer player, String text) {
        return IChatBaseComponent.optimizedComponent(plugin.parsePlaceholders(text,player)).toFlatText();
    }
    public void announceTitle(TabPlayer player, String title) {
        if (toggled.contains(player.getUniqueId())) return;
        addUsedPlaceholders(TAB.getInstance().getPlaceholderManager().detectPlaceholders(title));
        announcedTitles.put(player,title);
        sendTitle(player,false);
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
        player.sendMessage(toggled.contains(player.getUniqueId()) ? plugin.getTranslation().titleOff : plugin.getTranslation().titleOn,true);
        refresh(player,true);
        return true;
    }
}
