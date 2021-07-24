package io.github.tanguygab.tabadditions.shared.features.layouts;

import io.github.tanguygab.tabadditions.shared.ConfigType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.TAFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.event.CommandListener;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;
import me.neznamy.tab.shared.features.types.event.QuitEventListener;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;

import java.util.*;

public class LayoutManager implements Loadable, JoinEventListener, CommandListener, QuitEventListener {

    private static LayoutManager instance;

    private String togglecmd;
    private final Map<String, Layout> layouts = new HashMap<>();
    private final Map<TabPlayer,String> players = new HashMap<>();
    public final Map<TabPlayer,String> toAdd = new HashMap<>();
    public final Map<TabPlayer,String> toRemove = new HashMap<>();
    public final List<TabPlayer> toggledOff = new ArrayList<>();
    public final List<String> chars = new ArrayList<>();

    public LayoutManager() {
        instance = this;
        load();
    }

    public static LayoutManager getInstance() {
        return instance;
    }

    @Override
    public void load() {
        List<String> defaultchars = Arrays.asList("\u2764","\u2B50","\u21EE","\u2740","\u2694","\u26CF","\u291C\u25BA","\u00BB","\u00AB","\u2660","\u25B6",
                "\u25CF","\u2756","\u2589","\u25B2","\u25BC","\u272F","\u2726","\u2620","\u2622","\u2680","\u2681","\u2682","\u2683","\u2684","\u2685");
        List<String> charsconfig = TABAdditions.getInstance().getConfig(ConfigType.MAIN).getStringList("layout-characters",defaultchars);
        if (charsconfig != null && charsconfig.size() != 0)
            chars.addAll(charsconfig);
        else chars.addAll(defaultchars);
        Collections.sort(chars);


        for (Object layout : TABAdditions.getInstance().getConfig(ConfigType.LAYOUT).getConfigurationSection("layouts").keySet())
            layouts.put(layout+"",new Layout(layout.toString()));
        togglecmd = TABAdditions.getInstance().getConfig(ConfigType.LAYOUT).getString("toggle-cmd","layout");
        showLayoutAll();
        refresh();
        loadPlaceholders();
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
        TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(500,"handling TAB+ Layout",getFeatureType(), UsageType.REPEATING_TASK,()->{

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

            List<TabPlayer> list = new ArrayList<>(players.keySet());
            for (TabPlayer p : list) {
                if (p == null || !players.containsKey(p)) continue;
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
                Map<Integer,Map<String,String>> mapSlots = new HashMap<>();
                for (int i = 0; i < 80; i++) {
                    Map<String,String> map2 = new HashMap<>();
                    map2.put("text","");
                    map2.put("latency","");
                    map2.put("yellow-number","");
                    mapSlots.put(i, map2);
                }
                layout.placeholdersToRefresh.put(p,mapSlots);
                toAdd.remove(p);
                for (String fp : layout.fpnames.keySet())
                    PacketAPI.registerScoreboardTeam(p,"!"+fp+"TAB+_Layout","","",true,false, Collections.singleton(layout.fpnames.get(fp)), null, TabFeature.NAMETAGS);

            }
        }
    }
    public void removeLayout() {
        List<TabPlayer> list = new ArrayList<>(toRemove.keySet());
        for (TabPlayer p : list) {
            Layout layout = layouts.get(toRemove.get(p));
            if (layout != null) {
                List<PacketPlayOutPlayerInfo.PlayerInfoData> fps = new ArrayList<>(layout.fakeplayers.values());
                p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, fps));
                players.remove(p);
                layout.players.remove(p);
                layout.placeholdersToRefresh.remove(p);
                layout.skinsl.remove(p);
                layout.skinsp.remove(p);
                layout.skinss.remove(p);
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
    public Object getFeatureType() {
        return TAFeature.TA_LAYOUT;
    }

    @Override
    public void onJoin(TabPlayer p) {
        if (TABAdditions.getInstance().checkBedrock(p)) return;
        toAdd.put(p,getLayout(p));
    }

    @Override
    public void onQuit(TabPlayer p) {
        toAdd.remove(p);
        toggledOff.remove(p);
        toRemove.remove(p);
        players.remove(p);
    }

    public void loadPlaceholders() {
        PlaceholderManager pm = TAB.getInstance().getPlaceholderManager();
        for (Layout layout : layouts.values()) {
            for (String set : layout.setsnames.keySet())
                pm.registerPlaceholder(new PlayerPlaceholder("%layout-playerset:" + set + "%", 100) {
                    @Override
                    public String get(TabPlayer p) {
                        Layout l = layouts.get(getLayout(p));
                        if (l == null || !l.setsnames.containsKey(set)) return "0";
                        return l.playerSet(l.setsnames.get(set),p).size()+"";
                    }});
            for (String list : layout.listsnames.keySet())
                pm.registerPlaceholder(new PlayerPlaceholder("%layout-list:" + list + "%", 100) {
                    @Override
                    public String get(TabPlayer p) {
                        Layout l = layouts.get(getLayout(p));
                        if (l == null || !l.listsnames.containsKey(list)) return "0";
                        return l.lists(l.listsnames.get(list),p).size()+"";
                    }});
        }
        pm.registerPlaceholder(new PlayerPlaceholder("%layout-activated%", 100) {
            @Override
            public String get(TabPlayer p) {
                return !toggledOff.contains(p)+"";
            }});
    }
}
