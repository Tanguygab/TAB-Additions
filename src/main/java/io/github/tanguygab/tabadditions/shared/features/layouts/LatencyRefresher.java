package io.github.tanguygab.tabadditions.shared.features.layouts;

import me.neznamy.tab.api.Property;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;

public class LatencyRefresher extends TabFeature {

    public LatencyRefresher() {
        super("&aTAB+ Layout&r");
        load();
    }

    @Override
    public void load() {
        super.load();
    }

    @Override
    public void onJoin(TabPlayer p) {
        p.setProperty(this,"latency","%ping%");
    }

    @Override
    public void refresh(TabPlayer p, boolean force) {
        String pr = p.getProperty("latency").updateAndGet();
        int i = Integer.parseInt(pr);

    }
}
