package io.github.tanguygab.tabadditions.shared.commands;

import io.github.tanguygab.tabadditions.shared.ConfigType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.Title;
import me.neznamy.tab.shared.TAB;

import java.util.*;

public class Cmd {

    public static void getMain(String name, String[] args) {
        TABAdditions instance = TABAdditions.getInstance();

        if (args.length < 1 || args[0].equalsIgnoreCase("help"))
            new HelpCmd(name, TABAdditions.getInstance().getPlatform().getVersion());
        else
            switch (args[0].toLowerCase()) {

                case "title": {
                    if (!TABAdditions.getInstance().titlesEnabled)
                        instance.sendMessage(name,"&cTitle feature is not enabled, therefore this command cannot be used");
                    else if (args.length < 2)
                        instance.sendMessage(name,"&cYou have to provide a title!");
                    else {
                        Map<String,String> titleSection = TABAdditions.getInstance().getConfig(ConfigType.TITLE).getConfigurationSection("titles."+args[1]);
                        if ((titleSection == null || titleSection.isEmpty()) && !args[1].startsWith("custom:")) {
                            instance.sendMessage(name,"&cThis title doesn't exist!");
                            return;
                        }
                        else {
                            List<Object> titleProperties = new ArrayList<>();
                            if (!args[1].startsWith("custom:")) {
                                for (String property : titleSection.keySet())
                                    titleProperties.add(titleSection.get(property));
                            }
                            new TitleCmd(name, args, titleProperties);
                        }
                    }
                    break;
                }
                case "tags": {
                    new TagsCmd(name, args);
                    break;
                }
                case "test": {
                    instance.sendMessage(name,"&7Nothing to see here :D");
                }
            }
    }

    public static List<String> getTabComplete(String[] args) {
        if (args.length == 1)
            return new ArrayList<>(Arrays.asList("help","title","tags","fp"));
        if (args.length >= 2) {
            switch (args[0]) {
                case "tags": {
                    if (args.length == 2)
                        return new ArrayList<>(Arrays.asList("hide","show","toggle"));
                    break;
                }
                case "title": {
                    Title title = TAB.getInstance().getFeatureManager().getFeature("Title");
                    if (args.length == 2 && title != null) {
                        List<String> list = new ArrayList<>(title.getLists());
                        list.add("custom:<title>||<subtitle>||<fadein>||<stay>||<fadeout>");
                        return list;
                    }
                    break;
                }
                case "width": {
                    if (args.length == 2)
                        return Arrays.asList("chars","set");
                    if (args.length == 3 && args[2].equalsIgnoreCase("chars"))
                        return Collections.singletonList("<character>");
                    if (args.length == 3 && args[2].equalsIgnoreCase("set"))
                        return Collections.singletonList("<char ID>");
                    if (args.length == 4 && args[2].equalsIgnoreCase("set"))
                        return Collections.singletonList("<amount>");
                }
            }
        }
        return null;
    }
}
