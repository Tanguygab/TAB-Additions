package io.github.tanguygab.tabadditions.shared.commands;

import io.github.tanguygab.tabadditions.shared.ConfigType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.ActionBar;
import io.github.tanguygab.tabadditions.shared.features.TAFeature;
import io.github.tanguygab.tabadditions.shared.features.Title;
import io.github.tanguygab.tabadditions.shared.features.rfps.RFP;
import io.github.tanguygab.tabadditions.shared.features.rfps.RFPManager;
import me.neznamy.tab.shared.TAB;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Cmd {

    public static void getMain(String name, String[] args) {
        TABAdditions instance = TABAdditions.getInstance();

        if (args.length < 1 || args[0].equalsIgnoreCase("help"))
            new HelpCmd(name, TABAdditions.getInstance().getPlatform().getVersion());
        else
            switch (args[0].toLowerCase()) {

                case "actionbar": {
                    if (!TABAdditions.getInstance().getConfig(ConfigType.MAIN).getBoolean("features.actionbars"))
                        instance.sendMessage(name,"&cActionbar feature is not enabled, therefore this command cannot be used");
                    else if (args.length < 2)
                        instance.sendMessage(name,"&cYou have to provide an actionbar!");
                    else {
                        Map<String,String> section = TABAdditions.getInstance().getConfig(ConfigType.ACTIONBAR).getConfigurationSection("bars");
                        if (!section.containsKey(args[1]))
                            instance.sendMessage(name,"&cThis actionbar doesn't exist!");
                        else
                            new ActionBarCmd(name, args, section.get(args[1]));
                    }
                    break;
                }
                case "title": {
                    if (!TABAdditions.getInstance().getConfig(ConfigType.MAIN).getBoolean("features.titles"))
                        instance.sendMessage(name,"&cTitle feature is not enabled, therefore this command cannot be used");
                    else if (args.length < 2)
                        instance.sendMessage(name,"&cYou have to provide a title!");
                    else {
                        Map<String,String> titleSection = TABAdditions.getInstance().getConfig(ConfigType.TITLE).getConfigurationSection("titles."+args[1]);
                        if (titleSection.keySet().isEmpty()) {
                            instance.sendMessage(name,"&cThis title doesn't exist!");
                        }
                        else {
                            List<Object> titleProperties = new ArrayList<>();
                            for (Object property : titleSection.keySet())
                                titleProperties.add(titleSection.get(property));
                            new TitleCmd(name, args, titleProperties);
                        }
                    }
                    break;
                }
                case "fp": {
                    if (TABAdditions.getInstance().rfpEnabled) {
                        if (args.length < 2)
                            instance.sendMessage(name,"You have to provide add, remove, edit, info or list.");
                        else if (!args[1].equalsIgnoreCase("list") && args.length < 3)
                            instance.sendMessage(name,"You have to provide a fake player name.");
                        else if (args[1].equalsIgnoreCase("edit") && args.length < 4)
                            instance.sendMessage(name,"You have to provide an action.");
                        else new RealFakePlayerCmd(name, args);
                    }
                    break;
                }
                case "tags": {
                    new TagsCmd(name, args);
                    break;
                }
                case "width": {
                    new WidthCmd(name, args);
                }
                case "test": {
                    instance.sendMessage(name,"&7Nothing to see here :D");
                }
            }
    }

    public static List<String> getTabComplete(String[] args) {
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
