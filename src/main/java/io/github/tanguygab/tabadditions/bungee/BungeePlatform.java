package io.github.tanguygab.tabadditions.bungee;


import io.github.tanguygab.tabadditions.shared.Platform;
import io.github.tanguygab.tabadditions.shared.PlatformType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

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
	public void reload() {
		TABAdditions.getInstance().reload();
		plugin.getProxy().getPluginManager().unregisterListeners(plugin);
		plugin.getProxy().getPluginManager().registerListener(plugin, new BungeeEvents());
		TABAdditions.getInstance().floodgate = plugin.getProxy().getPluginManager().getPlugin("Floodgate") != null;
	}

	@Override
	public void disable() {
		plugin.getProxy().getPluginManager().unregisterCommands(plugin);
		plugin.getProxy().getPluginManager().unregisterListeners(plugin);

	}
}