package io.github.tanguygab.tabadditions.shared.features.layouts;

import io.github.tanguygab.tabadditions.shared.ConfigType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.event.CommandListener;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;

import java.util.*;

public class LayoutManager implements Loadable, JoinEventListener, CommandListener {
    private static LayoutManager instance;

    private final TabFeature feature;
    private String togglecmd;
    private final Map<String, Layout> layouts = new HashMap<>();
    private final Map<TabPlayer,String> players = new HashMap<>();
    public final Map<TabPlayer,String> toAdd = new HashMap<>();
    public final Map<TabPlayer,String> toRemove = new HashMap<>();
    public final List<TabPlayer> toggledOff = new ArrayList<>();

    public LayoutManager(TabFeature feature) {
        feature.setDisplayName("&aTAB+ Layout");
        this.feature = feature;
        instance = this;
        load();
    }

    public static LayoutManager getInstance() {
        return instance;
    }

    @Override
    public void load() {
        for (Object layout : TABAdditions.getInstance().getConfig(ConfigType.LAYOUT).getConfigurationSection("layouts").keySet())
            layouts.put(layout+"",new Layout(layout.toString()));
        togglecmd = TABAdditions.getInstance().getConfig(ConfigType.LAYOUT).getString("toggle-cmd","layout");
        showLayoutAll();
        refresh();
    }

    @Override
    public void unload() {
        removeLayoutAll();
        removeLayout();
    }

    public String getLayout(TabPlayer p) {
        String layout = TABAdditions.getInstance().getConfig(ConfigType.LAYOUT).getString("default-layout","");
        if (layout.equalsIgnoreCase("")) return "null";

        Layout l = layouts.get(layout);
        while (l != null && !l.isConditionMet(p)) {
            l = layouts.get(l.getChildLayout());
            if (l == null) return "null";
            layout = l.getName();
        }
        return layout;
    }

    private void refresh() {
        TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(100,"handling TAB+ Layout",feature, UsageType.REPEATING_TASK,()->{
            for (TabPlayer p : TAB.getInstance().getPlayers()) {
                if (!TABAdditions.getInstance().checkBedrock(p) && players.containsKey(p) && !players.get(p).equals(getLayout(p))) {
                    toRemove.put(p, players.get(p));
                    toAdd.put(p,getLayout(p));
                }
                if (!players.containsKey(p) && !getLayout(p).equals("null"))
                    toAdd.put(p,getLayout(p));
            }
            removeLayout();
            showLayout();
            for (TabPlayer p : players.keySet()) {
                layouts.get(players.get(p)).refreshPlaceholders(p);
                layouts.get(players.get(p)).refreshLists(p);
                layouts.get(players.get(p)).refreshSets(p);
            }
        });
    }

    public void showLayout() {
        List<TabPlayer> list = new ArrayList<>(toAdd.keySet());
        for (TabPlayer p : list) {
            Layout layout = layouts.get(toAdd.get(p));
            if (layout != null && !toggledOff.contains(p)) {
                List<PacketPlayOutPlayerInfo.PlayerInfoData> fps = new ArrayList<>(layout.fakeplayers.values());
                p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, fps));
                players.put(p, layout.getName());
                layout.players.add(p);
                toAdd.remove(p);
                Map<Integer,String> mapSlots = new HashMap<>();
                for (int i = 0; i < 80; i++)
                    mapSlots.put(i,"");
                layout.placeholdersToRefresh.put(p,mapSlots);
            }
        }
    }
    public void removeLayout() {
        List<TabPlayer> list = new ArrayList<>(toRemove.keySet());
        for (TabPlayer p : list) {
            Layout layout = layouts.get(toRemove.get(p));
            if (layout != null && !toggledOff.contains(p)) {
                List<PacketPlayOutPlayerInfo.PlayerInfoData> fps = new ArrayList<>(layout.fakeplayers.values());
                p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, fps));
                players.remove(p);
                layout.players.remove(p);
                toRemove.remove(p);
            }
        }
    }

    public void showLayoutAll() {
        for (TabPlayer p : TAB.getInstance().getPlayers())
            if (!TABAdditions.getInstance().checkBedrock(p) && !toggledOff.contains(p))
                toAdd.put(p,getLayout(p));
    }
    public void removeLayoutAll() {
        for (TabPlayer p : TAB.getInstance().getPlayers())
            if (!TABAdditions.getInstance().checkBedrock(p)  && !toggledOff.contains(p))
                toRemove.put(p,getLayout(p));
    }

    @Override
    public boolean onCommand(TabPlayer p, String msg) {
        if (msg.equals("/"+togglecmd) || msg.startsWith("/"+togglecmd+" ")) {
            String[] args = msg.split(" ");
            String type = "";
            if (args.length > 1) type = args[1];
            boolean output = true;
            if (args.length > 2) output = false;
            if (toggledOff.contains(p)) {
                if (type.equalsIgnoreCase("off")) {
                    if (output) p.sendMessage("&cYou have already disabled this!",true);
                } else {
                    toggledOff.remove(p);
                    toAdd.put(p,getLayout(p));
                    if (output) p.sendMessage("&7Enabled.",true);
                }
            } else {
                if (type.equalsIgnoreCase("on")) {
                    if (output) p.sendMessage("&cYou have already enabled this!", true);
                } else {
                    toggledOff.add(p);
                    toRemove.put(p, getLayout(p));
                    if (output) p.sendMessage("&7Disabled.",true);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public TabFeature getFeatureType() {
        return feature;
    }

    @Override
    public void onJoin(TabPlayer p) {
        if (TABAdditions.getInstance().checkBedrock(p)) return;
        toAdd.put(p,getLayout(p));
    }

}
