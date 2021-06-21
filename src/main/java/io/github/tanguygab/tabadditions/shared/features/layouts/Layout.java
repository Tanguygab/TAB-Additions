package io.github.tanguygab.tabadditions.shared.features.layouts;

import io.github.tanguygab.tabadditions.shared.ConfigType;
import io.github.tanguygab.tabadditions.shared.PlatformType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.layouts.sorting.Sorting;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.AlignedSuffix;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardScore;
import org.bukkit.entity.Player;

import java.util.*;

public class Layout {

    private final String name;
    private final Map<String,Object> config;
    private boolean created = false;
    protected final List<TabPlayer> players = new ArrayList<>();
    protected final Map<TabPlayer,Map<Integer,Map<String,String>>> placeholdersToRefresh = new HashMap<>();

    protected final Map<Integer,PacketPlayOutPlayerInfo.PlayerInfoData> fakeplayers = new HashMap<>();
    private final Map<Integer,Map<String,Object>> placeholders = new HashMap<>();
    private final Map<Object, List<Integer>> playersets = new HashMap<>();
    private final Map<Object, List<Integer>> lists = new HashMap<>();
    private final Map<Object,Sorting> sorting = new HashMap<>();

    protected final Map<TabPlayer, Map<Integer, String>> skinsp = new HashMap<>();
    protected final Map<TabPlayer, Map<Integer, String>> skinss = new HashMap<>();
    protected final Map<TabPlayer, Map<Integer, String>> skinsl = new HashMap<>();
    protected final Map<String,Object> setsnames = new HashMap<>();
    protected final Map<String,Object> listsnames = new HashMap<>();

    protected final Map<String,String> fpnames = new HashMap<>();
    private final TABAdditions instance;

    public Layout(String name) {
        instance = TABAdditions.getInstance();
        this.name = name;
        config = instance.getConfig(ConfigType.LAYOUT).getConfigurationSection("layouts."+name);
        create();
        loadPlaceholders();
    }

    protected String getName() {
        return name;
    }
    protected String getChildLayout() {
        if (config.containsKey("if-condition-not-met"))
            return config.get("if-condition-not-met")+"";
        return "";
    }
    protected boolean isConditionMet(TabPlayer p) {
        if (!config.containsKey("condition")) return true;
        return instance.isConditionMet(config.get("condition")+"",p);
    }

    protected void loadPlaceholders() {
        Map<String,Map<String,String>> slots = ((Map<String,Map<String,String>>)config.get("slots"));
        for (String slot : slots.keySet()) {
            if (slots.get(slot).get("type").equalsIgnoreCase("PLAYERS"))
                setsnames.put(slot,slots.get(slot));
            if (slots.get(slot).get("type").equalsIgnoreCase("LIST"))
                listsnames.put(slot,slots.get(slot));
        }
    }

