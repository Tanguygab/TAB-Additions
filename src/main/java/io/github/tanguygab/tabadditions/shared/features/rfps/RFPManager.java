package io.github.tanguygab.tabadditions.shared.features.rfps;

import io.github.tanguygab.tabadditions.shared.ConfigType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.YamlConfigurationFile;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;

import java.util.*;

public class RFPManager implements JoinEventListener, Loadable {

    private final TabFeature feature;
    private final Map<String, RFP> rfps = new HashMap<>();
    private final Map<TabPlayer,Map<RFP, Object>> skins = new HashMap<>();

    public RFPManager(TabFeature feature) {
        feature.setDisplayName("&aReal Fake Players");
        this.feature = feature;
        load();
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

        YamlConfigurationFile config = TABAdditions.getInstance().getConfig(ConfigType.MAIN);

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
        TABAdditions.getInstance().getConfig(ConfigType.MAIN).set("fakeplayers."+name,null);
        for (TabPlayer p : TAB.getInstance().getPlayers())
            p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, rfp.get(p)));

        return "&aRemoved FakePlayer.";
    }
    public String deleteAll() {
        if (rfps.isEmpty()) return "&cNo FakePlayers found.";

        List<String> list = new ArrayList<>(rfps.keySet());
        for (String name : list)
            deleteRFP(name);
        return "&aRemoved all FakePlayers.";
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

    @Override
    public void load() {
        Map<String,Object> config = TABAdditions.getInstance().getConfig(ConfigType.MAIN).getConfigurationSection("fakeplayers");
        for (Object rfp : config.keySet())
            rfps.put(rfp+"",new RFP(rfp+"", (Map<String, Object>) config.get(rfp+"")));
        showRFPAll();
        refresh();
    }

    @Override
    public void unload() {
        removeRFPAll();
    }

    public void refresh() {
        for (TabPlayer p : TAB.getInstance().getPlayers()) {
            TAB.getInstance().getCPUManager().runTask("refreshing RFPs skins for"+p.getName(),()->{
                List<RFP> rfps = new ArrayList<>(this.rfps.values());
                if (!skins.containsKey(p))
                    skins.put(p, new HashMap<>());

                for (RFP rfp : rfps) {
                    TAB.getInstance().getCPUManager().runTask("refreshing RFP ("+rfp.getConfigName()+") skin for "+p.getName(),()->{
                        Object skin = TABAdditions.getInstance().getSkins().getIcon(rfp.skin, p);
                        if (skin != null && skins.get(p).get(rfp) != skin) {
                            if (p != null)
                                rfp.forceUpdate(p,skin);
                        }
                        skins.get(p).put(rfp, skin);
                    });
                }
            });
        }
    }

    @Override
    public void onJoin(TabPlayer p) {
        showRFPAll();
    }

    @Override
    public TabFeature getFeatureType() {
        return feature;
    }


}
