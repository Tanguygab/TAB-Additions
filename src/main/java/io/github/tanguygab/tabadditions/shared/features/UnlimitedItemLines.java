package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.spigot.BukkitItemLine;
import me.neznamy.tab.api.ArmorStand;
import me.neznamy.tab.api.ArmorStandManager;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;
import me.neznamy.tab.shared.features.types.event.WorldChangeListener;
import org.bukkit.Material;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class UnlimitedItemLines implements Loadable,WorldChangeListener, JoinEventListener {

    private final TabFeature feature;

    public UnlimitedItemLines(TabFeature feature) {
        feature.setDisplayName("&aUnlimited Item Lines");
        this.feature = feature;
        load();
    }

    @Override
    public void load() {
        for (TabPlayer p : TAB.getInstance().getPlayers())
            load(p);
    }

    public void load(TabPlayer p) {
        ArmorStandManager asm = p.getArmorStandManager();
        Field armorstands;
        Map<String,ArmorStand> map = new HashMap<>();
        try {
            armorstands = ArmorStandManager.class.getDeclaredField("armorStands");
            armorstands.setAccessible(true);
            map = (Map<String,ArmorStand>) armorstands.get(asm);
        } catch (Exception e) {e.printStackTrace();}
        for (String str : map.keySet()) {
            ArmorStand as = map.get(str);
            if (as.getProperty().get().startsWith("ITEM:")) {
                BukkitItemLine itemLine = createItemLine(p,as);
                if (itemLine != null)
                    map.replace(str,itemLine);
            }
        }
    }

    @Override
    public void unload() {

    }

    @Override
    public void onJoin(TabPlayer p) {
        load(p);
    }

    @Override
    public void onWorldChange(TabPlayer p, String from, String to) {
        load(p);
    }

    public BukkitItemLine createItemLine(TabPlayer p, ArmorStand as) {
        Property prop = as.getProperty();
        prop.changeRawValue(prop.getOriginalRawValue().replace("ITEM:","").toUpperCase().replace(" ","_"));
        if (Material.getMaterial(prop.get()) == null) return null;

        return new BukkitItemLine(as.getEntityId(),p,prop,as.getOffset(),as.hasStaticOffset());
    }

    @Override
    public TabFeature getFeatureType() {
        return feature;
    }
}
