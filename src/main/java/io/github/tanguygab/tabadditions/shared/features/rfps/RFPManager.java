package io.github.tanguygab.tabadditions.shared.features.rfps;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.YamlConfigurationFile;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;

import java.util.*;

public class RFPManager {

    private static RFPManager instance;
    private final Map<String, RFP> rfps = new HashMap<>();

    public RFPManager() {
        instance = this;
        Map<String,Object> config = TABAdditions.getInstance().getConfig("").getConfigurationSection("fakeplayers");
        for (Object rfp : config.keySet())
            rfps.put(rfp+"",new RFP(rfp+"", (Map<String, Object>) config.get(rfp+"")));
        showRFPAll();

    }

    public static RFPManager getInstance() {
        return instance;
    }

    public void unregister() {
        removeRFPAll();
    }

    public List<RFP> getRFPS() {
        return new ArrayList<>(rfps.values());
    }
    public RFP getRFP(String name) {
        if (rfps.containsKey(name))
            return rfps.get(name);
        return null;
    }

    public String addRFP(String name) {
        if (rfps.containsKey(name)) return "&cThis FakePlayer already exists!";

        UUID uuid = UUID.randomUUID();

        YamlConfigurationFile config = TABAdditions.getInstance().getConfig("");

        config.set("fakeplayers."+name+".name", name);
        config.set("fakeplayers."+name+".uuid", uuid+"");
        config.save();

        RFP rfp = new RFP(name,config.getConfigurationSection("fakeplayers."+name));
        rfps.put(name,rfp);
        for (TabPlayer p : TAB.getInstance().getPlayers()) {
            p.sendCustomPacket(new PacketPlayOutScoreboardTeam(rfp.getSortingTeam()).setTeamOptions(69));
            String prefix = TABAdditions.getInstance().parsePlaceholders(rfp.getProps()[0],p);
            String suffix = TABAdditions.getInstance().parsePlaceholders(rfp.getProps()[1],p);
            PacketAPI.registerScoreboardTeam(p,rfp.getSortingTeam(),prefix,suffix,true,false, Collections.singletonList(rfp.getName()),null, TabFeature.NAMETAGS);
            p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, rfp.get(p)));

        }
        return "&aAdded FakePlayer";
    }
    public String deleteRFP(String name) {
        if (!rfps.containsKey(name)) return "&cThis FakePlayer doesn't exist!";

        RFP rfp = rfps.get(name);
        rfps.remove(name);
        TABAdditions.getInstance().getConfig("").set("fakeplayers."+name,null);
        for (TabPlayer p : TAB.getInstance().getPlayers())
            p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, rfp.get(p)));

        return "&aRemoved FakePlayer.";
    }

    public void showRFP(TabPlayer p) {
        List<PacketPlayOutPlayerInfo.PlayerInfoData> fps = new ArrayList<>();
        List<RFP> rfps = new ArrayList<>(this.rfps.values());
        for (RFP rfp : rfps) {
            fps.add(rfp.get(p));
            p.sendCustomPacket(new PacketPlayOutScoreboardTeam(rfp.getSortingTeam()).setTeamOptions(69));
            String prefix = TABAdditions.getInstance().parsePlaceholders(rfp.getProps()[0],p);
            String suffix = TABAdditions.getInstance().parsePlaceholders(rfp.getProps()[1],p);
            PacketAPI.registerScoreboardTeam(p,rfp.getSortingTeam(),prefix,suffix,true,false, Collections.singletonList(rfp.getName()),null, TabFeature.NAMETAGS);
        }
        p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, fps));
    }

    public void removeRFP(TabPlayer p) {
            List<PacketPlayOutPlayerInfo.PlayerInfoData> fps = new ArrayList<>();
            List<RFP> rfps = new ArrayList<>(this.rfps.values());
            for (RFP rfp : rfps) {
                fps.add(new PacketPlayOutPlayerInfo.PlayerInfoData(rfp.getUUID()));
                p.sendCustomPacket(new PacketPlayOutScoreboardTeam(rfp.getSortingTeam()).setTeamOptions(69));
            }
            p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, fps));
    }
    public void showRFPAll() {
        for (TabPlayer p : TAB.getInstance().getPlayers())
            showRFP(p);
    }

    public void removeRFPAll() {
        for (TabPlayer p : TAB.getInstance().getPlayers())
            removeRFP(p);
    }

}