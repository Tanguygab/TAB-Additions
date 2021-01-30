package io.github.tanguygab.tabadditions.shared.features.layouts;

import io.github.tanguygab.tabadditions.shared.SharedTA;
import io.github.tanguygab.tabadditions.shared.features.layouts.sorting.Sorting;
import io.github.tanguygab.tabadditions.spigot.SpigotTA;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.AlignedSuffix;
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
    protected final Map<Object,Sorting> sorting = new HashMap<>();

    Map<TabPlayer, Map<Integer, Object>> skinsp = new HashMap<>();
    Map<TabPlayer, Map<Integer, Object>> skinss = new HashMap<>();

    public Layout(String name) {
        this.name = name;
        config = SharedTA.layoutConfig.getConfigurationSection("layouts."+name);
        create();
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

    protected void refreshPlaceholders() {
        Map<Integer, Object> skins = new HashMap<>();
        for (TabPlayer p : players) {
            if (!placeholders.isEmpty())
                for (Integer i : placeholders.keySet()) {
                    PacketPlayOutPlayerInfo.PlayerInfoData fp = fakeplayers.get(i);
                    Map<String, Object> slot = placeholders.get(i);
                    String text = slot.get("text")+"";
                    text = TAB.getInstance().getPlatform().replaceAllPlaceholders(text, p);
                    if (text.contains("||")) {
                        String prefixName = text.split("\\|\\|")[0];
                        String suffix = text.split("\\|\\|")[1];
                        text = ((AlignedSuffix)TAB.getInstance().getFeatureManager().getFeature("alignedsuffix")).fixTextWidth(null, prefixName,suffix);
                    }

                    Object skin = null;
                    if (slot.containsKey("icon"))
                        skin = LayoutManager.getInstance().getIcon(slot.get("icon")+"", p);
                    if (!skinsp.containsKey(p) || (skin != null && skinsp.get(p).get(i) != skin)) {
                        p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, new PacketPlayOutPlayerInfo.PlayerInfoData(fp.uniqueId)));
                        p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, new PacketPlayOutPlayerInfo.PlayerInfoData(fp.name, fp.uniqueId, skin, 0, PacketPlayOutPlayerInfo.EnumGamemode.CREATIVE, IChatBaseComponent.fromColoredText(text))));
                    } else
                        p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, new PacketPlayOutPlayerInfo.PlayerInfoData(fp.uniqueId, IChatBaseComponent.fromColoredText(text))));
                    skins.put(i, skin);
                    p.sendCustomPacket(new PacketPlayOutScoreboardScore(PacketPlayOutScoreboardScore.Action.CHANGE, "TAB-YellowNumber", fp.name, 0));
                }
            skinsp.put(p, skins);
        }
    }

    protected void refreshSets() {
        Map<Integer,Object> skins = new HashMap<>();
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

                        TabPlayer pInSet = null;
                        if (pset.size() > inList)
                            pInSet = pset.get(inList);
                        boolean vanished = false;
                        if (pInSet != null) {
                            if (setConfig.get("vanished") != null && !(boolean) setConfig.get("vanished")) {
                                if (SharedTA.platform.type().equals("Spigot") && !((Player) p.getPlayer()).canSee(((Player) pInSet.getPlayer())))
                                    vanished = true;
                                else vanished = p.isVanished();
                            }
                        }

                        if (pset.size() <= inList || vanished || pInSet == null) {
                            String format = "";
                            Object skin = null;
                            if (setConfig.containsKey("empty")) {
                                Map<String,String> empty = (Map<String, String>) setConfig.get("empty");
                                if (empty.containsKey("text"))
                                    format = TAB.getInstance().getPlatform().replaceAllPlaceholders(empty.get("text"), p);
                                if (empty.containsKey("icon"))
                                    skin = LayoutManager.getInstance().getIcon(empty.get("icon")+"",p);
                            }
                            if (!skinss.containsKey(p) || (skinss.get(p).get(i) != skin)) {
                                p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, new PacketPlayOutPlayerInfo.PlayerInfoData(fp.uniqueId)));
                                p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, new PacketPlayOutPlayerInfo.PlayerInfoData(fp.name, fp.uniqueId, skin, 0, PacketPlayOutPlayerInfo.EnumGamemode.CREATIVE, IChatBaseComponent.fromColoredText(format))));
                            } else
                                p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, new PacketPlayOutPlayerInfo.PlayerInfoData(fp.uniqueId, IChatBaseComponent.fromColoredText(format))));
                            skins.put(i,skin);
                            p.sendCustomPacket(new PacketPlayOutScoreboardScore(PacketPlayOutScoreboardScore.Action.CHANGE, "TAB-YellowNumber", fp.name, 0));
                        } else {
                            String format = "%player%";
                            if (setConfig.containsKey("text")) format = setConfig.get("text")+"";
                            format = TAB.getInstance().getPlatform().replaceAllPlaceholders(format, pInSet);
                            if (format.contains("||")) {
                                String prefixName = format.split("\\|\\|")[0];
                                String suffix = format.split("\\|\\|")[1];
                                format = ((AlignedSuffix)TAB.getInstance().getFeatureManager().getFeature("alignedsuffix")).fixTextWidth(pInSet, prefixName,suffix);
                            }

                            String yellownumber = TAB.getInstance().getConfiguration().config.getString("yellow-number-in-tablist", "");
                            yellownumber = TAB.getInstance().getPlatform().replaceAllPlaceholders(yellownumber, pInSet);
                            int yellownumber2 = 0;
                            try {yellownumber2 = Integer.parseInt(yellownumber);}
                            catch (NumberFormatException ignored) {}

                            Object skin = pInSet.getSkin();
                            if (setConfig.containsKey("icon"))
                                skin = LayoutManager.getInstance().getIcon(setConfig.get("icon")+"",pInSet);
                            if (!skinss.containsKey(p) || (skin != null && skinss.get(p).get(i) != skin)) {
                                p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, new PacketPlayOutPlayerInfo.PlayerInfoData(fp.uniqueId)));
                                p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, new PacketPlayOutPlayerInfo.PlayerInfoData(fp.name, fp.uniqueId, skin, 0, PacketPlayOutPlayerInfo.EnumGamemode.CREATIVE, IChatBaseComponent.fromColoredText(format))));
                            }
                            else
                                p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, new PacketPlayOutPlayerInfo.PlayerInfoData(fp.uniqueId, IChatBaseComponent.fromColoredText(format))));
                            skins.put(i,skin);
                            p.sendCustomPacket(new PacketPlayOutScoreboardScore(PacketPlayOutScoreboardScore.Action.CHANGE, "TAB-YellowNumber", fp.name, yellownumber2));


                            inList = inList + 1;
                        }
                    }
                }
            skinss.put(p,skins);
        }
    }


    private List<TabPlayer> playerSet(Object slot) {
        Map<String,Object> section = (Map<String, Object>) slot;

        List<TabPlayer> list = new ArrayList<>(TAB.getInstance().getPlayers());
        String condition = section.get("condition").toString();
        if (condition != null && !condition.equals("")) {
            if (condition.startsWith("!")) {
                Condition cond = Condition.getCondition(condition.replaceFirst("!", ""));
                if (cond != null)
                    list.removeIf(cond::isMet);
            } else {
                Condition cond = Condition.getCondition(condition);
                if (cond != null)
                    list.removeIf(p -> !cond.isMet(p));
            }
        }

        if (!section.containsKey("sorting")) {
            Map<String,TabPlayer> pSorted = new TreeMap<>();
            for (TabPlayer p : list)
                pSorted.put(p.getTeamName(), p);
            return new ArrayList<>(pSorted.values());
        }



        Sorting sort;
        if (sorting.containsKey(slot)) sort = sorting.get(slot);
        else sort = new Sorting((Map<String, Object>) section.get("sorting"), list, getName());
        Map<String,TabPlayer> pSorted = new TreeMap<>();
        for (TabPlayer p : list)
            pSorted.put(sort.getPosition(p) + p.getName(), p);
        sorting.put(slot,sort);
        return new ArrayList<>(pSorted.values());
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
                if ("players".equalsIgnoreCase(slot.get("type")+"")) {
                    List<Integer> ids = new ArrayList<>();
                    if (playersets.containsKey(slot))
                        ids.addAll(playersets.get(slot));
                    ids.add(id);
                    playersets.put(slot, ids);
                } else {
                    text = slot.get("text")+"";
                    if (text.contains("||")) {
                        String prefixName = text.split("\\|\\|")[0];
                        String suffix = text.split("\\|\\|")[1];
                        text = ((AlignedSuffix)TAB.getInstance().getFeatureManager().getFeature("alignedsuffix")).fixTextWidth(null, prefixName,suffix);
                    }
                    if (TAB.getInstance().getPlaceholderManager().detectAll(text).size() > 0 || slot.containsKey("icon"))
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
