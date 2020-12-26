package io.github.tanguygab.tabadditions.shared.layouts;

import io.github.tanguygab.tabadditions.shared.SharedTA;
import io.github.tanguygab.tabadditions.spigot.SpigotTA;
import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardScore;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import org.bukkit.entity.Player;

import java.util.*;

public class Layout {

    private final String name;
    private final Map<String,Object> config;
    protected final List<TabPlayer> players = new ArrayList<>();

    protected final Map<Integer,PacketPlayOutPlayerInfo.PlayerInfoData> fakeplayers = new HashMap<>();
    protected final Map<Integer,Map<String,Object>> placeholders = new HashMap<>();
    protected final Map<Object, List<Integer>> playersets = new HashMap<>();
    protected Map<String,Integer> tasks = new HashMap<>();

    public Layout(String name) {
        this.name = name;
        config = SharedTA.layoutConfig.getConfigurationSection("layouts."+name);
        if (!Shared.isPremium()) return;
        create();
        refresh();
    }

    protected String getName() {
        return name;
    }
    protected String getChildLayout() {
        return config.get("if-condition-not-met").toString();
    }
    protected boolean isConditionMet(TabPlayer p) {
        Object conditionname = config.get("condition");
        if (conditionname == null) return true;
        Condition condition = Condition.getCondition(conditionname.toString());
        if (condition == null) return true;
        return condition.isMet(p);
    }

