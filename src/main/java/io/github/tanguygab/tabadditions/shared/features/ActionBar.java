package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.shared.ConfigType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;

import java.util.ArrayList;
import java.util.List;

public class ActionBar implements JoinEventListener {

    private final TabFeature feature;

    public ActionBar(TabFeature feature) {
        feature.setDisplayName("&aActionBar");
        this.feature = feature;

    }

    @Override
    public void onJoin(TabPlayer p) {
        p.loadPropertyFromConfig("actionbar");
        String prop = p.getProperty("actionbar").updateAndGet();
        if (prop.equals("")) return;
        String actionbar = TABAdditions.getInstance().getConfig(ConfigType.ACTIONBAR).getString("bars." + prop,"");
        if (actionbar.equals("")) return;
        actionbar = TABAdditions.getInstance().parsePlaceholders(actionbar,p);
        p.sendCustomPacket(new PacketPlayOutChat(actionbar, PacketPlayOutChat.ChatMessageType.GAME_INFO));
    }

    public List<String> getLists() {
        List<String> list = new ArrayList<>();
        for (Object key : TABAdditions.getInstance().getConfig(ConfigType.ACTIONBAR).getConfigurationSection("bars").keySet())
            list.add(key.toString());
        return list;
    }

    @Override
    public TabFeature getFeatureType() {
        return feature;
    }
}
