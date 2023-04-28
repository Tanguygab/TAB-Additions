package io.github.tanguygab.tabadditions.bungee;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import dev.simplix.protocolize.api.Protocolize;
import dev.simplix.protocolize.api.inventory.PlayerInventory;
import dev.simplix.protocolize.api.item.BaseItemStack;
import dev.simplix.protocolize.api.player.ProtocolizePlayer;
import io.github.tanguygab.tabadditions.shared.Platform;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatItem;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.List;

public class BungeePlatform extends Platform {

	private final TABAdditionsBungeeCord plugin;
	private BungeeListener listener;
	private final BungeeAudiences kyori;

	public BungeePlatform(TABAdditionsBungeeCord plugin) {
		this.plugin = plugin;
		kyori = BungeeAudiences.create(plugin);
	}

	@Override
	public boolean isProxy() {
		return true;
	}

    @Override
	public void registerPlaceholders(PlaceholderManager pm) {
		for (String server : ProxyServer.getInstance().getServers().keySet())
			pm.registerServerPlaceholder("%server-status:" + server + "%",10000,()->plugin.getServerStatus(server));
	}

	@Override
	public void registerCommand(String cmd, String... aliases) {
		plugin.getProxy().getPluginManager().registerCommand(plugin, new Command(cmd,null,aliases) {
			@Override public void execute(CommandSender sender, String[] args) {}
		});
	}

	@Override
	public boolean isPluginEnabled(String plugin) {
		return this.plugin.getProxy().getPluginManager().getPlugin(plugin) != null;
	}

	@Override
	public void sendTitle(TabPlayer p, String title, String subtitle, int fadein, int stay, int fadeout) {
		Title t = plugin.getProxy().createTitle()
				.title(new TextComponent(title))
				.subTitle(new TextComponent(subtitle))
				.fadeIn(fadein)
				.stay(stay)
				.fadeOut(fadeout);
		((ProxiedPlayer)p.getPlayer()).sendTitle(t);
	}

	@Override
	public void sendActionbar(TabPlayer p, String text) {
		((ProxiedPlayer)p.getPlayer()).sendMessage(ChatMessageType.ACTION_BAR,TextComponent.fromLegacyText(text));
	}

	@Override
	public void reload() {
		plugin.getProxy().getPluginManager().unregisterListener(listener);
		plugin.getProxy().getPluginManager().registerListener(plugin,listener = new BungeeListener());
	}

	@Override
	public void disable() {
		plugin.getProxy().getPluginManager().unregisterCommands(plugin);
	}

	@Override
	public void runTask(Runnable run) {
		plugin.getProxy().getScheduler().runAsync(plugin,run);
	}

	@Override
	public Audience getAudience(TabPlayer p) {
		return kyori.player(p.getUniqueId());
	}

	@Override
	public void sendToDiscord(TabPlayer player, String msg, String channel, boolean viewCondition, List<String> plugins) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF(String.join(",",plugins));
		out.writeUTF(msg);
		out.writeUTF(channel);
		out.writeUTF(viewCondition+"");
		((ProxiedPlayer)player.getPlayer()).sendData("tabadditions:channel",out.toByteArray());
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