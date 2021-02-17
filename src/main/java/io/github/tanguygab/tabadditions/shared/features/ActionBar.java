package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.shared.ConfigType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;

public class ActionBar implements JoinEventListener {

    private final TabFeature feature;

    public ActionBar(TabFeature feature) {
        feature.setDisplayName("ActionBar");
        this.feature = feature;

    }

    @Override
    public void onJoin(TabPlayer p) {
        p.loadPropertyFromConfig("actionbar");
        String actionbar = TABAdditions.getInstance().getConfig(ConfigType.ACTIONBAR).getString("bars." + p.getProperty("actionbar").get(),"");
        if (actionbar.equals("")) return;
        actionbar = TABAdditions.getInstance().parsePlaceholders(actionbar,p);
        p.sendCustomPacket(new PacketPlayOutChat(actionbar, PacketPlayOutChat.ChatMessageType.GAME_INFO));
    }

    @Override
    public TabFeature getFeatureType() {
        return feature;
    }
}
