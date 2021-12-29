package io.github.tanguygab.tabadditions.bungee;


import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.github.tanguygab.tabadditions.shared.Platform;
import io.github.tanguygab.tabadditions.shared.PlatformType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

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
	public Object getSkin(String[] props) {
		String[][] properties = new String[1][3];
		properties[0][0] = "textures";
		properties[0][1] = props[0];
		properties[0][2] = props[1];
		return properties;
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
	public void registerPlaceholders() {
		PlaceholderManager pm = TabAPI.getInstance().getPlaceholderManager();
		TABAdditions taba = TABAdditions.getInstance();
		pm.registerPlayerPlaceholder("%money%",1000,p->taba.parsePlaceholders("%vault_eco_balance%",p));
		pm.registerPlayerPlaceholder("%health%",100,p->taba.parsePlaceholders("%player_health_rounded%",p));
		pm.registerServerPlaceholder("%tps%",1000,()->taba.parsePlaceholders("%server_tps_1%",null));
		pm.registerPlayerPlaceholder("%afk%",500,p->taba.parsePlaceholders("%essentials_afk%",p));
		for (String server : ProxyServer.getInstance().getServers().keySet())
			pm.registerServerPlaceholder("%server-online:" + server + "%",10000,()->((TABAdditionsBungeeCord)plugin).getServerStatus(server));
	}

	@Override
	public void registerCommand(String cmd, boolean bool, String... aliases) {
		if (bool) {
			if (aliases == null)
				plugin.getProxy().getPluginManager().registerCommand(plugin, new TabPlusCmds(cmd));
			else plugin.getProxy().getPluginManager().registerCommand(plugin, new TabPlusCmds(cmd, null, aliases));
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
	public void reload() {
		TABAdditions.getInstance().reload();
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
}