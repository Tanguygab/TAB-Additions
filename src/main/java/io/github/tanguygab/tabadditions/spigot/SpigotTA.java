package io.github.tanguygab.tabadditions.spigot;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import io.github.tanguygab.tabadditions.shared.Platform;

public class SpigotTA extends Platform {

	private final Plugin plugin;
	
	public SpigotTA(Plugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public String type() {
		return "Spigot";
	}

	@Override
	public int AsyncTask(Runnable r, long delay, long period) {
		 return Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, r, delay, period);
	}

	@Override
	public void AsyncTask(Runnable r, long delay) {
		 Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin,r,delay);
	}

	@Override
	public void cancelTask(int id) {
		Bukkit.getServer().getScheduler().cancelTask(id);
	}
}