package io.github.tanguygab.tabadditions.spigot;

import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import io.github.tanguygab.tabadditions.shared.Platform;

import java.text.DecimalFormat;

public class SpigotPlatform extends Platform {

	private final TABAdditionsSpigot plugin;
	private static final DecimalFormat format = new DecimalFormat("#.##");

	public SpigotPlatform(TABAdditionsSpigot plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean isProxy() {
		return false;
	}

	@Override
	public void registerPlaceholders(PlaceholderManager pm) {
		pm.registerRelationalPlaceholder("%rel_distance%",1000,(viewer, target) -> {
			Player viewer0 = (Player) viewer.getPlayer(), target0 = (Player) target.getPlayer();
			if (!viewer0.getWorld().equals(target0.getWorld())) return "-1";
			Location vLoc = viewer0.getLocation(), tLoc = target0.getLocation();
			return format.format(Math.round(Math.sqrt(vLoc.distanceSquared(tLoc))));
		});
		pm.registerPlayerPlaceholder("%canseeworldonline%", 1000,viewer->{
			int count = 0;
			Player viewer0 = (Player) viewer.getPlayer();
			for (TabPlayer all : TabAPI.getInstance().getOnlinePlayers()) {
				Player all0 = (Player) all.getPlayer();
				if (viewer0.getWorld().equals(all0.getWorld()) && viewer0.canSee(all0))
					count++;
			}
			return count;
		});
		pm.registerPlayerPlaceholder("%sneak%",-1,player->((Player)player.getPlayer()).isSneaking());
	}

	@Override
	public void sendTitle(TabPlayer p, String title, String subtitle, int fadein, int stay, int fadeout) {
		try {
			((Player) p.getPlayer()).sendTitle(title, subtitle, fadein, stay, fadeout);
		} catch (Exception e) {
			((Player) p.getPlayer()).sendTitle(title,subtitle);
		}
	}

	@Override
	public void sendActionbar(TabPlayer p, String text) {
		((Player)p.getPlayer()).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(text));
	}

	@Override
	public void reload() {
		HandlerList.unregisterAll(plugin);
		plugin.getServer().getPluginManager().registerEvents(new SpigotListener(), plugin);
	}

	@Override
	public void disable() {
		plugin.getPluginLoader().disablePlugin(plugin);
	}

	@Override
	public void runTask(Runnable run) {
		plugin.getServer().getScheduler().runTask(plugin,run);
	}
}