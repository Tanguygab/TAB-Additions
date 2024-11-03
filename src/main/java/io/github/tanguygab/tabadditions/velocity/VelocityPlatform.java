package io.github.tanguygab.tabadditions.velocity;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.simplix.protocolize.api.Protocolize;
import dev.simplix.protocolize.api.inventory.PlayerInventory;
import dev.simplix.protocolize.api.item.BaseItemStack;
import dev.simplix.protocolize.api.player.ProtocolizePlayer;
import io.github.tanguygab.tabadditions.shared.Platform;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatItem;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import java.time.Duration;
import java.util.List;

public class VelocityPlatform extends Platform {

	public static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from("tabadditions:channel");

	private final TABAdditionsVelocity plugin;
	private VelocityListener listener;

	public VelocityPlatform(TABAdditionsVelocity plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean isProxy() {
		return true;
	}

    @Override
	public void registerPlaceholders(PlaceholderManager pm) {
		for (RegisteredServer server : plugin.server.getAllServers())
			pm.registerServerPlaceholder("%server-status:" + server.getServerInfo().getName() + "%",10000,()->plugin.getServerStatus(server));
	}

	@Override
	public void registerCommand(String cmd, String... aliases) {
		BrigadierCommand command = new BrigadierCommand(BrigadierCommand.literalArgumentBuilder(cmd));
		CommandManager cmds = plugin.server.getCommandManager();
		cmds.register(cmds.metaBuilder(command).aliases(aliases).plugin(this).build(), command);
	}

	@Override
	public boolean isPluginEnabled(String plugin) {
		return this.plugin.server.getPluginManager().getPlugin(plugin).isPresent();
	}

	@Override
	public void sendTitle(TabPlayer p, String title, String subtitle, int fadeIn, int stay, int fadeout) {
		Title t = Title.title(
				Component.text(title),
				Component.text(subtitle),
				Title.Times.times(Duration.ofSeconds(fadeIn), Duration.ofSeconds(stay), Duration.ofSeconds(fadeout))
		);
		((Player)p.getPlayer()).showTitle(t);
	}

	@Override
	public void sendActionbar(TabPlayer p, String text) {
		((Player)p.getPlayer()).sendActionBar(Component.text(text));
	}

	@Override
	public void reload() {
		if (listener != null) plugin.server.getEventManager().unregisterListener(plugin, listener);
		plugin.server.getEventManager().register(plugin, listener = new VelocityListener());
	}

	@Override
	public void disable() {
		plugin.server.getEventManager().unregisterListener(plugin, listener);
	}

	@Override
	public void runTask(Runnable run) {
		plugin.server.getScheduler().buildTask(plugin,run).schedule();
	}

	@Override
	public Audience audience(TabPlayer player) {
		return player == null ? plugin.server.getConsoleCommandSource() : plugin.server.getPlayer(player.getUniqueId()).orElse(null);
	}

	@Override
    public void sendToDiscord(TabPlayer player, String msg, String channel, List<String> plugins) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF(String.join(",",plugins));
		out.writeUTF(msg);
		out.writeUTF(channel);
		((Player)player.getPlayer()).sendPluginMessage(IDENTIFIER,out.toByteArray());
	}

	@Override
	public boolean supportsChatSuggestions() {
		return false;//isPluginEnabled("Protocolize");
	}

	@Override
	public void updateChatComplete(TabPlayer p, List<String> emojis, boolean add) {
		//not supported, maybe with Protocolize?
	}

	@Override
	public ChatItem getItem(TabPlayer p, boolean offhand) {
		ProtocolizePlayer player = Protocolize.playerProvider().player(p.getUniqueId());
		PlayerInventory inv = player.proxyInventory();
		BaseItemStack item = inv.item(offhand ? inv.heldItem() : 45);
		String displayName = getItemName(item.displayName(),item.itemType().toString());
		return new ChatItem(item.itemType().toString(),displayName,item.amount(),item.nbtData().toString());
	}
}