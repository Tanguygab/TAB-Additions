package io.github.tanguygab.tabadditions.spigot;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

import io.github.tanguygab.tabadditions.shared.Platform;
import io.github.tanguygab.tabadditions.shared.PlatformType;

public class SpigotPlatform extends Platform {

	private final TABAdditionsSpigot plugin;

	public SpigotPlatform(TABAdditionsSpigot plugin) {
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
	public boolean isPluginEnabled(String plugin) {
		return Bukkit.getPluginManager().isPluginEnabled(plugin);
	}

	@Override
	public String getVersion() {
		return plugin.getDescription().getVersion();
	}

	@Override
	public void registerPlaceholders() {}

	@Override
	public void sendTitle(TabPlayer p, String title, String subtitle, int fadein, int stay, int fadeout) {
		((Player)p.getPlayer()).sendTitle(title,subtitle,fadein,stay,fadeout);
	}

	@Override
	public void reload() {
		TABAdditions.getInstance().reload();

		HandlerList.unregisterAll((Plugin) plugin);
		Bukkit.getServer().getPluginManager().registerEvents(plugin, plugin);

		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
			new TABAdditionsExpansion(plugin).register();
	}

	@Override
	public void disable() {
		plugin.getPluginLoader().disablePlugin(plugin);
	}
}