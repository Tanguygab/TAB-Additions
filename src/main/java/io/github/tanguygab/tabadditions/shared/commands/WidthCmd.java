package io.github.tanguygab.tabadditions.shared.commands;

import io.github.tanguygab.tabadditions.shared.PlatformType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.packets.IChatBaseComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WidthCmd {

    public WidthCmd(String name, String[] args) {
        TABAdditions instance = TABAdditions.getInstance();
        if (args.length < 1) {
            instance.sendMessage(name, "/"+ (TABAdditions.getInstance().getPlatform().getType() == PlatformType.BUNGEE ? "b" : "") + "tab+ width chars <char>");
            instance.sendMessage(name, "/"+ (TABAdditions.getInstance().getPlatform().getType() == PlatformType.BUNGEE ? "b" : "") + "tab+ width set <charID> [amount]");
            return;
        }
        if (args[1].equalsIgnoreCase("chars")) {
            instance.sendMessage(name,"Chars:" + args[2]);
            String arg = args[2];
            int[] ints = new int[arg.length()];
            for (int i = 0; i < arg.length(); i++) {
                if (arg.charAt(i) == '\u00a7') ints[i] = '&';
                else ints[i] = arg.charAt(i);
            }
            instance.sendMessage(name,Arrays.toString(arg.toCharArray()));
            instance.sendMessage(name,Arrays.toString(ints));
        } else if (args[1].equalsIgnoreCase("set")) {
            int id = Integer.parseInt(args[2]);
            List<IChatBaseComponent> messages = new ArrayList<>();
            IChatBaseComponent charMessage = new IChatBaseComponent("\u00a72" + (char)id + " \u00a7d|");
            messages.add(new IChatBaseComponent("\u00a7b[TAB] Click the line with closest width"));
            int max = args.length > 3 ? Integer.parseInt(args[3]) : 10;
            for (int i = 1; i < max; i++) {
                messages.add(getText(i, id));
                if (i % 2 != 0) messages.add(charMessage);
            }
            for (IChatBaseComponent message : messages) {
                instance.sendMessage(name,message);
            }
        }
        else if (args[1].equalsIgnoreCase("put")) {
            try {
                int c = Integer.parseInt(args[2]);
                try {
                    int i = Integer.parseInt(args[3]);
                    TAB.getInstance().getConfiguration().getPremiumConfig().set("character-width-overrides." + c, i);
                } catch (Exception e) {
                    instance.sendMessage(name, "You have to provide a width!");
                }
            } catch (Exception e) {
                instance.sendMessage(name, "You have to provide a character ID!");
            }
        }
    }

    private IChatBaseComponent getText(int width, int c) {
        String text = "";
        int pixelsRemaining = width + 1;
        while (pixelsRemaining % 2 != 0) {
            pixelsRemaining -= 3;
            text += "l";
        }
        while (pixelsRemaining > 0) {
            pixelsRemaining -= 2;
            text += "i";
        }
        return new IChatBaseComponent(("&b&k" + text + " &e|&b (" + width + " pixels) &7&l[Click to copy]").replace('&', '\u00a7'))
                .onClickRunCommand(TABAdditions.getInstance().getPlatform().getType() == PlatformType.BUNGEE ? "b" : "" + "tab+ width confirm "+c+" "+width)
                .onHoverShowText("Click to copy with " + width + " pixels");
    }
}
