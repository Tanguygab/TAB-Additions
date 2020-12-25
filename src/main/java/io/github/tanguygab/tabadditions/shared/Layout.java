package io.github.tanguygab.tabadditions.shared;

import io.github.tanguygab.tabadditions.spigot.NMS;
import io.github.tanguygab.tabadditions.spigot.SpigotTA;
import me.neznamy.tab.api.TabPlayer;

import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardScore;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;

import me.neznamy.tab.shared.config.Configs;
import org.bukkit.entity.Player;

import java.util.*;

public class Layout {

	private static Layout instance;
	
    protected final HashMap<Integer,PacketPlayOutPlayerInfo.PlayerInfoData> fakeplayers = new HashMap<>();
    protected final Map<Integer,Map<String,Object>> placeholders = new HashMap<>();
    protected final Map<Object, List<Integer>> playersets = new HashMap<>();
    protected Map<String,Integer> tasks = new HashMap<>();
    private static final Map<String,Object> icons = new HashMap<>();

    public Layout() {
    	instance = this;
        if (!Shared.isPremium()) return;
        create();
        refresh();
    }
    
    public static Layout getInstance() {
    	return instance;
    }

    private Object getIcon(String icon,TabPlayer p) {
        icon = Shared.platform.replaceAllPlaceholders(icon, p);
        if (icons.containsKey(icon))
            return icons.get(icon);
        String deficon = icon;
        Object skin = null;
        if (icon.startsWith("player-head:")) {
            icon = icon.replace("player-head:", "");
            if (TABAPI.getPlayer(icon) != null)
                skin = TABAPI.getPlayer(icon).getSkin();
            else if (SharedTA.platform instanceof SpigotTA)
                skin = NMS.getPropPlayer(icon);
        }
        else if (icon.startsWith("mineskin:")) {
            icon = icon.replace("mineskin:", "");
            try {
                int mineskinid = Integer.parseInt(icon);
                if (SharedTA.platform instanceof SpigotTA)
                    skin = NMS.getPropSkin(mineskinid);
            }
            catch (NumberFormatException ignored) {}
        }
        icons.put(deficon,skin);
        return skin;
    }

    private void refresh() {
        Map<TabPlayer,Map<Integer,Boolean>> skinss = new HashMap<>();
        Map<TabPlayer,Map<Integer,Boolean>> skinsp = new HashMap<>();
        //placeholders
        tasks.put("Layout-Placeholders",
        SharedTA.platform.AsyncTask(() -> {
            Map<Integer,Boolean> skins = new HashMap<>();
            for (TabPlayer p : Shared.getPlayers()) {
                if (!placeholders.isEmpty())
                    for (Integer i : placeholders.keySet()) {
                        PacketPlayOutPlayerInfo.PlayerInfoData fp = fakeplayers.get(i);
                        Map<String,Object> slot = placeholders.get(i);
                        String text = slot.get("text").toString();
                        text = Shared.platform.replaceAllPlaceholders(text, p);

                        if (!skinsp.containsKey(p)) {
                        Object skin = null;
                        if (slot.containsKey("icon"))
                            skin = getIcon(slot.get("icon").toString(),p);

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
            for (TabPlayer p : Shared.getPlayers()) {
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
                                    TABAPI.getPlayer("Tanguygab").sendMessage(IChatBaseComponent.fromColoredText(format).toString()+fp.displayName.toString(),false);
                                    if (!skinss.containsKey(p)) {
                                        Object skin = pInSet.getSkin();
                                        if (setConfig.containsKey("icon"))
                                            skin = getIcon(setConfig.get("icon").toString(),pInSet);

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
        Map<String,Object> section = SharedTA.layoutConfig.getConfigurationSection("layouts.Lobby");
        assert section != null;

        List<String> shape = (List<String>) section.get("shape");
        assert shape != null;
        String shape2 = "";
        for (String line : shape) shape2 = shape2+line+" ";
        String[] shape3 = shape2.split(" ");

        int id=0;
        int col=0;
        Map<String,Object> slots = (Map<String, Object>) section.get("slots");
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

    public void removeAll() {
        List<PacketPlayOutPlayerInfo.PlayerInfoData> fps = new ArrayList<>(fakeplayers.values());
        for (TabPlayer p : Shared.getPlayers())
            p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, fps));
    }
    public void addAll() {
        List<PacketPlayOutPlayerInfo.PlayerInfoData> fps = new ArrayList<>(fakeplayers.values());
        for (TabPlayer p : Shared.getPlayers()) {
            p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, fps));
        }
    }
    public void addP(TabPlayer p) {
        List<PacketPlayOutPlayerInfo.PlayerInfoData> fps = new ArrayList<>(fakeplayers.values());
        p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, fps));
    }
    public void removeP(TabPlayer p) {
        List<PacketPlayOutPlayerInfo.PlayerInfoData> fps = new ArrayList<>(fakeplayers.values());
       p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, fps));
    }

}
