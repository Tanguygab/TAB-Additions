package io.github.tanguygab.tabadditions.spigot;

import io.github.tanguygab.tabadditions.shared.features.chat.Chat;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class SpigotListener implements Listener {

    private final TAB tab = TAB.getInstance();

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        ((PlayerPlaceholder) tab.getPlaceholderManager().getPlaceholder("%sneak%")).updateValue(getPlayer(e),e.isSneaking());
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        if (!tab.getFeatureManager().isFeatureEnabled("Chat")) return;
        e.setCancelled(true);
        ((Chat)tab.getFeatureManager().getFeature("Chat")).onChat(getPlayer(e),e.getMessage());
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (!tab.getFeatureManager().isFeatureEnabled("Chat")) return;
        if (((Chat)tab.getFeatureManager().getFeature("Chat")).onCommand(getPlayer(e),e.getMessage()))
            e.setCancelled(true);
    }

    private TabPlayer getPlayer(PlayerEvent e) {
        return tab.getPlayer(e.getPlayer().getUniqueId());
    }
}
