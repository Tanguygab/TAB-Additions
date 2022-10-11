package io.github.tanguygab.tabadditions.bungee;


import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.github.tanguygab.tabadditions.shared.Platform;
import io.github.tanguygab.tabadditions.shared.PlatformType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BungeePlatform extends Platform {

	private final Plugin plugin;
	
	public BungeePlatform(Plugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public PlatformType getType() {
		return PlatformType.BUNGEE;
	}

	@Override
	public boolean isPluginEnabled(String plugin) {
		return this.plugin.getProxy().getPluginManager().getPlugin(plugin) != null;
	}

	@Override
	public String getVersion() {
		return ProxyServer.getInstance().getPluginManager().getPlugin("TAB-Additions").getDescription().getVersion();
	}

	@Override
	public void registerPlaceholders(PlaceholderManager pm) {
		for (String server : ProxyServer.getInstance().getServers().keySet())
			pm.registerServerPlaceholder("%server-status:" + server + "%",10000,()->((TABAdditionsBungeeCord)plugin).getServerStatus(server));
	}

	@Override
	public void registerCommand(String cmd, boolean bool, String... aliases) {
		if (bool) {
			plugin.getProxy().getPluginManager().registerCommand(plugin, new Command(cmd,null,aliases) {
				@Override public void execute(CommandSender sender, String[] args) {}
			});
		}
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
	public void sendSound(TabPlayer p, String sound) {
		// not supported
	}

	@Override
	public void reload() {
		plugin.getProxy().getPluginManager().unregisterListeners(plugin);
		plugin.getProxy().getPluginManager().registerListener(plugin, new BungeeEvents());
	}

	@Override
	public void loadFeatures() {}

	@Override
	public void disable() {
		plugin.getProxy().getPluginManager().unregisterCommands(plugin);
		plugin.getProxy().getPluginManager().unregisterListeners(plugin);

	}

	@Override
	public void sendToDiscord(UUID uniqueId, String msg, String channel, boolean viewCondition, Map<String, Boolean> cfg) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("DiscordSRV");
		out.writeUTF(msg);
		out.writeUTF(channel);
		out.writeUTF(viewCondition+"");
		plugin.getProxy().getPlayer(UUID.randomUUID()).sendData("tabadditions:channel",out.toByteArray());
	}

	@Override
	public void addToChatComplete(TabPlayer p, List<String> emojis) {}
	@Override
	public void removeFromChatComplete(TabPlayer p, List<String> emojis) {}
	@Override
	public boolean supportsChatSuggestions() {
		return false;
	}
}