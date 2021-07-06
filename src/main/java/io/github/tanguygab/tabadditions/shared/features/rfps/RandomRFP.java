package io.github.tanguygab.tabadditions.shared.features.rfps;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.layouts.sorting.Sorting;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;

import java.util.*;

public class RandomRFP extends RFP{

    public List<String> names = new ArrayList<>();
    public int min;
    public int max;
    public int time;

    public List<String> currentNames = new ArrayList<>();
    public Map<String,Object> lastskins = new HashMap<>();
    public int currentAmount = 0;

    public RandomRFP(String configname, Map<String, Object> config) {
        super(configname, config);
        names.addAll((ArrayList<String>) config.get("names"));
        min = (int) config.get("min");
        max = (int) config.get("max");
        time = (int) config.get("time");
    }

    @Override
    public String getName() {
        return "Random";
    }

    @Override
    public String getConfigName() {
        return "random";
    }

    @Override
    public PacketPlayOutPlayerInfo.PlayerInfoData get(TabPlayer p) {
        return super.get(p);
    }

    public int getCurrentAmount() {
        int amount = new Random().nextInt(max);
        if (amount < min) amount = min;
        currentAmount = amount;
        return amount;
    }

    public List<String> getCurrentNames() {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < currentAmount; i++)
            names.add(this.names.get(new Random().nextInt(this.names.size())));
        currentNames = names;
        return currentNames;
    }

    public void updateAmount() {
        getCurrentAmount();
        getCurrentNames();
    }

    public void update(TabPlayer p) {
        for (String name : getCurrentNames()) {

            PacketPlayOutPlayerInfo.PlayerInfoData fp = get(p);
            p.sendCustomPacket(new PacketPlayOutScoreboardTeam(getSortingTeam(name)));
            String[] props = getProps();
            String prefix = TABAdditions.getInstance().parsePlaceholders(props[0].replace("%name%",name), p);
            String suffix = TABAdditions.getInstance().parsePlaceholders(props[1].replace("%name%",name), p);
            String icon = TABAdditions.getInstance().parsePlaceholders(this.skin.replace("%name%",name),p);
            if (skin != null && !icon.equals(lastskins.get(name))) {
                fp.setSkin(skin);
                lastskins.put(name,icon);
                p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, fp));
                p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, fp));
            } else {
                p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, fp));
                p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_LATENCY, fp));
            }
            PacketAPI.registerScoreboardTeam(p, getSortingTeam(name), prefix, suffix, true, false, Collections.singletonList(getName()), null, TabFeature.NAMETAGS);
        }
    }
    public String getSortingTeam(String name) {
        String chars = Sorting.loadSortingList().get(super.group);
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
}
