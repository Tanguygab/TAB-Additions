package io.github.tanguygab.tabadditions.bungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import io.github.tanguygab.tabadditions.shared.features.chat.Chat;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class BungeeListener implements Listener {

    private final TAB tab = TAB.getInstance();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(ChatEvent e) {
        if (e.isCancelled()) return;
        Chat chat = tab.getFeatureManager().getFeature("Chat");
        if (chat == null) return;
        TabPlayer player = getPlayer(e.getSender());
        if (e.isCommand() && chat.onCommand(player,e.getMessage())) {
            e.setCancelled(true);
            return;
        }
        if (chat.isBukkitBridgeChatEnabled()) return;
        e.setCancelled(true);
        chat.onChat(player,e.getMessage());
    }

    @EventHandler
    public void onMessageReceived(PluginMessageEvent e) {
        if (!e.getTag().equalsIgnoreCase("tabadditions:channel")) return;
        ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());
        if (!in.readUTF().equalsIgnoreCase("Chat")) return;
        Chat chat = tab.getFeatureManager().getFeature("Chat");
        if (chat != null) chat.onChat(getPlayer(e.getReceiver()),in.readUTF());
    }

    private TabPlayer getPlayer(Connection player) {
        return tab.getPlayer(((ProxiedPlayer) player).getUniqueId());
    }

}
