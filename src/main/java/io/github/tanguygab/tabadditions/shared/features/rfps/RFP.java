package io.github.tanguygab.tabadditions.shared.features.rfps;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.Skins;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;

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

    public String getName() {
        return configname;
    }
    public UUID getUUID() {
        return uuid;
    }
    public String getInfo() {
        String[] props = getProps();
        return "&aInformation on FakePlayer "+ configname + ":" +
                "\nName: " + name +
                "\nLatency bar: " + latency +
                "\nSkin: " + skin +
                "\nGroup: " + group +
                "\nPrefix: " + props[0] +
                "\nSuffix: " + props[1];
    }
    public String[] getProps() {
        String prefix = "";
        String suffix = "";

        Map<String,Object> config = TABAdditions.getInstance().getConfig("").getConfigurationSection("fakeplayers."+configname);
        Map<String,Object> tabConfig = TAB.getInstance().getConfiguration().config.getConfigurationSection("Groups");

        if (config.containsKey("prefix"))
            prefix = config.get("prefix")+"";
        else if (tabConfig.containsKey(group) && ((Map<String,Object>)tabConfig.get(group)).containsKey("tabprefix"))
            prefix = ((Map<String,Object>)tabConfig.get(group)).get("tabprefix")+"";
        else if (tabConfig.containsKey("_OTHER_") && ((Map<String,Object>)tabConfig.get("_OTHER_")).containsKey("tabprefix"))
            prefix = ((Map<String,Object>)tabConfig.get("_OTHER_")).get("tabprefix")+"";

        if (config.containsKey("suffix"))
            suffix = config.get("suffix")+"";
        else if (tabConfig.containsKey(group) && ((Map<String,Object>)tabConfig.get(group)).containsKey("tabsuffix"))
            suffix = ((Map<String,Object>)tabConfig.get(group)).get("tabsuffix")+"";
        else if (tabConfig.containsKey("_OTHER_") && ((Map<String,Object>)tabConfig.get("_OTHER_")).containsKey("tabsuffix"))
            suffix = ((Map<String,Object>)tabConfig.get("_OTHER_")).get("tabsuffix")+"";

        return new String[]{prefix,suffix};
    }

    public PacketPlayOutPlayerInfo.PlayerInfoData get(TabPlayer p) {
        PacketPlayOutPlayerInfo.PlayerInfoData rfp = new PacketPlayOutPlayerInfo.PlayerInfoData(uuid);
        rfp.name = name;
        rfp.displayName = IChatBaseComponent.fromColoredText(TABAdditions.getInstance().parsePlaceholders(getProps()[0]+name+getProps()[1],p));
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

    public void forceUpdate() {
        for (TabPlayer p : TAB.getInstance().getPlayers()) {
            p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, get(p)));
            p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, get(p)));
        }
    }

    public String setName(String value) {
        String old = name;
        name = value;
        TABAdditions.getInstance().getConfig("").set("fakeplayers." + configname + ".name", value);
        TABAdditions.getInstance().getConfig("").save();
        return "&aFakePlayer's name changed from "+old+" to "+value+".";
    }
    public String setSkin(String value) {
        String old = skin;
        skin = value;
        TABAdditions.getInstance().getConfig("").set("fakeplayers." + configname + ".skin", value);
        TABAdditions.getInstance().getConfig("").save();
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
            TABAdditions.getInstance().getConfig("").save();
            return "&aFakePlayer latency changed from "+old+" to "+num+".";
        } catch (NumberFormatException ignored) {
            return "&cCouldn't change FakePlayer's latency to "+name+", provided a number!";
        }
    }
    public String setGroup(String value) {
        String old = group;
        group = value;
        TABAdditions.getInstance().getConfig("").set("fakeplayers." + configname + ".group", value);
        TABAdditions.getInstance().getConfig("").save();
        return "&aFakePlayer's group changed from "+old+" to "+value+".";
    }
    public String setPrefix(String value) {
        TABAdditions.getInstance().getConfig("").set("fakeplayers." + configname + ".prefix", value);
        TABAdditions.getInstance().getConfig("").save();
        return "&aFakePlayer's prefix changed to "+value+".";
    }
    public String setSuffix(String value) {
        TABAdditions.getInstance().getConfig("").set("fakeplayers." + configname + ".suffix", value);
        TABAdditions.getInstance().getConfig("").save();
        return "&aFakePlayer's prefix changed to "+value+".";
    }

}
