package io.github.tanguygab.tabadditions.shared.commands;

import io.github.tanguygab.tabadditions.shared.Skins;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.TAFeature;
import io.github.tanguygab.tabadditions.shared.features.rfps.RFP;
import io.github.tanguygab.tabadditions.shared.features.rfps.RFPManager;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

public class RealFakePlayerCmd {
    public RealFakePlayerCmd(String nameS, String[] args) {
        TABAdditions instance = TABAdditions.getInstance();
        RFPManager rfpm = (RFPManager) TAB.getInstance().getFeatureManager().getFeature(TAFeature.RFP.toString());
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
            if (name.equalsIgnoreCase("_ALL_"))
                output = "&cYou can't use that name";
            else output = rfpm.addRFP(name);
        }

        else if (args[1].equalsIgnoreCase("remove")) {
            if (name.equals("_ALL_"))
                output = rfpm.deleteAll();
            else output = rfpm.deleteRFP(name);
        }

        else if (args[1].equalsIgnoreCase("info"))
            output = rfpm.getRFP(name).getInfo();

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
                output = rfpm.getRFP(name).setName(value);

            if (prop.equalsIgnoreCase("latency"))
                output = rfpm.getRFP(name).setLatency(value);

            if (prop.equalsIgnoreCase("skin"))
                output = rfpm.getRFP(name).setSkin(value);

            if (prop.equalsIgnoreCase("group"))
                output = rfpm.getRFP(name).setGroup(value);

            if (prop.equalsIgnoreCase("prefix"))
                output = rfpm.getRFP(name).setPrefix(value);

            if (prop.equalsIgnoreCase("suffix"))
                output = rfpm.getRFP(name).setSuffix(value);

            for (TabPlayer p : TAB.getInstance().getPlayers()) {
                Object skin = null;
                if (prop.equalsIgnoreCase("skin"))
                    skin = Skins.getInstance().getIcon(value,p);
                rfpm.getRFP(name).forceUpdate(p,skin);
            }
        }

        instance.sendMessage(nameS,output);
    }
}
