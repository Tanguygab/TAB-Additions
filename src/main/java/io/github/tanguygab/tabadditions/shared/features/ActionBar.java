package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.shared.ConfigType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;

import java.util.ArrayList;
import java.util.List;

public class ActionBar implements JoinEventListener, Loadable {

    public List<TabPlayer> noBar = new ArrayList<>();

    @Override
    public void load() {
        TAB tab = TAB.getInstance();

        for (TabPlayer p : tab.getPlayers())
            p.loadPropertyFromConfig("actionbar");

        tab.getCPUManager().startRepeatingMeasuredTask(2000,"handling permanent ActionBars",this,UsageType.REFRESHING,()->{
            for (TabPlayer p : tab.getPlayers()) {
                if (noBar.contains(p)) continue;
                Property prop = p.getProperty("actionbar");
                if (prop != null)
                    sendActionBar(p,prop.updateAndGet());
            }
        });
    }

    @Override
    public void unload() {}

    @Override
    public void onJoin(TabPlayer p) {
        p.loadPropertyFromConfig("actionbar");
        p.loadPropertyFromConfig("join-actionbar");
        String prop = p.getProperty("join-actionbar").updateAndGet();
        if (prop.equals("")) return;
        String actionbar = TABAdditions.getInstance().getConfig(ConfigType.ACTIONBAR).getString("bars." + prop,"");
        addToNoBar(p);
        sendActionBar(p,actionbar);
    }

    public List<String> getLists() {
        List<String> list = new ArrayList<>();
        for (Object key : TABAdditions.getInstance().getConfig(ConfigType.ACTIONBAR).getConfigurationSection("bars").keySet())
            list.add(key.toString());
        return list;
    }

    public void sendActionBar(TabPlayer p, String actionbar) {
        if (actionbar.equals("")) return;

        actionbar = TABAdditions.getInstance().parsePlaceholders(actionbar,p);
        if (actionbar.startsWith("custom:"))
            actionbar = actionbar.replace("custom:","").replace("_"," ");

        p.sendCustomPacket(new PacketPlayOutChat(IChatBaseComponent.fromColoredText(actionbar), PacketPlayOutChat.ChatMessageType.GAME_INFO));

    }

    public void addToNoBar(TabPlayer p) {
        noBar.add(p);
        TAB.getInstance().getCPUManager().runTaskLater(2000,"handling ActionBar on join for "+p.getName(),this, UsageType.REFRESHING,()-> noBar.remove(p));
    }

    @Override
    public Object getFeatureType() {
        return TAFeature.ACTIONBAR;
    }
}
