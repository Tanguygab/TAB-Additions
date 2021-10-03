package io.github.tanguygab.tabadditions.shared.commands;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.ActionBar;
import io.github.tanguygab.tabadditions.shared.features.TAFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

public class ActionBarCmd {

    public ActionBarCmd(String name, String[] args, String actionbar) {

        TAB tab = TAB.getInstance();
        TABAdditions instance = TABAdditions.getInstance();
        ActionBar feature = (ActionBar) tab.getFeatureManager().getFeature(TAFeature.ACTIONBAR.toString());


        if (args.length > 2 && args[2].equals("*")) {
            for (TabPlayer p : tab.getPlayers()) {
                feature.addToNoBar(p);
                feature.sendActionBar(p, actionbar);
            }
            return;
        }

        TabPlayer p = null;
        if (args.length > 2)
            p = instance.getPlayer(args[2]);
        else if (!name.equals("~Console~"))
            p = instance.getPlayer(name);

        if (p == null) {
            instance.sendMessage(name,"&cThis player isn't connected");
            return;
        }
        feature.addToNoBar(p);
        feature.sendActionBar(p,actionbar.replace("_"," "));

    }
}
