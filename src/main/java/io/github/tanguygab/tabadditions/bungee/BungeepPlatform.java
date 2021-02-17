package io.github.tanguygab.tabadditions.bungee;

import java.util.concurrent.TimeUnit;

import io.github.tanguygab.tabadditions.shared.Platform;
import io.github.tanguygab.tabadditions.shared.PlatformType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeepPlatform extends Platform {

	private final Plugin plugin;
	
	public BungeepPlatform(Plugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public PlatformType getType() {
		return PlatformType.BUNGEE;
	}

	@Override
	public Object getSkin(String[] props) {
		String[][] s = new String[1][3];
		s[0][0] = "textures";
		s[0][1] = props[0];
		s[0][2] = props[1];
		return s;
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