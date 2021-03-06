package io.github.tanguygab.tabadditions.shared.features.rfps;

import io.github.tanguygab.tabadditions.shared.ConfigType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.TAFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.YamlConfigurationFile;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;

import java.util.*;

public class RFPManager implements JoinEventListener, Loadable {

    private final Map<String, RFP> rfps = new HashMap<>();
    public RandomRFP randomRFP;

    public RFPManager() {
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
            p.sendCustomPacket(new PacketPlayOutScoreboardTeam(rfp.getSortingTeam()));
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
                p.sendCustomPacket(new PacketPlayOutScoreboardTeam(rfp.getSortingTeam()));
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
                p.sendCustomPacket(new PacketPlayOutScoreboardTeam(rfp.getSortingTeam()));
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
        for (Object rfp : config.keySet()) {
            if (!rfp.equals("random"))
                rfps.put(rfp + "", new RFP(rfp + "", (Map<String, Object>) config.get(rfp + "")));
            else {
                randomRFP = new RandomRFP(rfp + "", (Map<String, Object>) config.get(rfp+""));
            }
        }
        showRFPAll();
        refresh();
    }

    public void refresh() {
        TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(500,"refreshing RFPs",TAFeature.RFP, UsageType.REFRESHING,() -> {
            for (TabPlayer p : TAB.getInstance().getPlayers()) {
                updateRFPs(p);
            }
        });
    }

    public void updateRFPs(TabPlayer p) {
        List<RFP> rfps = new ArrayList<>(this.rfps.values());
        for (RFP rfp : rfps) {
            Object skin = TABAdditions.getInstance().getSkins().getIcon(rfp.skin, p);
            if (skin != null) {
                if (!TABAdditions.getInstance().enabled) return;
                rfp.update(p, skin);
            }
        }
        //if (randomRFP != null) randomRFP.update(p);
    }

    @Override
    public void unload() {
        removeRFPAll();
    }

    @Override
    public void onJoin(TabPlayer p) {
        showRFP(p);
        updateRFPs(p);
    }

    @Override
    public Object getFeatureType() {
        return TAFeature.RFP;
    }


}
