package io.github.tanguygab.tabadditions.shared.features.commands;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.rfps.RFP;
import io.github.tanguygab.tabadditions.shared.features.rfps.RFPManager;

public class RealFakePlayerCmd {
    public RealFakePlayerCmd(String nameS, String[] args) {
        TABAdditions instance = TABAdditions.getInstance();
        String output = "";

        if (args[1].equalsIgnoreCase("list")) {
            instance.sendMessage(nameS,"Fakeplayers list:");
            output = "&a";
            for (RFP rfp : RFPManager.getInstance().getRFPS()) {
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
            else output = RFPManager.getInstance().addRFP(name);
        }

        else if (args[1].equalsIgnoreCase("remove")) {
            if (name.equals("_ALL_"))
                output = RFPManager.getInstance().deleteAll();
            else output = RFPManager.getInstance().deleteRFP(name);
        }

        else if (args[1].equalsIgnoreCase("info"))
            output = RFPManager.getInstance().getRFP(name).getInfo();

        else if (args[1].equalsIgnoreCase("edit")) {
            String prop = args[3];
            String value = null;
            if (args.length > 4)
                value = args[4];
            if (prop.equalsIgnoreCase("name"))
                output = RFPManager.getInstance().getRFP(name).setName(value);

            if (prop.equalsIgnoreCase("latency"))
                output = RFPManager.getInstance().getRFP(name).setLatency(value);

            if (prop.equalsIgnoreCase("skin"))
                output = RFPManager.getInstance().getRFP(name).setSkin(value);

            if (prop.equalsIgnoreCase("group"))
                output = RFPManager.getInstance().getRFP(name).setGroup(value);

            if (prop.equalsIgnoreCase("prefix"))
                output = RFPManager.getInstance().getRFP(name).setPrefix(value);

            if (prop.equalsIgnoreCase("suffix"))
                output = RFPManager.getInstance().getRFP(name).setSuffix(value);

            RFPManager.getInstance().getRFP(name).forceUpdate(null);
        }

        instance.sendMessage(nameS,output);
    }
}
