package io.github.tanguygab.tabadditions.shared.features.layouts;

import io.github.tanguygab.tabadditions.shared.PlatformType;
import io.github.tanguygab.tabadditions.shared.Skins;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.layouts.sorting.Sorting;
import me.neznamy.tab.api.Property;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.CrossPlatformPacket;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore;
import me.neznamy.tab.shared.PropertyImpl;
import me.neznamy.tab.shared.TAB;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerSet {

    private final String layout;
    private final TabFeature feature;
    private final Map<String,Object> config;

    private Sorting sorting;
    private List<PacketPlayOutPlayerInfo.PlayerInfoData> fakeplayers;
    private Map<TabPlayer, List<Property>> properties;

    public PlayerSet(TabFeature feature, String layout, Map<String, Object> config) {
        this.feature = feature;
        this.layout = layout;
        this.config = config;
    }

    public List<PacketPlayOutPlayerInfo.PlayerInfoData> getFakePlayers() {
        return fakeplayers;
    }

    public void refresh(TabPlayer viewer) {
        List<TabPlayer> inSet = playerSet(config,viewer);
        int inList = 0;
        for (PacketPlayOutPlayerInfo.PlayerInfoData fp : fakeplayers) {
            TabPlayer pInSet = null;
            if (inSet.size() > inList)
                pInSet = inSet.get(inList);

            List<CrossPlatformPacket> packets;
            if (inSet.size() <= inList || pInSet == null)
                packets = slot(viewer,pInSet,fp, (Map<String, Object>) config.get("empty"),config,inSet.indexOf(pInSet)+1);
            else if (fakeplayers.size() == inList+1 && inSet.size() != fakeplayers.size() && config.containsKey("more"))
                packets = slot(viewer,pInSet,fp, (Map<String, Object>) config.get("more"),config,inSet.indexOf(pInSet)+1);
            else
                packets = slot(viewer,pInSet,fp, config,config,inSet.indexOf(pInSet)+1);

            packets.forEach(packet -> viewer.sendCustomPacket(packet,feature));
        }

    }

    public List<CrossPlatformPacket> slot(TabPlayer viewer, TabPlayer p, PacketPlayOutPlayerInfo.PlayerInfoData fp, Map<String, Object> section, Map<String, Object> def, int place) {
        String name = get(section,"text",def,place);
        String icon = get(section,"icon",def,place);
        String latency = get(section,"latency",def,place);
        String yellownumber = get(section,"yellow-number",def,place);

        String uuid2 = fp.getUniqueId().toString();
        if (!properties.containsKey(viewer)) properties.put(viewer, new ArrayList<>());
        properties.get(viewer).add(prop(p,uuid2+"-name-"+name));
        properties.get(viewer).add(prop(p,uuid2+"-icon-"+icon));
        properties.get(viewer).add(prop(p,uuid2+"-latency-"+latency));
        properties.get(viewer).add(prop(p,uuid2+"-yellownumber-"+yellownumber));


        IChatBaseComponent displayName = IChatBaseComponent.optimizedComponent(getProp(viewer,uuid2,"name"));
        Object skin = Skins.getInstance().getIcon(getProp(viewer,uuid2,"icon"));
        int lat = TabAPI.getInstance().getErrorManager().parseInteger(getProp(viewer,uuid2,"latency"),0,"TAB+ Layout's latency");


        return Arrays.asList(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, new PacketPlayOutPlayerInfo.PlayerInfoData(
            fp.getName(),fp.getUniqueId(),skin,lat, fp.getGameMode(),displayName)),
                new PacketPlayOutScoreboardScore(PacketPlayOutScoreboardScore.Action.CHANGE,
                        "TAB-YellowNumber",
                        fp.getName(),
                        TabAPI.getInstance().getErrorManager().parseInteger(getProp(viewer,uuid2,"yellownumber"),0,"layout"))
                );
    }

    private String getProp(TabPlayer p, String uuid, String name) {
        if (!properties.containsKey(p)) return "";
        for (Property prop : properties.get(p)) {
            if (prop.get().startsWith("TAB+-Layout-"+uuid+"-"+name)) return prop.updateAndGet().replace("TAB+-Layout-"+uuid+"-"+name+"-","");
        }
        return "";
    }

    private Property prop(TabPlayer p, String str) {
        return new PropertyImpl(feature,p,"TAB+-Layout-"+str,"TAB+ Layout");
    }

    private String get(Map<String,Object> section, String key, Map<String,Object> def, int place) {
        return String.valueOf(section.containsKey(key) ? section.get(key) : def.get(key)).replace("%place%",place+"");
    }

    protected List<TabPlayer> playerSet(Object slot, TabPlayer viewer) {
        Map<String, Object> section = (Map<String, Object>) slot;

        List<TabPlayer> list = new ArrayList<>(TAB.getInstance().getOnlinePlayers());
        if (section.get("condition") != null && !section.get("condition").toString().equals("")) {
            String cond = section.get("condition") + "";
            list.removeIf(p -> !TABAdditions.getInstance().isConditionMet(cond, p, viewer, p, feature));
        }
        if (section.get("vanished") == null || !Boolean.parseBoolean("" + section.get("vanished"))) {
            if (TABAdditions.getInstance().getPlatform().getType() == PlatformType.SPIGOT)
                list.removeIf(p -> !viewer.hasPermission("tab.seevanished") && !((Player) viewer.getPlayer()).canSee(((Player) p.getPlayer())));
            list.removeIf(p -> !viewer.hasPermission("tab.seevanished") && p.isVanished());
        }

        if (!section.containsKey("sorting")) {
            Map<String, TabPlayer> pSorted = new TreeMap<>();
            for (TabPlayer p : list)
                if (p != null && p.getTeamName() != null)
                    pSorted.put(p.getTeamName(), p);
            return new ArrayList<>(pSorted.values());
        }

        if (sorting == null)
            sorting = new Sorting((Map<String, Object>) section.get("sorting"), list, layout);
        Map<String,TabPlayer> pSorted = new TreeMap<>();
        for (TabPlayer p : list)
            if (p != null && p.getTeamName() != null)
                pSorted.put(sorting.getPosition(p) + p.getName(), p);
        return new ArrayList<>(pSorted.values());
    }
}
