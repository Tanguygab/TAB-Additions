package io.github.tanguygab.tabadditions.spigot;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import io.github.tanguygab.tabadditions.shared.Platform;
import io.github.tanguygab.tabadditions.shared.PlatformType;

public class SpigotTA extends Platform {

	private final Plugin plugin;
	
	public SpigotTA(Plugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public PlatformType getType() {
		return PlatformType.SPIGOT;
	}

	@Override
	public Object getSkin(String[] props) {
		PropertyMap properties = new PropertyMap();
		Property property = new Property("textures",props[0],props[1]);
		properties.put("textures",property);
		return properties;
	}

	@Override
	public int AsyncTask(Runnable r, long delay, long period) {
		 return Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, r, delay*20/1000, period*20/1000);
	}

	@Override
	public void AsyncTask(Runnable r, long delay) {
		 Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin,r,delay*20/1000);
	}

	@Override
	public void cancelTask(int id) {
		Bukkit.getServer().getScheduler().cancelTask(id);
	}

	@Override
	public void disable() {
		plugin.getPluginLoader().disablePlugin(plugin);
	}
}