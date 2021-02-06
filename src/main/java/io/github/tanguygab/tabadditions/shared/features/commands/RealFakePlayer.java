package io.github.tanguygab.tabadditions.shared.features.commands;

import io.github.tanguygab.tabadditions.shared.SharedTA;
import io.github.tanguygab.tabadditions.shared.features.Skins;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;

import java.util.Map;
import java.util.UUID;

public class RealFakePlayer {
    public RealFakePlayer(TabPlayer p, String[] args) {

        PacketPlayOutPlayerInfo.EnumPlayerInfoAction action = null;
        PacketPlayOutPlayerInfo.PlayerInfoData fp = new PacketPlayOutPlayerInfo.PlayerInfoData(UUID.randomUUID());
        if (SharedTA.config.getConfigurationSection("fakeplayers").containsKey(args[2])) {
            Map<String, Object> map = (Map<String, Object>) SharedTA.config.getConfigurationSection("fakeplayers").get(args[2]);
            if (map.containsKey("name"))
                fp.name = map.get("name") + "";
            if (map.containsKey("skin"))
                fp.skin = map.get("skin");
            if (map.containsKey("latency"))
                fp.latency = (int) map.get("latency");
            if (map.containsKey("uuid"))
                fp.uniqueId = UUID.fromString(map.get("uuid") + "");
        }

        if (args[1].equalsIgnoreCase("add")) {
            action = PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER;
            fp.name = args[2];
            SharedTA.config.set("fakeplayers." + args[2] + ".name", args[2]);
            SharedTA.config.set("fakeplayers." + args[2] + ".uuid", fp.uniqueId + "");
        }
        if (args[1].equalsIgnoreCase("remove")) {
            action = PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER;
            SharedTA.config.set("fakeplayers." + args[2], null);
        }
        if (args[1].equalsIgnoreCase("edit")) {
            if (args[3].equalsIgnoreCase("name")) {
                action = PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER;
                SharedTA.config.set("fakeplayers." + args[2] + ".name", args[4]);
                fp.name = args[4];
            }
            if (args[3].equalsIgnoreCase("latency")) {
                action = PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_LATENCY;
                try {
                    int num = Integer.parseInt(args[4]);
                    if (num <= 1)
                        fp.latency = 1000;
                    if (num == 2)
                        fp.latency = 600;
                    if (num == 3)
                        fp.latency = 300;
                    if (num >= 4)
                        fp.latency = 200;

                    SharedTA.config.set("fakeplayers." + args[2] + ".latency", num);
                } catch (NumberFormatException ignored) {
                }
            }
            if (args[3].equalsIgnoreCase("skin")) {
                action = PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER;
                SharedTA.config.set("fakeplayers." + args[2] + ".skin", args[4]);
                fp.skin = Skins.getIcon(args[4], p);
            }
            if (args[3].equalsIgnoreCase("group")) {
                SharedTA.config.set("fakeplayers." + args[2] + ".group", args[4]);
            }
            SharedTA.config.save();
        }

        for (TabPlayer all : TAB.getInstance().getPlayers()) {
            if (action == PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER) {
                all.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, fp));
            }
            all.sendCustomPacket(new PacketPlayOutPlayerInfo(action, fp));
        }
    }
}
