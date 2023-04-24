package io.github.tanguygab.tabadditions.bungee;

import io.github.tanguygab.tabadditions.shared.Platform;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeePlatform extends Platform {

	private final TABAdditionsBungeeCord plugin;
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
			pm.registerServerPlaceholder("%server-status:" + server + "%",10000,()->((TABAdditionsBungeeCord)plugin).getServerStatus(server));
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
	public void reload() {}

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
}