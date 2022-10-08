package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.shared.ConfigType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.TranslationFile;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Title extends TabFeature {

    public Title() {
        super("Title","&aTitle&r");
        load();
    }

    public List<String> toggleTitle = new ArrayList<>();

    @Override
    public void load() {
        boolean toggleEnabled = TABAdditions.getInstance().getConfig(ConfigType.TITLE).getBoolean("toggletitle",true);
        TABAdditions.getInstance().getPlatform().registerCommand("toggletitle",toggleEnabled);
        if (toggleEnabled) {
            toggleTitle.addAll(TabAPI.getInstance().getPlayerCache().getStringList("toggletitle", new ArrayList<>()));
            TabAPI.getInstance().getPlayerCache().set("toggletitle",null);
        }
    }

    @Override
    public void unload() {
        if (TABAdditions.getInstance().getConfig(ConfigType.TITLE).getBoolean("toggletitle",true))
            TabAPI.getInstance().getPlayerCache().set("toggletitle", toggleTitle);
    }

    public void sendTitle(List<Object> properties, String[] args, TabPlayer p) {
        if (toggleTitle.contains(p.getName().toLowerCase())) return;

        String title;
        String subtitle;
        int fadeIn;
        int stay;
        int fadeOut;
        if (args.length < 1 || !args[1].startsWith("custom:")) {
            title = properties.get(1)+"";
            subtitle = properties.get(0)+"";
            fadeIn = (int) properties.get(2);
            stay = (int) properties.get(3);
            fadeOut = (int) properties.get(4);
        } else {
            String[] t = args[1].replace("custom:","").split("\\|\\|");
            title = t[0];
            subtitle = t.length > 1 ? t[1] : "";
            fadeIn = t.length > 2 ? parseInt(t[2]) : 5;
            stay = t.length > 3 ? parseInt(t[3]) : 5;
            fadeOut = t.length > 4 ? parseInt(t[4]) : 5;
        }
        TABAdditions.getInstance().getPlatform().sendTitle(p,parseText(title,p),parseText(subtitle,p),fadeIn,stay,fadeOut);
    }

    @Override
    public void onJoin(TabPlayer p) {
        p.loadPropertyFromConfig(this,"join-title");
        Map<String,Object> tSection = TABAdditions.getInstance().getConfig(ConfigType.TITLE).getConfigurationSection("titles." + p.getProperty("join-title").get());
        if (tSection != null && tSection.size() >= 5) {
            List<Object> titleProperties = new ArrayList<>();
            for (String property : tSection.keySet())
                titleProperties.add(tSection.get(property));
            sendTitle(titleProperties, new String[]{},p);
        }
    }

    public void toggleTitle(String name) {
        TabPlayer p = TabAPI.getInstance().getPlayer(name);
        TranslationFile translation = TABAdditions.getInstance().getMsgs();

        if (toggleTitle.contains(name.toLowerCase())) {
            toggleTitle.remove(name.toLowerCase());
            p.sendMessage(translation.titleOn, true);
        } else {
            toggleTitle.add(name.toLowerCase());
            p.sendMessage(translation.titleOff, true);
        }
    }

    private int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return 5;
        }
    }

    private String parseText(String str, TabPlayer p) {
        return TABAdditions.getInstance().parsePlaceholders(str,p,this).replace("_"," ");
    }

    public List<String> getLists() {
        List<String> list = new ArrayList<>();
        for (Object key : TABAdditions.getInstance().getConfig(ConfigType.TITLE).getConfigurationSection("titles").keySet())
            list.add(key.toString());
        return list;
    }
    @Override
    public boolean onCommand(TabPlayer sender, String msg) {
        if (!msg.equals("/toggletitle")) return false;
        toggleTitle(sender.getName());
        return true;
    }
}