    private void refresh() {
        Map<TabPlayer,Map<Integer,Boolean>> skinss = new HashMap<>();
        Map<TabPlayer,Map<Integer,Boolean>> skinsp = new HashMap<>();
        //placeholders
        tasks.put("Layout-Placeholders",
                SharedTA.platform.AsyncTask(() -> {
                    Map<Integer,Boolean> skins = new HashMap<>();
                    for (TabPlayer p : players) {
                        if (!placeholders.isEmpty())
                            for (Integer i : placeholders.keySet()) {
                                PacketPlayOutPlayerInfo.PlayerInfoData fp = fakeplayers.get(i);
                                Map<String,Object> slot = placeholders.get(i);
                                String text = slot.get("text").toString();
                                text = Shared.platform.replaceAllPlaceholders(text, p);

                                if (!skinsp.containsKey(p)) {
                                    Object skin = null;
                                    if (slot.containsKey("icon"))
                                        skin = LayoutManager.getInstance().getIcon(slot.get("icon").toString(),p);

                                    p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, new PacketPlayOutPlayerInfo.PlayerInfoData(fp.uniqueId)));
                                    p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, new PacketPlayOutPlayerInfo.PlayerInfoData(fp.name, fp.uniqueId, skin, 0, PacketPlayOutPlayerInfo.EnumGamemode.CREATIVE, IChatBaseComponent.fromColoredText(text))));
                                    skins.put(i,true);
                                }
                                else
                                    p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, new PacketPlayOutPlayerInfo.PlayerInfoData(fp.uniqueId, IChatBaseComponent.fromColoredText(text))));
                                p.sendCustomPacket(new PacketPlayOutScoreboardScore(PacketPlayOutScoreboardScore.Action.CHANGE, "TAB-YellowNumber", fp.name, 0));
                            }
                        skinsp.put(p,skins);
                    }
                },0L,10L));

        //player sets
        tasks.put("Layout-PlayerSets",
                SharedTA.platform.AsyncTask(() -> {
                    Map<Integer,Boolean> skins = new HashMap<>();
                    for (TabPlayer p : players) {
                        if (!playersets.isEmpty())
                            for (Object set : playersets.keySet()) {
                                List<TabPlayer> pset = playerSet(set);
                                int inList = 0;
                                Map<String,Object> setConfig = (Map<String,Object>) set;
                                if (setConfig.containsKey("vertical") && (boolean)setConfig.get("vertical"))
                                    Collections.sort(playersets.get(set));
                                for (Integer i : playersets.get(set)) {
                                    PacketPlayOutPlayerInfo.PlayerInfoData fp = fakeplayers.get(i);
                                    if (pset.size() <= inList) {
                                        String empty = "";
                                        p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, new PacketPlayOutPlayerInfo.PlayerInfoData(fp.uniqueId)));
                                        p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, new PacketPlayOutPlayerInfo.PlayerInfoData(fp.name,fp.uniqueId,fp.skin, fp.latency, fp.gameMode,IChatBaseComponent.fromColoredText(empty))));
                                        p.sendCustomPacket(new PacketPlayOutScoreboardScore(PacketPlayOutScoreboardScore.Action.CHANGE, "TAB-YellowNumber", fp.name, 0));
                                    } else {
                                        TabPlayer pInSet = pset.get(inList);

                                        boolean vanished = false;
                                        if (SharedTA.platform instanceof SpigotTA && !((Player)p.getPlayer()).canSee(((Player)pInSet.getPlayer())))
                                            vanished = true;
                                        else if (p.isVanished())
                                            vanished = true;

                                        if (vanished) {
                                            p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, new PacketPlayOutPlayerInfo.PlayerInfoData(fp.uniqueId)));
                                            p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, new PacketPlayOutPlayerInfo.PlayerInfoData(fp.name, fp.uniqueId, null, 0, PacketPlayOutPlayerInfo.EnumGamemode.CREATIVE, IChatBaseComponent.fromString(""))));
                                            p.sendCustomPacket(new PacketPlayOutScoreboardScore(PacketPlayOutScoreboardScore.Action.CHANGE, "TAB-YellowNumber", fp.name, 0));
                                        }
                                        else {

                                            String format = "%player%";
                                            if (setConfig.containsKey("text")) format = setConfig.get("text").toString();
                                            format = Shared.platform.replaceAllPlaceholders(format, pInSet);

                                            String yellownumber = Configs.config.getString("yellow-number-in-tablist", "");
                                            yellownumber = Shared.platform.replaceAllPlaceholders(yellownumber, pInSet);
                                            int yellownumber2 = 0;
                                            try {yellownumber2 = Integer.parseInt(yellownumber);}
                                            catch (NumberFormatException ignored) {}

                                            if (!skinss.containsKey(p)) {
                                                Object skin = pInSet.getSkin();
                                                if (setConfig.containsKey("icon"))
                                                    skin = LayoutManager.getInstance().getIcon(setConfig.get("icon").toString(),pInSet);

                                                p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, new PacketPlayOutPlayerInfo.PlayerInfoData(fp.uniqueId)));
                                                p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, new PacketPlayOutPlayerInfo.PlayerInfoData(fp.name, fp.uniqueId, skin, 0, PacketPlayOutPlayerInfo.EnumGamemode.CREATIVE, IChatBaseComponent.fromColoredText(format))));
                                                skins.put(i,true);
                                            }
                                            else
                                                p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, new PacketPlayOutPlayerInfo.PlayerInfoData(fp.uniqueId, IChatBaseComponent.fromColoredText(format))));
                                            p.sendCustomPacket(new PacketPlayOutScoreboardScore(PacketPlayOutScoreboardScore.Action.CHANGE, "TAB-YellowNumber", fp.name, yellownumber2));


                                            inList = inList + 1;
                                        }
                                    }
                                }
                            }
                        skinss.put(p,skins);
                    }
                },0L,5L));
    }

    private List<TabPlayer> playerSet(Object slot) {
        Map<String,TabPlayer> pSorted = new TreeMap<>();
        for (TabPlayer pl : Shared.getPlayers())
            pSorted.put(pl.getTeamName(), pl);
        List<TabPlayer> players = new ArrayList<>(pSorted.values());
        String condition = ((Map<String,String>)slot).get("condition");
        if (condition != null && !condition.equals(""))
            if (condition.startsWith("!")) {
                Condition cond = Condition.getCondition(condition.replaceFirst("!",""));
                if (cond != null)
                    players.removeIf(cond::isMet);
            }
            else {
                Condition cond = Condition.getCondition(condition);
                if (cond != null)
                    players.removeIf(p -> !cond.isMet(p));
            }
        return players;
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
        for (int i=0;i<80;i++) {
            PacketPlayOutPlayerInfo.PlayerInfoData fp = new PacketPlayOutPlayerInfo.PlayerInfoData(UUID.randomUUID(),PacketPlayOutPlayerInfo.EnumGamemode.CREATIVE);
            Map<String,Object> slot = (Map<String, Object>) slots.get(shape3[i]);
            String text = "";

            if (!slot.isEmpty()) {
                if ("players".equals(slot.get("type").toString().toLowerCase())) {
                    List<Integer> ids = new ArrayList<>();
                    if (playersets.containsKey(slot))
                        ids.addAll(playersets.get(slot));
                    ids.add(id);
                    playersets.put(slot, ids);
                } else {
                    text = slot.get("text").toString();
                    if (PlaceholderManager.detectAll(text).size() > 0 || slot.containsKey("icon"))
                        placeholders.put(id,slot);
                }
            }

            String id2 = id+"";
            if (id < 10) id2="0"+id;
            fp.name = id2+"FAKEPLAYER";
            fp.displayName = IChatBaseComponent.fromColoredText(text);
            fakeplayers.put(id,fp);

            id = id + 20;
            if (id >= 80) {
                col = col + 1;
                id = col;
            }
        }
    }

}