    protected void refreshPlaceholders(TabPlayer p) {
        if(!created) return;
        Map<Integer, String> skins = new HashMap<>();
        for (Integer i : placeholders.keySet()) {
            PacketPlayOutPlayerInfo.PlayerInfoData fp = fakeplayers.get(i);
            Map<String, Object> slot = placeholders.get(i);
            String text = slot.get("text")+"";
            text = instance.parsePlaceholders(text, p);
            if (text.contains("||")) {
                String prefixName = text.split("\\|\\|")[0];
                String suffix = "";
                if (text.split("\\|\\|").length > 1)
                    suffix = text.split("\\|\\|")[1];
                AlignedSuffix alignedSuffix = ((AlignedSuffix)TAB.getInstance().getFeatureManager().getFeature("alignedsuffix"));
                if (alignedSuffix != null)
                    text = alignedSuffix.formatName(prefixName,suffix);
            }

            String latency = slot.get("latency")+"";
            latency = instance.parsePlaceholders(latency,p);
            int lat = 0;
            try {lat = getLatency(Integer.parseInt(latency));}
            catch (Exception ignored) {latency = "NaN";}

            Object skin = null;
            String icon = "";
            if (slot.containsKey("icon")) {
                skin = instance.getSkins().getIcon(slot.get("icon")+"", p);
                icon = instance.parsePlaceholders(slot.get("icon")+"",p);
            }

            String yellownumber = "0";
            if (slot.get("yellow-number") != null) {
                yellownumber = instance.parsePlaceholders(slot.get("yellow-number")+"",p);
                try {Integer.parseInt(yellownumber);}
                catch (Exception ignored) {yellownumber = "0";}
            }

            sendPackets(skinsp,p,fp,skin,lat,text,i,icon,latency,yellownumber);
            skins.put(i, icon);
        }
        skinsp.put(p, skins);
    }
    protected void refreshLists(TabPlayer p) {
        if (!created) return;
        Map<Integer,String> skins = new HashMap<>();
        for (Object list : lists.keySet()) {
            Map<String,Object> listConfig = (Map<String,Object>) list;
            int inList = 0;
            List<String> strlist = lists(list,p);
            List<Integer> intlist = new ArrayList<>(lists.get(list));
            if (listConfig.containsKey("vertical") && (boolean)listConfig.get("vertical"))
                Collections.sort(intlist);
            for (Integer i : intlist) {
                PacketPlayOutPlayerInfo.PlayerInfoData fp = fakeplayers.get(i);

                String strInList = null;
                if (strlist.size() > inList)
                    strInList = strlist.get(inList);


                if (strlist.size() <= inList || strInList == null) {
                    String format = "";
                    Object skin = null;
                    String icon = "";
                    if (listConfig.containsKey("empty")) {
                        Map<String, String> empty = (Map<String, String>) listConfig.get("empty");
                        if (empty.containsKey("text"))
                            format = instance.parsePlaceholders(empty.get("text"), p);
                        if (empty.containsKey("icon")) {
                            skin = instance.getSkins().getIcon(empty.get("icon") + "", p);
                            icon = instance.parsePlaceholders(empty.get("icon"), p);
                        }
                        String latency = empty.get("latency");
                        latency = instance.parsePlaceholders(latency,p);
                        int lat = 0;
                        try {lat = getLatency(Integer.parseInt(latency));}
                        catch (Exception ignored) {latency = "NaN";}

                        String yellownumber = "0";
                        if (empty.get("yellow-number") != null) {
                            yellownumber = instance.parsePlaceholders(empty.get("yellow-number"),p);
                            try {Integer.parseInt(yellownumber);}
                            catch (Exception ignored) {yellownumber = "0";}
                        }

                        sendPackets(skinsl,p,fp,skin,lat,format,i,icon,latency,yellownumber);
                        skins.put(i, icon);
                    }
                } else if (intlist.size() == inList+1 && intlist.size() != strlist.size() && listConfig.containsKey("more")) {
                    String format = "";
                    Object skin = null;
                    String icon = "";
                    int num = strlist.size()-intlist.size()+1;
                    Map<String, String> more = (Map<String, String>) listConfig.get("more");
                    if (more.containsKey("text"))
                        format = instance.parsePlaceholders(more.get("text").replace("%num%",num+""), p);
                    if (more.containsKey("icon")) {
                        icon = instance.parsePlaceholders(more.get("icon").replace("%num%",num+""), p);
                        skin = instance.getSkins().getIcon(icon, p);
                    }
                    String latency = more.get("latency");
                    latency = instance.parsePlaceholders(latency.replace("%num%",num+""),p);
                    int lat = 0;
                    try {lat = getLatency(Integer.parseInt(latency));}
                    catch (Exception ignored) {latency = "NaN";}

                    String yellownumber = "0";
                    if (more.get("yellow-number") != null) {
                        yellownumber = instance.parsePlaceholders(more.get("yellow-number").replace("%num%",num+""),p);
                        try {Integer.parseInt(yellownumber);}
                        catch (Exception ignored) {yellownumber = "0";}
                    }

                    sendPackets(skinsl,p,fp,skin,lat,format,i,icon,latency,yellownumber);
                    skins.put(i, icon);
                } else {

                    String format = "%name%";
                    if (listConfig.containsKey("text")) format = listConfig.get("text")+"";
                    format = instance.parsePlaceholders(format.replace("%name%",strInList).replace("%place%",inList+1+""), p);
                    if (format.contains("||")) {
                        String prefixName = format.split("\\|\\|")[0];
                        String suffix = "";
                        if (format.split("\\|\\|").length > 1)
                            suffix = format.split("\\|\\|")[1];
                        AlignedSuffix alignedSuffix = ((AlignedSuffix)TAB.getInstance().getFeatureManager().getFeature("alignedsuffix"));
                        if (alignedSuffix != null)
                            format = alignedSuffix.formatName(prefixName,suffix);
                    }

                    String latency = listConfig.get("latency")+"";
                    latency = instance.parsePlaceholders(latency.replace("%name%",strInList).replace("%place%",inList+1+""),p);
                    int lat = 0;
                    try {lat = getLatency(Integer.parseInt(latency));}
                    catch (Exception ignored) {latency = "NaN";}

                    Object skin = null;
                    String icon = "";
                    if (listConfig.containsKey("icon")) {
                        icon = listConfig.get("icon")+"";
                        icon = instance.parsePlaceholders(icon.replace("%name%",strInList).replace("%place%",inList+1+""),p);
                        skin = instance.getSkins().getIcon(icon, p);
                    }

                    String yellownumber = "0";
                    if (listConfig.get("yellow-number") != null) {
                        yellownumber = instance.parsePlaceholders(listConfig.get("yellow-number").toString().replace("%name%",strInList).replace("%place%",inList+1+""),p);
                        try {Integer.parseInt(yellownumber);}
                        catch (Exception ignored) {yellownumber = "0";}
                    }

                    sendPackets(skinsl,p,fp,skin,lat,format,i,icon,latency,yellownumber);
                    skins.put(i,icon);

                    inList = inList + 1;
                }
            }
        }
        skinsl.put(p,skins);
    }
    protected void refreshSets(TabPlayer p) {
        if(!created) return;
        Map<Object, List<Integer>> playersets = new HashMap<>(this.playersets);
        Map<Integer,String> skins = new HashMap<>();
        for (Object set : playersets.keySet()) {
            List<TabPlayer> pset = playerSet(set,p);
            int inList = 0;
            Map<String,Object> setConfig = (Map<String,Object>) set;
            List<Integer> intlist = new ArrayList<>(playersets.get(set));
            if (setConfig.containsKey("vertical") && (boolean)setConfig.get("vertical"))
                Collections.sort(intlist);
            for (Integer i : intlist) {
                PacketPlayOutPlayerInfo.PlayerInfoData fp = fakeplayers.get(i);

                TabPlayer pInSet = null;
                if (pset.size() > inList)
                    pInSet = pset.get(inList);

                if (pset.size() <= inList || pInSet == null) {
                    String format = "";
                    Object skin = null;
                    String icon = "";
                    if (setConfig.containsKey("empty")) {
                        Map<String, String> empty = (Map<String, String>) setConfig.get("empty");
                        if (empty.containsKey("text"))
                            format = instance.parsePlaceholders(empty.get("text"), p);
                        if (empty.containsKey("icon")) {
                            icon = instance.parsePlaceholders(empty.get("icon"), p);
                            skin = instance.getSkins().getIcon(icon + "", p);
                        }
                        String latency = empty.get("latency")+"";
                        latency = instance.parsePlaceholders(latency,p);
                        int lat = 0;
                        try {lat = getLatency(Integer.parseInt(latency));}
                        catch (Exception ignored) {latency = "NaN";}

                        String yellownumber = "0";
                        if (empty.get("yellow-number") != null) {
                            yellownumber = instance.parsePlaceholders(empty.get("yellow-number"),p);
                            try {Integer.parseInt(yellownumber);}
                            catch (Exception ignored) {yellownumber = "0";}
                        }

                        sendPackets(skinss,p,fp,skin,lat,format,i,icon,latency,yellownumber);
                        skins.put(i, icon);
                    }
                } else if (intlist.size() == inList+1 && pset.size() != intlist.size() && setConfig.containsKey("more")) {
                    String format = "";
                    Object skin = null;
                    String icon = "";
                    int num = pset.size()-intlist.size()+1;
                    Map<String, String> more = (Map<String, String>) setConfig.get("more");
                    if (more.containsKey("text"))
                        format = instance.parsePlaceholders(more.get("text").replace("%num%",num+""), p);
                    if (more.containsKey("icon")) {
                        icon = instance.parsePlaceholders(more.get("icon").replace("%num%",num+""), p);
                        skin = instance.getSkins().getIcon(icon, p);
                    }
                    String latency = setConfig.get("latency")+"";
                    latency = instance.parsePlaceholders(latency.replace("%num%",num+""),p);
                    int lat = 0;
                    try {lat = getLatency(Integer.parseInt(latency));}
                    catch (Exception ignored) {
                        latency = "NaN";
                    }

                    String yellownumber = "0";
                    if (more.get("yellow-number") != null) {
                        yellownumber = instance.parsePlaceholders(more.get("yellow-number").replace("%num%",num+""),p);
                        try {Integer.parseInt(yellownumber);}
                        catch (Exception ignored) {yellownumber = "0";}
                    }

                    sendPackets(skinss,p,fp,skin,lat,format,i,icon,latency,yellownumber);
                    skins.put(i, icon);
                } else {
                    String format = "%player%";
                    if (setConfig.containsKey("text"))
                        format = (setConfig.get("text") + "").replace("%place%", inList + 1 + "");
                    format = instance.parsePlaceholders(format, pInSet, p, pInSet);
                    if (format.contains("||")) {
                        String prefixName = format.split("\\|\\|")[0];
                        String suffix = "";
                        if (format.split("\\|\\|").length > 1)
                            suffix = format.split("\\|\\|")[1];
                        AlignedSuffix alignedSuffix = (AlignedSuffix) TAB.getInstance().getFeatureManager().getFeature("alignedsuffix");
                        if (alignedSuffix != null)
                            format = alignedSuffix.formatName(prefixName, suffix);
                    }

                    String latency = setConfig.get("latency") + "";
                    latency = instance.parsePlaceholders(latency.replace("%place%", inList + 1 + ""), pInSet, p, pInSet);
                    int lat;
                    try {
                        lat = getLatency(Integer.parseInt(latency));
                    } catch (Exception ignored) {
                        latency = "NaN";
                        lat = Math.toIntExact(pInSet.getPing());
                    }

                    String yellownumber;
                    if (setConfig.containsKey("yellow-number")) {
                        yellownumber = instance.parsePlaceholders(setConfig.get("yellow-number").toString().replace("%place%", inList+1+""), pInSet, p, pInSet);
                        try {Integer.parseInt(yellownumber);}
                        catch (Exception ignored) {yellownumber = "0";}
                    } else {
                        yellownumber = instance.parsePlaceholders(TAB.getInstance().getConfiguration().config.getString("yellow-number-in-tablist","").replace("%place%",inList+1+""), pInSet, p, pInSet);
                        try {Integer.parseInt(yellownumber);}
                        catch (Exception ignored) {yellownumber = "0";}
                    }

                    Object skin = pInSet.getSkin();
                    String icon = "player-head:"+pInSet.getName();
                    if (setConfig.containsKey("icon")) {
                        icon = instance.parsePlaceholders((setConfig.get("icon")+"").replace("%place%",inList+1+""),pInSet,p,pInSet);
                        skin = instance.getSkins().getIcon(icon, pInSet);
                    }

                    sendPackets(skinss,p,fp,skin,lat,format,i,icon,latency,yellownumber);
                    skins.put(i,icon);

                    inList = inList + 1;
                }
            }
        }
        skinss.put(p,skins);
    }

