package io.github.tanguygab.tabadditions.spigot;

import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.shared.TAB;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class SpigotListener implements Listener {

    private final TAB tab;
    public SpigotListener() {
        tab = TAB.getInstance();
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        ((PlayerPlaceholder) tab.getPlaceholderManager().getPlaceholder("%sneak%")).updateValue(tab.getPlayer(e.getPlayer().getUniqueId()),e.isSneaking());
    }
}
