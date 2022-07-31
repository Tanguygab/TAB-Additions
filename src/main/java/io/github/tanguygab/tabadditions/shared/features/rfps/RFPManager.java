package io.github.tanguygab.tabadditions.shared.features.rfps;

import io.github.tanguygab.tabadditions.shared.ConfigType;
import io.github.tanguygab.tabadditions.shared.PlatformType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.api.protocol.Skin;

import java.util.*;
import java.util.concurrent.Future;

public class RFPManager extends TabFeature {

    private final Map<String, RFP> rfps = new HashMap<>();

    private final TabAPI tab;
    private Future<?> task;

    public RFPManager() {
        super("Real Fake Players","&aReal Fake Players&r");
        tab = TabAPI.getInstance();
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

        ConfigurationFile config = TABAdditions.getInstance().getConfig(ConfigType.MAIN);

        config.set("fakeplayers."+name+".name", name);
        config.set("fakeplayers."+name+".uuid", uuid+"");
        config.save();

        RFP rfp = new RFP(name,config.getConfigurationSection("fakeplayers."+name),this);
        rfps.put(name,rfp);
        for (TabPlayer p : tab.getOnlinePlayers()) {
            p.sendCustomPacket(new PacketPlayOutScoreboardTeam(rfp.getSortingTeam()),this);
            String prefix = TABAdditions.getInstance().parsePlaceholders(rfp.getProps(p)[0],p,this);
            String suffix = TABAdditions.getInstance().parsePlaceholders(rfp.getProps(p)[1],p,this);
            registerScoreboardTeam(p,rfp.getSortingTeam(),prefix,suffix, rfp.getName());
            p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, rfp.get(p)),this);

        }
        return "&aAdded FakePlayer";
    }

    protected void registerScoreboardTeam(TabPlayer p, String teamName, String prefix, String suffix, String rfpname) {
        if (p.getVersion().getMinorVersion() >= 8 && tab.getConfig().getBoolean("unregister-before-register",true) && TABAdditions.getInstance().getPlatform().getType() == PlatformType.SPIGOT) {
            p.sendCustomPacket(new PacketPlayOutScoreboardTeam(teamName), this);
        }
        p.sendCustomPacket(new PacketPlayOutScoreboardTeam(teamName, prefix, suffix, "always", "never", Collections.singleton(rfpname), 0), this);
    }

    public String deleteRFP(String name) {
        if (!rfps.containsKey(name)) return "&cThis FakePlayer doesn't exist!";

        RFP rfp = rfps.get(name);
        rfps.remove(name);
        TABAdditions.getInstance().getConfig(ConfigType.MAIN).set("fakeplayers."+name,null);
        for (TabPlayer p : tab.getOnlinePlayers())
            p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, rfp.get(p)),this);

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
                p.sendCustomPacket(new PacketPlayOutScoreboardTeam(rfp.getSortingTeam()),this);
                String prefix = TABAdditions.getInstance().parsePlaceholders(rfp.getProps(p)[0],p,this);
                String suffix = TABAdditions.getInstance().parsePlaceholders(rfp.getProps(p)[1],p,this);
                registerScoreboardTeam(p,rfp.getSortingTeam(),prefix,suffix,rfp.getName());
            }
            p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, fps),this);
    }
    public void removeRFP(TabPlayer p) {
            List<PacketPlayOutPlayerInfo.PlayerInfoData> fps = new ArrayList<>();
            List<RFP> rfps = new ArrayList<>(this.rfps.values());
            for (RFP rfp : rfps) {
                fps.add(new PacketPlayOutPlayerInfo.PlayerInfoData(rfp.getUUID()));
                p.sendCustomPacket(new PacketPlayOutScoreboardTeam(rfp.getSortingTeam()),this);
            }
            p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, fps),this);
    }

    public void showRFPAll() {
        for (TabPlayer p : tab.getOnlinePlayers())
            showRFP(p);
    }
    public void removeRFPAll() {
        for (TabPlayer p : tab.getOnlinePlayers())
            removeRFP(p);
    }

    @Override
    public void load() {
        Map<String,Object> config = TABAdditions.getInstance().getConfig(ConfigType.MAIN).getConfigurationSection("fakeplayers");
        for (Object rfp : config.keySet())
            rfps.put(rfp + "", new RFP(rfp + "", (Map<String, Object>) config.get(rfp + ""),this));
        showRFPAll();
        refresh();
    }

    public void refresh() {
        task = tab.getThreadManager().startRepeatingMeasuredTask(500,this,"refreshing RFPs",() -> {
            for (TabPlayer p : tab.getOnlinePlayers()) {
                updateRFPs(p);
            }
        });

    }

    public void updateRFPs(TabPlayer p) {
        List<RFP> rfps = new ArrayList<>(this.rfps.values());
        for (RFP rfp : rfps) {
            Skin skin = TABAdditions.getInstance().getSkins().getSkin(rfp.skin);
            if (skin != null) {
                if (!TABAdditions.getInstance().enabled) return;
                rfp.update(p, skin);
            }
        }
    }

    @Override
    public void unload() {
        task.cancel(true);
        removeRFPAll();
    }

    @Override
    public void onJoin(TabPlayer p) {
        showRFP(p);
        updateRFPs(p);
    }
}