    protected List<String> lists(Object slot, TabPlayer p) {
        Map<String,String> section = (Map<String, String>) slot;

        String input = "";
        if (section.containsKey("input"))
            input = section.get("input");
        String separator = " ";
        if (section.containsKey("separator"))
            separator = section.get("separator");
        input = instance.parsePlaceholders(input, p);

        return new ArrayList<>(Arrays.asList(input.split(separator)));
    }
    protected List<TabPlayer> playerSet(Object slot, TabPlayer viewer) {
        Map<String,Object> section = (Map<String, Object>) slot;

        List<TabPlayer> list = new ArrayList<>(TAB.getInstance().getPlayers());
        if (section.get("condition") != null && !section.get("condition").toString().equals("")) {
            String cond = section.get("condition")+"";
            list.removeIf(p -> !instance.isConditionMet( instance.parsePlaceholders(cond, p, viewer,p), p));
        }
        if (section.get("vanished") == null || !Boolean.parseBoolean(""+section.get("vanished"))) {
            if (instance.getPlatform().getType() == PlatformType.SPIGOT)
                list.removeIf(p -> !viewer.hasPermission("tab.seevanished") && !((Player) viewer.getPlayer()).canSee(((Player) p.getPlayer())));
            else list.removeIf(p -> !viewer.hasPermission("tab.seevanished") && p.isVanished());
        }

        if (!section.containsKey("sorting")) {
            Map<String,TabPlayer> pSorted = new TreeMap<>();
            for (TabPlayer p : list)
                if (p != null && p.getTeamName() != null)
                    pSorted.put(p.getTeamName(), p);
            return new ArrayList<>(pSorted.values());
        }



        Sorting sort;
        if (sorting.containsKey(slot)) sort = sorting.get(slot);
        else sort = new Sorting((Map<String, Object>) section.get("sorting"), list, getName());
        Map<String,TabPlayer> pSorted = new TreeMap<>();
        for (TabPlayer p : list)
            if (p != null && p.getTeamName() != null)
                pSorted.put(sort.getPosition(p) + p.getName(), p);
        sorting.put(slot,sort);
        return new ArrayList<>(pSorted.values());
    }

