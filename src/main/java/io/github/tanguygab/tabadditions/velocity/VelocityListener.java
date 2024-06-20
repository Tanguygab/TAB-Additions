package io.github.tanguygab.tabadditions.velocity;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import io.github.tanguygab.tabadditions.shared.features.chat.Chat;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabPlayer;

public class VelocityListener {

    private final TAB tab = TAB.getInstance();

    @Subscribe
    public void onChat(PlayerChatEvent e) {
        if (!e.getResult().isAllowed()) return;
        Chat chat = tab.getFeatureManager().getFeature("Chat");
        if (chat == null || chat.isBukkitBridgeChatEnabled()) return;

        e.setResult(PlayerChatEvent.ChatResult.denied());
        chat.onChat(getPlayer(e.getPlayer()), e.getMessage());
    }

    @Subscribe
    public void onCommand(CommandExecuteEvent e) {
        if (!e.getResult().isAllowed()) return;
        Chat chat = tab.getFeatureManager().getFeature("Chat");
        if (chat == null || !(e.getCommandSource() instanceof Player sender)) return;
        TabPlayer player = getPlayer(sender);
        if (chat.onCommand(player,e.getCommand())) {
            e.setResult(CommandExecuteEvent.CommandResult.denied());
        }
    }


    @Subscribe
    public void onMessageReceived(PluginMessageEvent e) {
        if (e.getIdentifier() != VelocityPlatform.IDENTIFIER || !(e.getSource() instanceof Player receiver)) return;

        ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());
        if (!in.readUTF().equalsIgnoreCase("Chat")) return;
        Chat chat = tab.getFeatureManager().getFeature("Chat");
        if (chat != null) chat.onChat(getPlayer(receiver),in.readUTF());
    }

    private TabPlayer getPlayer(Player player) {
        return tab.getPlayer(player.getUniqueId());
    }

}
