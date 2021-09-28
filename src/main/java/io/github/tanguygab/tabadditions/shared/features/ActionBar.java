package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.shared.ConfigType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.api.protocol.PacketPlayOutChat;

import java.util.ArrayList;
import java.util.List;

public class ActionBar extends TabFeature {

    public ActionBar() {
        super("&aActionBar&r");
    }

    public List<String> toggleActionBar = new ArrayList<>();

    @Override
    public void load() {
        boolean toggleEnabled = TABAdditions.getInstance().getConfig(ConfigType.TITLE).getBoolean("toggleactionbar",true);
        TABAdditions.getInstance().getPlatform().registerCommand("toggleactionbar",toggleEnabled);
        if (toggleEnabled) {
            toggleActionBar.addAll(TabAPI.getInstance().getPlayerCache().getStringList("toggleactionbar", new ArrayList<>()));
            TabAPI.getInstance().getPlayerCache().set("toggleactionbar",null);
        }
    }

    @Override
    public void unload() {
        if (TABAdditions.getInstance().getConfig(ConfigType.TITLE).getBoolean("toggleactionbar",true))
            TabAPI.getInstance().getPlayerCache().set("toggleactionbar", toggleActionBar);
    }

    @Override
    public void onJoin(TabPlayer p) {
        p.loadPropertyFromConfig(this,"actionbar");
        String prop = p.getProperty("actionbar").updateAndGet();
        if (prop.equals("")) return;
        String actionbar = TABAdditions.getInstance().getConfig(ConfigType.ACTIONBAR).getString("bars." + prop,"");
        sendActionBar(p,actionbar);
    }

    public List<String> getLists() {
        List<String> list = new ArrayList<>();
        for (Object key : TABAdditions.getInstance().getConfig(ConfigType.ACTIONBAR).getConfigurationSection("bars").keySet())
            list.add(key.toString());
        return list;
    }

    public void sendActionBar(TabPlayer p, String actionbar) {
        if (toggleActionBar.contains(p.getName().toLowerCase()) || actionbar.equals("")) return;

        actionbar = TABAdditions.getInstance().parsePlaceholders(actionbar,p);
        if (actionbar.startsWith("custom:"))
            actionbar = actionbar.replace("custom:","").replace("_"," ");

        p.sendCustomPacket(new PacketPlayOutChat(IChatBaseComponent.optimizedComponent(actionbar), PacketPlayOutChat.ChatMessageType.GAME_INFO),this);

    }

    public void toggleActionBar(String name) {
        TabPlayer p = TabAPI.getInstance().getPlayer(name);
        ConfigurationFile translation = TABAdditions.getInstance().getTranslation();

        if (toggleActionBar.contains(name.toLowerCase())) {
            toggleActionBar.remove(name.toLowerCase());
            p.sendMessage(translation.getString("tab+_actionbars_off", "&cYou won't receive any new actionbars!"), true);
        } else {
            toggleActionBar.add(name.toLowerCase());
            p.sendMessage(translation.getString("tab+_actionbars_on", "&aYou will now receive new actionbars!"), true);
        }
    }
}
