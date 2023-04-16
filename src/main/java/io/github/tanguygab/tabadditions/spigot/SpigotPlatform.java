package io.github.tanguygab.tabadditions.spigot;

import github.scarsz.discordsrv.DiscordSRV;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.*;

import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import net.essentialsx.api.v2.services.discord.DiscordService;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

import io.github.tanguygab.tabadditions.shared.Platform;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public class SpigotPlatform extends Platform {

	private final TABAdditionsSpigot plugin;
	private boolean chatSuggestions;
	private Method getCommandMap;
	private Constructor<?> chatCompleteConstructor;
	private Class<?> actionEnum;

	public SpigotPlatform(TABAdditionsSpigot plugin) {
		this.plugin = plugin;

		try {
			getCommandMap = plugin.getServer().getClass().getMethod("getCommandMap");

			Class<?> chatCompleteClass = Class.forName("net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket");
			actionEnum = Class.forName("net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket$Action");
			chatCompleteConstructor = chatCompleteClass.getConstructor(actionEnum,List.class);
			chatSuggestions = true;
		} catch (Exception ignored) {
			chatSuggestions = false;
		}
	}

	@Override
	public boolean isProxy() {
		return false;
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
	public void registerPlaceholders(PlaceholderManager pm) {
		pm.registerRelationalPlaceholder("%rel_distance%",1000,(viewer, target) -> {
			Player viewer0 = (Player) viewer.getPlayer();
			Player target0 = (Player) target.getPlayer();
			if (!viewer0.getWorld().equals(target0.getWorld())) return "-1";
			Location vLoc = viewer0.getLocation();
			Location tLoc = target0.getLocation();
			return vLoc.distanceSquared(tLoc);
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
	public void sendSound(TabPlayer p, String sound) {
		Player player = (Player) p.getPlayer();
		try {player.playSound(player.getLocation(), Sound.valueOf(sound), 1, 1);}
		catch (Exception ignored) {}
	}

	@Override
	public void reload() {
		HandlerList.unregisterAll((Plugin) plugin);
		plugin.getServer().getPluginManager().registerEvents(plugin, plugin);
	}

	@Override
	public void registerCommand(String cmd, boolean bool, String... aliases) {
		if (!bool) return;
		try {
			Command command = new BukkitCommand(cmd,"","/"+cmd,Arrays.asList(aliases)) {
				@Override public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, String[] args) {return true;}
			};
			((SimpleCommandMap)getCommandMap.invoke(plugin.getServer())).register(cmd,"chat",command);
		} catch (Exception e) {e.printStackTrace();}
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
	public void sendToDiscord(UUID uuid, String msg, String channel, boolean viewCondition, String plugin) {
		Player p = this.plugin.getServer().getPlayer(uuid);
		if (plugin.equalsIgnoreCase("DiscordSRV") && isPluginEnabled("DiscordSRV")) sendDiscordSRV(p,msg,channel,viewCondition);
		if (plugin.equalsIgnoreCase("EssentialsX") && isPluginEnabled("EssentialsDiscord") && !viewCondition) sendEssentialsX(p,msg);
	}

	public void sendDiscordSRV(Player p, String msg, String channel, boolean viewCondition) {
		DiscordSRV discord = DiscordSRV.getPlugin();
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

	@Override
	public void addToChatComplete(TabPlayer p, List<String> emojis) {
		try {
			Object addAction = actionEnum.getEnumConstants()[0];
			Object packet = chatCompleteConstructor.newInstance(addAction,emojis);
			((BukkitTabPlayer)p).sendPacket(packet);
		} catch (Exception e) {e.printStackTrace();}
	}
	@Override
	public void removeFromChatComplete(TabPlayer p, List<String> emojis) {
		try {
			Object removeAction = actionEnum.getEnumConstants()[1];
			Object packet = chatCompleteConstructor.newInstance(removeAction,emojis);
			((BukkitTabPlayer)p).sendPacket(packet);
		} catch (Exception e) {e.printStackTrace();}
	}
	@Override
	public boolean supportsChatSuggestions() {
		return chatSuggestions;
	}

	@Override
	public void runTask(Runnable run) {
		plugin.getServer().getScheduler().runTask(plugin,run);
	}
}