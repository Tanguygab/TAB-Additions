package io.github.tanguygab.tabadditions.shared.features.rfps;

import io.github.tanguygab.tabadditions.shared.ConfigType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.PropertyConfiguration;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.config.YamlConfigurationFile;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardTeam;

import java.util.*;

public class RFP {

    private final YamlConfigurationFile configfile;
    protected final RFPManager feature;

    private final String configname;
    private String name;
    private final UUID uuid;
    private int latency;
    public String skin;
    protected String group;
    public String lastskin;

    public RFP(String configname,Map<String,Object> config, RFPManager feature) {
        this.feature = feature;
        configfile = TABAdditions.getInstance().getConfig(ConfigType.MAIN);
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
    public String getInfo(TabPlayer p) {
        String[] props = getProps(p);
        return "&6Information on FakePlayer &2"+ configname + "&6:" +
                "\n&aName: &f" + name +
                "\n&aLatency bar: &f" + latency +
                "\n&aSkin: &f" + skin +
                "\n&aGroup: &f" + group +
                "\n&aPrefix: &f" + props[0] +
                "\n&aSuffix: &f" + props[1] +
                "\n&aTeam: &f" + getSortingTeam();
    }
    public String[] getProps(TabPlayer p) {
        PropertyConfiguration groupconfig = TabAPI.getInstance().getGroups();

        String prefix = "";
        String suffix = "";
        String[] prefix2 = groupconfig.getProperty(group,"prefix",p.getServer(),p.getWorld());
        if (prefix2.length > 0)
        prefix = prefix2[0];
        String[] suffix2 = groupconfig.getProperty(group,"suffix",p.getServer(),p.getWorld());
        if (suffix2.length > 0)
            suffix = suffix2[0];

        return new String[]{prefix,suffix};
    }

    public PacketPlayOutPlayerInfo.PlayerInfoData get(TabPlayer p) {
        PacketPlayOutPlayerInfo.PlayerInfoData rfp = new PacketPlayOutPlayerInfo.PlayerInfoData(uuid);
        rfp.setName(getName());
        String[] props = getProps(p);
        props[0] = TABAdditions.getInstance().parsePlaceholders(props[0],p,feature);
        props[1] = TABAdditions.getInstance().parsePlaceholders(props[1],p,feature);


        rfp.setDisplayName(IChatBaseComponent.optimizedComponent(props[0]+getName()+props[1]));
        rfp.setUniqueId(uuid);
        if (configfile.getBoolean("real-latency",false))
            rfp.setLatency(latency);
        else if (latency <= 1)
            rfp.setLatency(1000);
        else if (latency == 2)
            rfp.setLatency(600);
        else if (latency == 3)
            rfp.setLatency(300);
        else if (latency == 4)
            rfp.setLatency(200);
        else rfp.setLatency(100);
        return rfp;
    }
    public String getSortingTeam() {
        String groups = null;
        for (String str : TabAPI.getInstance().getConfig().getStringList("scoreboard-teams.sorting-types", new ArrayList<>())) {
            if (str.startsWith("GROUPS:")) {
                groups = str.replace("GROUPS:","");
                break;
            }
        }
        String chars;
        if (groups != null) {
            chars = convertSortingElements(groups.split(",")).get(group);
            if (chars == null) chars = "9";
        } else chars = "9";

        int id = 65;
        boolean done = false;
        String potentialTeamName = chars+name+"";
        if (potentialTeamName.length() > 15)
            potentialTeamName = potentialTeamName.substring(0, 15);
        potentialTeamName += (char) id;
        while (!done) {
            for (TabPlayer all : TabAPI.getInstance().getOnlinePlayers()) {
                if (all.getTeamName() != null && all.getTeamName().equals(potentialTeamName)) {
                    id = id + 1;
                }
            }
            done = true;
        }
        return potentialTeamName;
    }
    protected LinkedHashMap<String, String> convertSortingElements(String[] elements) {
        LinkedHashMap<String, String> sortedGroups = new LinkedHashMap();
        int index = 1;
        int charCount = String.valueOf(elements.length).length();
        String[] var5 = elements;
        int var6 = elements.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            String group;
            for (group = var5[var7]; group.startsWith(" "); group = group.substring(1)) {
            }

            while(group.endsWith(" ")) {
                group = group.substring(0, group.length() - 1);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(index);

            while(sb.length() < charCount) {
                sb.insert(0, "0");
            }

            String[] var10 = group.toLowerCase().split(" ");
            int var11 = var10.length;

            for (int var12 = 0; var12 < var11; ++var12) {
                String group0 = var10[var12];
                sortedGroups.put(group0, sb.toString());
            }

            ++index;
        }

        return sortedGroups;
    }

    public void update(TabPlayer p, Object skin) {
        PacketPlayOutPlayerInfo.PlayerInfoData fp = get(p);
        p.sendCustomPacket(new PacketPlayOutScoreboardTeam(getSortingTeam()),feature);
        String[] props = getProps(p);
        String prefix = TABAdditions.getInstance().parsePlaceholders(props[0],p,feature);
        String suffix = TABAdditions.getInstance().parsePlaceholders(props[1],p,feature);
        String icon = TABAdditions.getInstance().parsePlaceholders(this.skin,p,feature);
        if (skin != null && !icon.equals(lastskin)) {
            fp.setSkin(skin);
            lastskin = icon;
            p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, fp),feature);
            p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, fp),feature);
        } else {
            p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, fp),feature);
            p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_LATENCY , fp),feature);
        }
        feature.registerScoreboardTeam(p,getSortingTeam(),prefix,suffix,getName());
    }

    public String setName(String value) {
        String old = name;
        if (value == null)
            name = "";
        else name = value;
        configfile.set("fakeplayers." + configname + ".name", value);
        return "&aFakePlayer's name changed from "+old+" to "+value+".";
    }
    public String setSkin(String value) {
        String old = skin;
        if (value == null)
            skin = "";
        else skin = value;
        configfile.set("fakeplayers." + configname + ".skin", value);
        return "&aFakePlayer's skin changed from "+old+" to "+value+".";
    }
    public String setLatency(String value) {
        int old = latency;
        try {
            int num = Integer.parseInt(value);
            if (configfile.getBoolean("real-latency",false))
                latency = num;
            else if (num < 1) num = 1;
            else if (num > 5) num = 5;
            latency = num;
            configfile.set("fakeplayers." + configname + ".latency", num);
            return "&aFakePlayer latency changed from "+old+" to "+num+".";
        } catch (NumberFormatException ignored) {
            return "&cCouldn't change FakePlayer's latency to "+name+", provided a number!";
        }
    }
    public String setGroup(String value) {
        String old = group;

        if (value == null)
            group = "";
        else {
            value = value.toLowerCase();
            group = value;
        }
        configfile.set("fakeplayers." + configname + ".group", value);
        return "&aFakePlayer's group changed from "+old+" to "+value+".";
    }
    public String setPrefix(String value) {
        configfile.set("fakeplayers." + configname + ".prefix", value);
        return "&aFakePlayer's prefix changed to "+value+".";
    }
    public String setSuffix(String value) {
        configfile.set("fakeplayers." + configname + ".suffix", value);
        return "&aFakePlayer's prefix changed to "+value+".";
    }

}
