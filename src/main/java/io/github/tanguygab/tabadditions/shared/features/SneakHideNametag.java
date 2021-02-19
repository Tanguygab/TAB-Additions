package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.types.Feature;
import me.neznamy.tab.shared.features.types.event.SneakEventListener;

import java.util.HashMap;
import java.util.Map;

public class SneakHideNametag implements SneakEventListener {

    private final TabFeature feature;

    private final Map<TabPlayer, Boolean> tag = new HashMap<>();

    public SneakHideNametag(TabFeature feature) {
        feature.setDisplayName("&aSneak Hide Nametag");
        this.feature = feature;
    }

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
    public TabFeature getFeatureType() {
        return feature;
    }
}
