package io.github.tanguygab.tabadditions.shared;

import io.github.tanguygab.tabadditions.shared.features.layouts.LayoutManager;
import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;


public class SharedEvents {

    public static void JoinEvent(String name) {
    	TABAdditions.getInstance().getPlatform().AsyncTask(()->{
            TabPlayer p = TABAPI.getPlayer(name);

            if (TABAdditions.getInstance().layoutEnabled && TAB.getInstance().isPremium() && !TABAdditions.getInstance().checkBedrock(p)) {
                LayoutManager lm = LayoutManager.getInstance();
                lm.toAdd.put(p,lm.getLayout(p));
            }

        }, 50);

    }
}
