package io.github.tanguygab.tabadditions.shared.commands;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.rfps.RFP;
import io.github.tanguygab.tabadditions.shared.features.rfps.RFPManager;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;

public class RealFakePlayerCmd {
    public RealFakePlayerCmd(String nameS, String[] args) {
        TABAdditions instance = TABAdditions.getInstance();
        RFPManager rfpm = (RFPManager) TabAPI.getInstance().getFeatureManager().getFeature("Real Fake Players");
        String output = "";

        if (args[1].equalsIgnoreCase("list")) {
            instance.sendMessage(nameS,"Fakeplayers list:");
            output = "&a";
            for (RFP rfp : rfpm.getRFPS()) {
                if (!output.equalsIgnoreCase("&a")) output = output+", ";
                output = output+rfp.getConfigName();
            }
            instance.sendMessage(nameS,output);
            return;
        }

        String name = args[2];

        if (args[1].equalsIgnoreCase("add")) {
            if (name.equalsIgnoreCase("_ALL_") || name.equalsIgnoreCase("random"))
                output = "&cYou can't use that name";
            else output = rfpm.addRFP(name);
        }

        else if (args[1].equalsIgnoreCase("remove")) {
            if (name.equals("_ALL_"))
                output = rfpm.deleteAll();
            else if (name.equalsIgnoreCase("random"))
                output = "&cYou can't use that name";
            else output = rfpm.deleteRFP(name);
        }
        else if (rfpm.getRFP(name) == null) {
            output = "This RFP doesn't exist.";
        } else {
            RFP rfp = rfpm.getRFP(name);
            if (args[1].equalsIgnoreCase("info"))
                output = rfp.getInfo(TabAPI.getInstance().getPlayer(nameS));

            else if (args[1].equalsIgnoreCase("edit")) {
                String prop = args[3];
                String value = null;
                if (args.length > 4) {
                    value = "";
                    for (int i = 4; i < args.length; i++) {
                        if (i != 4) value = value + " ";
                        value = value + args[i];
                    }
                }
                if (prop.equalsIgnoreCase("name"))
                    output = rfp.setName(value);

                if (prop.equalsIgnoreCase("latency"))
                    output = rfp.setLatency(value);

                if (prop.equalsIgnoreCase("skin"))
                    output = rfp.setSkin(value);

                if (prop.equalsIgnoreCase("group"))
                    output = rfp.setGroup(value);

                if (prop.equalsIgnoreCase("prefix"))
                    output = rfp.setPrefix(value);

                if (prop.equalsIgnoreCase("suffix"))
                    output = rfp.setSuffix(value);

                for (TabPlayer p : TabAPI.getInstance().getOnlinePlayers()) {
                    Object skin = null;
                    if (prop.equalsIgnoreCase("skin"))
                        skin = instance.getSkins().getSkin(value);
                    rfp.update(p, skin);
                }
            }
        }

        instance.sendMessage(nameS,output);
    }
}
