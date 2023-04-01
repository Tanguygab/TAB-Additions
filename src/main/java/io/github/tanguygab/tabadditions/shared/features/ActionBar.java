package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.shared.ConfigType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.TranslationFile;
import me.neznamy.tab.api.Property;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.feature.*;
import me.neznamy.tab.shared.TAB;

import java.util.ArrayList;
import java.util.List;

public class ActionBar extends TabFeature implements Refreshable, Loadable, UnLoadable, CommandListener, JoinListener {

    public List<String> toggleActionBar = new ArrayList<>();
    public List<TabPlayer> noBar = new ArrayList<>();


    public ActionBar() {
        load();
    }
    @Override
    public String getFeatureName() {
        return "ActionBar";
    }

    @Override
    public void refresh(TabPlayer tabPlayer, boolean b) {

    }

    @Override
    public String getRefreshDisplayName() {
        return "&aActionBar&r";
    }


    @Override
    public void load() {
        TabAPI tab = TabAPI.getInstance();
        boolean toggleEnabled = TABAdditions.getInstance().getConfig(ConfigType.TITLE).getBoolean("toggleactionbar",true);
        TABAdditions.getInstance().getPlatform().registerCommand("toggleactionbar",toggleEnabled);
        if (toggleEnabled) {
            toggleActionBar.addAll(tab.getPlayerCache().getStringList("toggleactionbar", new ArrayList<>()));
            tab.getPlayerCache().set("toggleactionbar",null);
        }

        for (TabPlayer p : tab.getOnlinePlayers())
            p.loadPropertyFromConfig(this,"actionbar");

        TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(1000,this,"handling permanent ActionBars",()->{
            for (TabPlayer p : tab.getOnlinePlayers()) {
                if (noBar.contains(p)) continue;
                Property prop = p.getProperty("actionbar");
                if (prop != null)
                    sendActionBar(p,prop.updateAndGet());
            }
        });
    }

    @Override
    public void unload() {
        if (TABAdditions.getInstance().getConfig(ConfigType.TITLE).getBoolean("toggleactionbar",true))
            TabAPI.getInstance().getPlayerCache().set("toggleactionbar", toggleActionBar);
    }

    @Override
    public void onJoin(TabPlayer p) {
        p.loadPropertyFromConfig(this,"join-actionbar");
        p.loadPropertyFromConfig(this,"actionbar");
        String prop = p.getProperty("join-actionbar").updateAndGet();
        if (prop.equals("")) return;
        addToNoBar(p);
        sendActionBar(p,prop);
    }

    public List<String> getLists() {
        List<String> list = new ArrayList<>();
        for (Object key : TABAdditions.getInstance().getConfig(ConfigType.ACTIONBAR).getConfigurationSection("bars").keySet())
            list.add(key.toString());
        return list;
    }

    public void sendActionBar(TabPlayer p, String actionbar) {
        if (toggleActionBar.contains(p.getName().toLowerCase()) || actionbar.equals("")) return;
        if (actionbar.startsWith("custom:"))
            actionbar = TABAdditions.getInstance().parsePlaceholders(actionbar.replace("custom:",""),p).replace("_"," ");
        else actionbar = TABAdditions.getInstance().parsePlaceholders(TABAdditions.getInstance().getConfig(ConfigType.ACTIONBAR).getString("bars." + actionbar,""),p);

        TABAdditions.getInstance().getPlatform().sendActionbar(p,IChatBaseComponent.optimizedComponent(actionbar).toFlatText());
    }

    public void addToNoBar(TabPlayer p) {
        noBar.add(p);
        TAB.getInstance().getCPUManager().runTaskLater(2000,this,"handling ActionBar "+p.getName(),()-> noBar.remove(p));
    }

    public void toggleActionBar(String name) {
        TabPlayer p = TabAPI.getInstance().getPlayer(name);
        TranslationFile translation = TABAdditions.getInstance().getMsgs();

        if (toggleActionBar.contains(name.toLowerCase())) {
            toggleActionBar.remove(name.toLowerCase());
            p.sendMessage(translation.actionBarOn, true);
        } else {
            toggleActionBar.add(name.toLowerCase());
            p.sendMessage(translation.actionBarOff, true);
        }
    }

    @Override
    public boolean onCommand(TabPlayer sender, String msg) {
        if (!msg.equals("/toggleactionbar")) return false;
        toggleActionBar(sender.getName());
        return true;
    }
}
