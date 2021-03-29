package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.types.event.SneakEventListener;

import java.util.HashMap;
import java.util.Map;

public class SneakHideNametag implements SneakEventListener {

    private final Map<TabPlayer, Boolean> tag = new HashMap<>();

    @Override
    public void onSneak(TabPlayer p, boolean isSneaking) {
        if (TABAdditions.getInstance().sneakhideEnabled) {
            if (isSneaking) {
                tag.put(p, p.hasHiddenNametag());
                p.hideNametag();
            } else if (!tag.get(p))
                p.showNametag();
        }
    }

    @Override
    public Object getFeatureType() {
        return TAFeature.SNEAK_HIDE_NAMETAG;
    }
}
