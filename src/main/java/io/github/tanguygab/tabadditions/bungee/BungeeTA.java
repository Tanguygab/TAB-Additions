package io.github.tanguygab.tabadditions.bungee;

import java.util.concurrent.TimeUnit;

import io.github.tanguygab.tabadditions.shared.Platform;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeTA extends Platform {

	private Plugin plugin;
	
	public BungeeTA(Plugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public int AsyncTask(Runnable r, long delay, long period) {
		return ProxyServer.getInstance().getScheduler().schedule(plugin,r,delay/20,period/20, TimeUnit.SECONDS).getId();
	}

	@Override
	public void AsyncTask(Runnable r, long delay) {
		ProxyServer.getInstance().getScheduler().schedule(plugin,r,delay/20,TimeUnit.SECONDS);
	}

	@Override
	public void cancelTask(int id) {
		ProxyServer.getInstance().getScheduler().cancel(id);
	}
}