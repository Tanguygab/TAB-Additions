package io.github.tanguygab.tabadditions.shared.commands;

import io.github.tanguygab.tabadditions.shared.features.ActionBar;
import io.github.tanguygab.tabadditions.shared.features.TAFeature;
import io.github.tanguygab.tabadditions.shared.features.Title;
import io.github.tanguygab.tabadditions.shared.features.rfps.RFP;
import io.github.tanguygab.tabadditions.shared.features.rfps.RFPManager;
import me.neznamy.tab.shared.TAB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TabComplete {

    public static List<String> get(String[] args) {
        if (args.length == 1)
            return new ArrayList<>(Arrays.asList("help","actionbar","title","tags","fp"));
        if (args.length >= 2) {
            switch (args[0]) {
                case "actionbar":
                    ActionBar actionbar = (ActionBar) TAB.getInstance().getFeatureManager().getFeature(TAFeature.ACTIONBAR.toString());
                    if (args.length == 2 && actionbar != null)
                        return actionbar.getLists();
                    break;
                case "tags": {
                    if (args.length == 2)
                        return new ArrayList<>(Arrays.asList("hide","show","toggle"));
                    break;
                }
                case "fp": {
                    RFPManager rfpm = (RFPManager) TAB.getInstance().getFeatureManager().getFeature(TAFeature.RFP.toString());
                    if (rfpm == null)
                        return null;
                    if (args.length == 2)
                        return new ArrayList<>(Arrays.asList("add","remove","edit","list","info"));
                    if (!args[1].equalsIgnoreCase("list") && args.length == 3) {
                        List<RFP> rfps = rfpm.getRFPS();
                        List<String> rfpnames = new ArrayList<>();
                        for (RFP rfp : rfps)
                            rfpnames.add(rfp.getConfigName());
                        if (args[1].equalsIgnoreCase("remove"))
                            rfpnames.add("_ALL_");
                        return rfpnames;
                    }
                    if (args[1].equalsIgnoreCase("edit") && args.length == 4)
                        return new ArrayList<>(Arrays.asList("name","skin","latency","group","prefix","suffix"));
                    break;
                }
                case "title": {
                    Title title = (Title) TAB.getInstance().getFeatureManager().getFeature(TAFeature.TITLE.toString());
                    if (args.length == 2 && title != null)
                        return title.getLists();
                    break;
                }
            }
        }
        return null;
    }
}
