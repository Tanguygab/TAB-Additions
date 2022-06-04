package io.github.tanguygab.tabadditions.spigot;

import github.scarsz.discordsrv.DiscordSRV;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.*;

import io.github.tanguygab.tabadditions.shared.features.chat.ChatManager;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import net.essentialsx.api.v2.services.discord.DiscordService;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import io.github.tanguygab.tabadditions.shared.Platform;
import io.github.tanguygab.tabadditions.shared.PlatformType;

import java.util.Map;
import java.util.UUID;

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
	public boolean isPluginEnabled(String plugin) {
		return this.plugin.getServer().getPluginManager().isPluginEnabled(plugin);
	}

	@Override
	public String getVersion() {
		return plugin.getDescription().getVersion();
	}

	@Override
	public void registerPlaceholders() {}

	@Override
	public void sendTitle(TabPlayer p, String title, String subtitle, int fadein, int stay, int fadeout) {
		try {
			((Player) p.getPlayer()).sendTitle(title, subtitle, fadein, stay, fadeout);
		} catch (Exception e) {
			((Player) p.getPlayer()).sendTitle(title,subtitle);
		}
	}

	@Override
	public void sendSound(TabPlayer p, String sound) {
		Player player = (Player) p.getPlayer();
		try {player.playSound(player.getLocation(), Sound.valueOf(sound), 1, 1);}
		catch (Exception ignored) {}
	}

	@Override
	public void reload() {
		TABAdditions.getInstance().reload();

		HandlerList.unregisterAll((Plugin) plugin);
		plugin.getServer().getPluginManager().registerEvents(plugin, plugin);

		if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null)
			new TABAdditionsExpansion(plugin).register();

	}

	@Override
	public void registerCommand(String cmd, boolean bool, String... aliases) {
		if (bool && plugin.getCommand(cmd) != null)
			plugin.getCommand(cmd).setExecutor(new TabPlusCmds());
	}

	@Override
	public void loadFeatures() {
		TABAdditions instance = TABAdditions.getInstance();
		//Sneak Hide Nametag
		if (instance.sneakhideEnabled)
			instance.registerFeature(new SneakHideNametag());
		//Sneak Hide Nametag
		if (instance.sithideEnabled)
			instance.registerFeature(new SitHideNametag());
		//Nametag in Range
		if (instance.nametagInRange != 0)
			instance.registerFeature(new NametagInRange());
		//Tablist Names Radius
		if (instance.tablistNamesRadius != 0)
			instance.registerFeature(new TablistNamesRadius());
		//Only You
		if (instance.onlyyou)
			instance.registerFeature(new OnlyYou());
	}

	@Override
	public void disable() {
		plugin.getPluginLoader().disablePlugin(plugin);
	}

	@Override
	public void sendToDiscord(UUID uuid, String msg, String channel, boolean viewCondition, Map<String,Boolean> cfg) {

		Player p = plugin.getServer().getPlayer(uuid);
		if (cfg.getOrDefault("DiscordSRV",false) && isPluginEnabled("DiscordSRV")) sendDiscordSRV(p,msg,channel,viewCondition);
		if (cfg.getOrDefault("EssentialsX",false) && isPluginEnabled("EssentialsDiscord") && !viewCondition) sendEssentialsX(p,msg);
	}

	public void sendDiscordSRV(Player p, String msg, String channel, boolean viewCondition) {
		DiscordSRV discord = DiscordSRV.getPlugin();
		if (msg.contains("[item]")) {
			ChatManager chat = (ChatManager) TabAPI.getInstance().getFeatureManager().getFeature("Chat");
			msg.replace("[item]",chat.hovercheck(null,"item:mainhand",TabAPI.getInstance().getPlayer(p.getUniqueId()),null,null).toFlatText());
		}

		if (!viewCondition)
			discord.processChatMessage(p, msg, discord.getMainChatChannel(), false);
		else if (!discord.getOptionalChannel(channel).equals(discord.getMainChatChannel()))
			discord.processChatMessage(p, msg, discord.getOptionalChannel(channel),false);
	}

	public void sendEssentialsX(Player p, String msg) {
		DiscordService api = plugin.getServer().getServicesManager().load(DiscordService.class);
		assert api != null;
		api.sendChatMessage(p, msg);
	}

}