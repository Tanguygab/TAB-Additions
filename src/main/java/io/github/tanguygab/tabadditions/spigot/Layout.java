package io.github.tanguygab.tabadditions.spigot;

import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.placeholders.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class Layout {

    protected static FileConfiguration config;
    protected static Plugin plugin;

    protected static final HashMap<Integer,PacketPlayOutPlayerInfo.PlayerInfoData> fakeplayers = new HashMap<>();
    protected static final List<Integer> placeholders = new ArrayList<>();
    protected static final Map<ConfigurationSection, List<Integer>> playersets = new HashMap<>();

    public Layout(FileConfiguration config, Plugin plugin) {
        if (!Premium.is()) return;
        Layout.config = config;
        Layout.plugin = plugin;
        create();
        refresh();
    }

    private static void refresh() {
        //placeholders
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, () -> {
            for (TabPlayer p : Shared.getPlayers()) {
                if (!placeholders.isEmpty())
                    for (Integer i : placeholders) {
                        PacketPlayOutPlayerInfo.PlayerInfoData fp = fakeplayers.get(i);

                        String text = "";
                        for (IChatBaseComponent txt : fp.displayName.getExtra())
                            text = text + txt.getText();
                        text = Shared.platform.replaceAllPlaceholders(text, p);

                        p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, new PacketPlayOutPlayerInfo.PlayerInfoData(fp.uniqueId, IChatBaseComponent.fromColoredText(text))));
                        //PacketAPI.removeScoreboardScore(p, fp.name, "TAB-YellowNumber");
                    }
            }
        }, 0L,20L);

        //player sets
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, () -> {
            for (TabPlayer p : Shared.getPlayers()) {
                if (!playersets.isEmpty())
                    for (ConfigurationSection set : playersets.keySet()) {
                        List<TabPlayer> pset = playerSet(set);
                        int inList = 0;
                        for (Integer i : playersets.get(set)) {
                            PacketPlayOutPlayerInfo.PlayerInfoData fp = fakeplayers.get(i);
                            if (pset.size() <= inList) {
                                String empty = "";
                                p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, new PacketPlayOutPlayerInfo.PlayerInfoData(fp.uniqueId,IChatBaseComponent.fromColoredText(empty))));
                                PacketAPI.removeScoreboardScore(p, fp.name, "TAB-YellowNumber");
                            }
                            else {
                                TabPlayer pInSet = pset.get(inList);


                                String format = set.getString("text","%tab_customtabname%");
                                format = Shared.platform.replaceAllPlaceholders(format, pInSet);

                                String yellownumber = Configs.config.getString("yellow-number-in-tablist","");
                                yellownumber = Shared.platform.replaceAllPlaceholders(yellownumber,pInSet);
                                int yellownumber2 = 0;
                                try {yellownumber2 = Integer.parseInt(yellownumber);}
                                catch (NumberFormatException ignored) {}

                                p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, new PacketPlayOutPlayerInfo.PlayerInfoData(fp.uniqueId,IChatBaseComponent.fromColoredText(format))));
                                PacketAPI.setScoreboardScore(p, fp.name, "TAB-YellowNumber",yellownumber2);

                                inList = inList+1;
                            }
                        }
                    }


            }
        }, 0L,20L);
    }

    private static List<TabPlayer> playerSet(ConfigurationSection slot) {
        Map<String,TabPlayer> pSorted = new TreeMap<>();
        for (TabPlayer pl : Shared.getPlayers())
            pSorted.put(pl.getTeamName(), pl);
        List<TabPlayer> players = new ArrayList<>(pSorted.values());
        if (slot.get("condition") != null)
            if (slot.getString("condition").startsWith("!"))
                players.removeIf(p -> Premium.conditions.get(slot.getString("condition").replaceFirst("!","")).isMet(p));
            else
                players.removeIf(p -> !Premium.conditions.get(slot.getString("condition")).isMet(p));
        return players;
    }

    protected static void create() {
        ConfigurationSection section = config.getConfigurationSection("layouts.Lobby");
        assert section != null;

        List<String> shape = (List<String>) section.getList("shape");
        String shape2 = "";
        assert shape != null;
        for (String line : shape) shape2 = shape2+line+" ";
        String[] shape3 = shape2.split(" ");

        int id=0;
        int col=0;
        for (int i=0;i<80;i++) {
            PacketPlayOutPlayerInfo.PlayerInfoData fp = new PacketPlayOutPlayerInfo.PlayerInfoData(UUID.randomUUID(),PacketPlayOutPlayerInfo.EnumGamemode.CREATIVE);
            ConfigurationSection slot = section.getConfigurationSection("slots").getConfigurationSection(shape3[i]);
            String text = "";
            String yellownumber = "";

            switch (slot.getString("type","text").toLowerCase()) {
                case "text":
                    text = slot.getString("text", "");
                    yellownumber = slot.getString("yellow-number", "0");
                    if (Placeholders.detectAll(text).size() > 0)
                        placeholders.add(id);
                    break;
                case "players":
                    List<Integer> ids = new ArrayList<>();
                    if (playersets.containsKey(slot))
                        ids.addAll(playersets.get(slot));
                    ids.add(id);
                    playersets.put(slot,ids);

                    break;
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

    protected static void removeAll() {
        List<PacketPlayOutPlayerInfo.PlayerInfoData> fps = new ArrayList<>(fakeplayers.values());
        for (TabPlayer p : Shared.getPlayers())
            p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, fps));
        fakeplayers.clear();
        placeholders.clear();
    }
    protected static void addAll() {
        List<PacketPlayOutPlayerInfo.PlayerInfoData> fps = new ArrayList<>(fakeplayers.values());
        for (TabPlayer p : Shared.getPlayers()) {
            p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, fps));
        }
    }
    protected static void addP(TabPlayer p) {
        List<PacketPlayOutPlayerInfo.PlayerInfoData> fps = new ArrayList<>(fakeplayers.values());
        p.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, fps));
    }
    protected static void removeP(TabPlayer p) {
        List<PacketPlayOutPlayerInfo.PlayerInfoData> fps = new ArrayList<>(fakeplayers.values());
        TABAPI.getPlayer(p.getUniqueId()).sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, fps));
    }

}
