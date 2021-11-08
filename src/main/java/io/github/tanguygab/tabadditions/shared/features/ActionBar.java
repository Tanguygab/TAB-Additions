package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.shared.ConfigType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.Property;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.api.protocol.PacketPlayOutChat;
import me.neznamy.tab.api.task.RepeatingTask;

import java.util.ArrayList;
import java.util.List;

public class ActionBar extends TabFeature {

    public ActionBar() {
        super("&aActionBar&r");
        load();
    }

    public List<String> toggleActionBar = new ArrayList<>();
    public List<TabPlayer> noBar = new ArrayList<>();
    public RepeatingTask task;

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

        task = tab.getThreadManager().startRepeatingMeasuredTask(2000,"handling permanent ActionBars",this,"refreshing",()->{
            for (TabPlayer p : tab.getOnlinePlayers()) {
                if (noBar.contains(p)) continue;
                Property prop = p.getProperty("actionbar");
                sendActionBar(p,prop.updateAndGet());
            }
        });
    }

    @Override
    public void unload() {
        task.cancel();
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



        p.sendCustomPacket(new PacketPlayOutChat(IChatBaseComponent.optimizedComponent(actionbar), PacketPlayOutChat.ChatMessageType.GAME_INFO),this);

    }

    public void addToNoBar(TabPlayer p) {
        noBar.add(p);
        TabAPI.getInstance().getThreadManager().runTaskLater(2000,"handling ActionBar on join for "+p.getName(),this,"",()-> noBar.remove(p));
    }

    public void toggleActionBar(String name) {
        TabPlayer p = TabAPI.getInstance().getPlayer(name);
        ConfigurationFile translation = TABAdditions.getInstance().getTranslation();

        if (toggleActionBar.contains(name.toLowerCase())) {
            toggleActionBar.remove(name.toLowerCase());
            p.sendMessage(translation.getString("tab+_actionbars_on", "&aYou will now receive new actionbars!"), true);
        } else {
            toggleActionBar.add(name.toLowerCase());
            p.sendMessage(translation.getString("tab+_actionbars_off", "&cYou won't receive any new actionbars!"), true);
        }
    }
}