    public void sendPackets(Map<TabPlayer, Map<Integer, String>> skins, TabPlayer p, PacketPlayOutPlayerInfo.PlayerInfoData fp, Object skin, int lat, String text, int i, String icon, String latency, String yellownumber) {
        if (!placeholdersToRefresh.containsKey(p) || !placeholdersToRefresh.get(p).containsKey(i))
            return;

        if (!skins.containsKey(p) || (skin != null && !icon.equals(skins.get(p).get(i)))) {
            p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, new PacketPlayOutPlayerInfo.PlayerInfoData(fp.uniqueId)));
            p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, new PacketPlayOutPlayerInfo.PlayerInfoData(fp.name, fp.uniqueId, skin, lat, PacketPlayOutPlayerInfo.EnumGamemode.CREATIVE, IChatBaseComponent.fromColoredText(text))));
        } else if (!placeholdersToRefresh.get(p).get(i).get("text").equals(text))
            p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, new PacketPlayOutPlayerInfo.PlayerInfoData(fp.uniqueId, IChatBaseComponent.fromColoredText(text))));
        placeholdersToRefresh.get(p).get(i).put("text",text);

        if (!placeholdersToRefresh.get(p).get(i).get("latency").equals(latency))
            p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_LATENCY, new PacketPlayOutPlayerInfo.PlayerInfoData(fp.uniqueId,lat)));
        placeholdersToRefresh.get(p).get(i).put("latency",latency);

        if (!placeholdersToRefresh.get(p).get(i).get("yellow-number").equals(yellownumber))
            p.sendCustomPacket(new PacketPlayOutScoreboardScore(PacketPlayOutScoreboardScore.Action.CHANGE, "TAB-YellowNumber", fp.name, Integer.parseInt(yellownumber)));
        placeholdersToRefresh.get(p).get(i).put("yellow-number",yellownumber);
    }
    private int getLatency(int lat) {
        boolean realLat = false;
        if (instance.getConfig(ConfigType.MAIN).hasConfigOption("real-latency"))
            realLat = instance.getConfig(ConfigType.MAIN).getBoolean("real-latency");

        if (realLat) return lat;
        else if (lat <= 1) return 1000;
        else if (lat == 2) return 600;
        else if (lat == 3) return  300;
        else if (lat == 4) return  200;
        else return 100;
    }

    protected void create() {
        if (config == null) return;

        List<String> shape = (List<String>) config.get("shape");
        if (shape == null) return;
        String shape2 = "";
        for (String line : shape) shape2 = shape2+line+" ";
        String[] shape3 = shape2.split(" ");

        int id=0;
        int col=0;
        Map<String,Object> slots = (Map<String, Object>) config.get("slots");
        for (int i=0;i<shape3.length;i++) {
            PacketPlayOutPlayerInfo.PlayerInfoData fp = new PacketPlayOutPlayerInfo.PlayerInfoData(UUID.randomUUID(),PacketPlayOutPlayerInfo.EnumGamemode.CREATIVE);
            Map<String,Object> slot = (Map<String, Object>) slots.get(shape3[i]);
            String text = "";

            if (slot != null && !slot.isEmpty()) {
                if ("players".equalsIgnoreCase(slot.get("type") + "")) {
                    List<Integer> ids = new ArrayList<>();
                    if (playersets.containsKey(slot))
                        ids.addAll(playersets.get(slot));
                    ids.add(id);
                    playersets.put(slot, ids);
                } else if ("list".equalsIgnoreCase(slot.get("type")+"")) {
                    List<Integer> ids = new ArrayList<>();
                    if (lists.containsKey(slot))
                        ids.addAll(lists.get(slot));
                    ids.add(id);
                    lists.put(slot,ids);
                } else {
                    text = slot.get("text") + "";
                    if (text.contains("||")) {
                        String prefixName = text.split("\\|\\|")[0];
                        String suffix = text.split("\\|\\|")[1];
                        AlignedSuffix alignedSuffix = ((AlignedSuffix) TAB.getInstance().getFeatureManager().getFeature("alignedsuffix"));
                        if (alignedSuffix != null)
                            text = alignedSuffix.formatName(prefixName, suffix);
                    }
                    String latency = slot.get("latency")+"";

                    if (TAB.getInstance().getPlaceholderManager().detectAll(text).size() > 0 || slot.containsKey("icon"))
                        placeholders.put(id, slot);
                    else if (TAB.getInstance().getPlaceholderManager().detectAll(latency).size() > 0)
                        placeholders.put(id, slot);
                    else {
                        try {
                            fp.latency = getLatency(Integer.parseInt(latency));
                        } catch (Exception ignored) {}
                    }
                }
            }

            String id2 = id+"";
            if (id < 10) id2="0"+id;
            List<String> chars = LayoutManager.getInstance().chars;
            if (chars.size() > id)
                fp.name = chars.get(id);
            else fp.name = chars.get(chars.size()-1)+id2;
            fpnames.put(id2,fp.name);
            fp.displayName = IChatBaseComponent.fromColoredText(text);
            fakeplayers.put(id,fp);

            id = id + 20;
            if (id >= 80) {
                col = col + 1;
                id = col;
            }
        }
        created = true;
    }

}
