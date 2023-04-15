package io.github.tanguygab.tabadditions.shared.commands;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import java.util.*;

public class Cmd {

    public static void getMain(String name, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("tags")) {
            new TagsCmd(name,args);
            return;
        }
        new HelpCmd(name, TABAdditions.getInstance().getPlatform().getVersion());
    }

    public static List<String> getTabComplete(String[] args) {
        if (args.length == 1) return List.of("tags");
        if (args.length == 2 && args[0].equals("tags")) return Arrays.asList("hide", "show", "toggle");
        return null;
    }
}
