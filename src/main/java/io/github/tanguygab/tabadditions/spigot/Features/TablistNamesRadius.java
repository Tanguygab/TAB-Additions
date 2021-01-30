package io.github.tanguygab.tabadditions.spigot.Features;

import io.github.tanguygab.tabadditions.shared.SharedTA;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TablistNamesRadius {

    public int load() {

        return Bukkit.getScheduler().scheduleAsyncRepeatingTask((Plugin) SharedTA.plugin, () -> {
            int zone = (int) Math.pow(SharedTA.tablistNamesRadius, 2);
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {

                    if (p != player && p.getWorld().equals(player.getWorld()) && player.getLocation().distanceSquared(p.getLocation()) < zone) {
                        TabPlayer p2 = TAB.getInstance().getPlayer(player.getUniqueId());
                        TAB.getInstance().getPlayer(p.getUniqueId()).sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, new PacketPlayOutPlayerInfo.PlayerInfoData(p2.getName(), p2.getTablistUUID(), p2.getSkin(), (int)p2.getPing(), PacketPlayOutPlayerInfo.EnumGamemode.CREATIVE, null)));
                    }
                    else if (p != player) {
                        TAB.getInstance().getPlayer(p.getUniqueId()).sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, new PacketPlayOutPlayerInfo.PlayerInfoData(player.getUniqueId())));
                    }
                }
            }
        }, 0L, 10L);
    }
}
