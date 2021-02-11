package io.github.tanguygab.tabadditions.bungee;

import java.util.concurrent.TimeUnit;

import io.github.tanguygab.tabadditions.shared.Platform;
import io.github.tanguygab.tabadditions.shared.PlatformType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeTA extends Platform {

	private final Plugin plugin;
	
	public BungeeTA(Plugin plugin) {
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
	public int AsyncTask(Runnable r, long delay, long period) {
		return ProxyServer.getInstance().getScheduler().schedule(plugin,r,delay,period, TimeUnit.MILLISECONDS).getId();
	}

	@Override
	public void AsyncTask(Runnable r, long delay) {
		ProxyServer.getInstance().getScheduler().schedule(plugin,r,delay,TimeUnit.MILLISECONDS);
	}

	@Override
	public void cancelTask(int id) {
		ProxyServer.getInstance().getScheduler().cancel(id);
	}

	@Override
	public void disable() {
		plugin.getProxy().getPluginManager().unregisterCommands(plugin);
		plugin.getProxy().getPluginManager().unregisterListeners(plugin);

	}
}