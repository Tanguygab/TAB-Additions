package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.spigot.BukkitItemLine;
import me.neznamy.tab.api.ArmorStand;
import me.neznamy.tab.api.ArmorStandManager;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;
import me.neznamy.tab.shared.features.types.event.WorldChangeListener;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class UnlimitedItemLines implements Loadable,WorldChangeListener, JoinEventListener {

    public UnlimitedItemLines() {
        load();
    }

    @Override
    public void load() {
        for (TabPlayer p : TAB.getInstance().getPlayers())
            load(p);
    }

    public void load(TabPlayer p) {
        ArmorStandManager asm = p.getArmorStandManager();
        try {
            Field armorstands = ArmorStandManager.class.getDeclaredField("armorStands");
            armorstands.setAccessible(true);
            Map<String, ArmorStand> map = new HashMap<>((Map<String, ArmorStand>) armorstands.get(asm));
            for (String str : map.keySet()) {
                ArmorStand as = map.get(str);
                if (as.getProperty().get().startsWith("ITEM:")) {
                    BukkitItemLine itemLine = createItemLine(p, as);
                    if (itemLine != null) {
                        asm.addArmorStand(str, itemLine);
                    }
                }
            }
        } catch (Exception e) {e.printStackTrace();}
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
        prop.changeRawValue(prop.getOriginalRawValue().replace("ITEM:",""));

        boolean glow = false;
        if (prop.getFormat(p).contains(",glow")) {
            glow = true;
            prop.changeRawValue(prop.getOriginalRawValue().replace(",glow",""));
        }

        return new BukkitItemLine(as.getEntityId(),p,prop,as.getOffset(),as.hasStaticOffset(),glow);
    }

    @Override
    public Object getFeatureType() {
        return TAFeature.UNLIMITED_ITEM_LINES;
    }
}
