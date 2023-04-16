package io.github.tanguygab.tabadditions.spigot;

import io.github.tanguygab.tabadditions.shared.features.chat.ChatManager;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class SpigotListener implements Listener {

    private final TAB tab;
    public SpigotListener() {
        tab = TAB.getInstance();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        if (!tab.getFeatureManager().isFeatureEnabled("Chat")) return;
        e.setCancelled(true);
        ((ChatManager)tab.getFeatureManager().getFeature("Chat")).onChat(getPlayer(e),e.getMessage());
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        ((PlayerPlaceholder) tab.getPlaceholderManager().getPlaceholder("%sneak%")).updateValue(getPlayer(e),e.isSneaking());
    }

    private TabPlayer getPlayer(PlayerEvent e) {
        return tab.getPlayer(e.getPlayer().getUniqueId());
    }

}
