package io.github.tanguygab.tabadditions.spigot;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.spigot.Features.BukkitEvents;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

import io.github.tanguygab.tabadditions.shared.Platform;
import io.github.tanguygab.tabadditions.shared.PlatformType;

public class SpigotPlatform extends Platform {

	private final Plugin plugin;
	
	public SpigotPlatform(Plugin plugin) {
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
	public void reload() {
		TABAdditions.getInstance().reload();

		HandlerList.unregisterAll(plugin);
		Bukkit.getServer().getPluginManager().registerEvents(new BukkitEvents(), plugin);

		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
			new TABAdditionsExpansion(plugin).register();
		TABAdditions.getInstance().floodgate = Bukkit.getPluginManager().getPlugin("Floodgate") != null;
	}

	@Override
	public void disable() {
		plugin.getPluginLoader().disablePlugin(plugin);
	}
}