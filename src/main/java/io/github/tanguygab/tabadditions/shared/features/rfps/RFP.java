package io.github.tanguygab.tabadditions.shared.features.rfps;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.Skins;
import io.github.tanguygab.tabadditions.shared.features.layouts.sorting.Sorting;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public class RFP {

    private final String configname;
    private String name;
    private final UUID uuid;
    private int latency;
    private String skin;
    private String group;

    public RFP(String configname,Map<String,Object> config) {
        this.configname = configname;
        if (config.containsKey("name"))
            name = config.get("name") + "";
        else name = "";

        if (config.containsKey("skin"))
            skin = config.get("skin")+"";
        else skin = "";

        if (config.containsKey("latency"))
            latency = (int) config.get("latency");
        else latency = 5;

        if (config.containsKey("uuid"))
            uuid = UUID.fromString(config.get("uuid")+"");
        else uuid = UUID.randomUUID();

        if (config.containsKey("group"))
            group = config.get("group")+"";
        else group = "";
    }

    public String getConfigName() {
        return configname;
    }
    public String getName() {
        String value = name;
        if (value.length() > 15)
            value = value.substring(0, 15);
        return value;
    }
    public UUID getUUID() {
        return uuid;
    }
    public String getInfo() {
        String[] props = getProps();
        return "&6Information on FakePlayer &2"+ configname + "&6:" +
                "\n&aName: &f" + name +
                "\n&aLatency bar: &f" + latency +
                "\n&aSkin: &f" + skin +
                "\n&aGroup: &f" + group +
                "\n&aPrefix: &f" + props[0] +
                "\n&aSuffix: &f" + props[1] +
                "\n&aTeam: &f" + getSortingTeam();
    }
    public String[] getProps() {
        String prefix = "";
        String suffix = "";

        Map<String,Object> config = TABAdditions.getInstance().getConfig("").getConfigurationSection("fakeplayers."+configname);
        Map<String,Object> tabConfig = TAB.getInstance().getConfiguration().config.getConfigurationSection("Groups");

        String configGroup = "";
        for (String g : tabConfig.keySet()) {
            if (g.equalsIgnoreCase(group)) {
                configGroup = g;
                break;
            }
        }

        if (config.containsKey("prefix"))
            prefix = config.get("prefix")+"";
        else if (!configGroup.equals("") && ((Map<String,Object>)tabConfig.get(configGroup)).containsKey("tabprefix"))
            prefix = ((Map<String,Object>)tabConfig.get(configGroup)).get("tabprefix")+"";
        else if (tabConfig.containsKey("_OTHER_") && ((Map<String,Object>)tabConfig.get("_OTHER_")).containsKey("tabprefix"))
            prefix = ((Map<String,Object>)tabConfig.get("_OTHER_")).get("tabprefix")+"";

        if (config.containsKey("suffix"))
            suffix = config.get("suffix")+"";
        else if (!configGroup.equals("") && ((Map<String,Object>)tabConfig.get(configGroup)).containsKey("tabsuffix"))
            suffix = ((Map<String,Object>)tabConfig.get(configGroup)).get("tabsuffix")+"";
        else if (tabConfig.containsKey("_OTHER_") && ((Map<String,Object>)tabConfig.get("_OTHER_")).containsKey("tabsuffix"))
            suffix = ((Map<String,Object>)tabConfig.get("_OTHER_")).get("tabsuffix")+"";

        return new String[]{prefix,suffix};
    }

    public PacketPlayOutPlayerInfo.PlayerInfoData get(TabPlayer p) {
        PacketPlayOutPlayerInfo.PlayerInfoData rfp = new PacketPlayOutPlayerInfo.PlayerInfoData(uuid);
        rfp.name = getName();
        rfp.uniqueId = uuid;
        rfp.skin = Skins.getIcon(skin, p);
        if (latency <= 1)
            rfp.latency = 1000;
        else if (latency == 2)
            rfp.latency = 600;
        else if (latency == 3)
            rfp.latency = 300;
        else if (latency == 4)
            rfp.latency = 200;
        else rfp.latency = 100;
        return rfp;
    }
    public String getSortingTeam() {
        String chars = Sorting.loadSortingList().get(group);
        if (chars == null) chars = "9";

        int id = 65;
        boolean done = false;
        String potentialTeamName = chars+name+"";
        if (potentialTeamName.length() > 15)
            potentialTeamName = potentialTeamName.substring(0, 15);
        potentialTeamName += (char) id;
        while (!done) {
            for (TabPlayer all : TAB.getInstance().getPlayers()) {
                if (all.getTeamName() != null && all.getTeamName().equals(potentialTeamName)) {
                    id = id + 1;
                }
            }
            done = true;
        }
        return potentialTeamName;
    }

    public void forceUpdate() {
        for (TabPlayer p : TAB.getInstance().getPlayers()) {
            p.sendCustomPacket(new PacketPlayOutScoreboardTeam(getSortingTeam()).setTeamOptions(69));
            p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, get(p)));
            String prefix = TABAdditions.getInstance().parsePlaceholders(getProps()[0],p);
            String suffix = TABAdditions.getInstance().parsePlaceholders(getProps()[1],p);
            PacketAPI.registerScoreboardTeam(p,getSortingTeam(),prefix,suffix,true,false, Collections.singletonList(getName()),null, TabFeature.NAMETAGS);
            p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, get(p)));
        }
    }

    public String setName(String value) {
        String old = name;
        name = value;
        TABAdditions.getInstance().getConfig("").set("fakeplayers." + configname + ".name", value);
        return "&aFakePlayer's name changed from "+old+" to "+value+".";
    }
    public String setSkin(String value) {
        String old = skin;
        skin = value;
        TABAdditions.getInstance().getConfig("").set("fakeplayers." + configname + ".skin", value);
        return "&aFakePlayer's skin changed from "+old+" to "+value+".";
    }
    public String setLatency(String value) {
        int old = latency;
        try {
            int num = Integer.parseInt(value);
            if (num < 1) num = 1;
            if (num > 5) num = 5;
            latency = num;
            TABAdditions.getInstance().getConfig("").set("fakeplayers." + configname + ".latency", num);
            return "&aFakePlayer latency changed from "+old+" to "+num+".";
        } catch (NumberFormatException ignored) {
            return "&cCouldn't change FakePlayer's latency to "+name+", provided a number!";
        }
    }
    public String setGroup(String value) {
        value = value.toLowerCase();
        String old = group;
        group = value;
        TABAdditions.getInstance().getConfig("").set("fakeplayers." + configname + ".group", value);
        return "&aFakePlayer's group changed from "+old+" to "+value+".";
    }
    public String setPrefix(String value) {
        TABAdditions.getInstance().getConfig("").set("fakeplayers." + configname + ".prefix", value);
        return "&aFakePlayer's prefix changed to "+value+".";
    }
    public String setSuffix(String value) {
        TABAdditions.getInstance().getConfig("").set("fakeplayers." + configname + ".suffix", value);
        return "&aFakePlayer's prefix changed to "+value+".";
    }

}
