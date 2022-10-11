package io.github.tanguygab.tabadditions.spigot;

import github.scarsz.discordsrv.DiscordSRV;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.*;

import io.github.tanguygab.tabadditions.shared.features.chat.ChatManager;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import net.essentialsx.api.v2.services.discord.DiscordService;
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
import io.github.tanguygab.tabadditions.shared.PlatformType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public class SpigotPlatform extends Platform {

	private final TABAdditionsSpigot plugin;
	private final boolean chatSuggestions;
	private Method getCommandMap;
	private Constructor<?> chatCompleteConstructor;
	private Class<?> actionEnum;

	public SpigotPlatform(TABAdditionsSpigot plugin) {
		this.plugin = plugin;
		chatSuggestions = TabAPI.getInstance().getServerVersion().getMinorVersion() >= 19;

		try {
			Class<?> chatCompleteClass = Class.forName("net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket");
			actionEnum = Class.forName("net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket$a");
			chatCompleteConstructor = chatCompleteClass.getConstructor(actionEnum,List.class);
			getCommandMap = plugin.getServer().getClass().getMethod("getCommandMap");
		} catch (Exception e) {e.printStackTrace();}
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
	public void registerPlaceholders(PlaceholderManager pm) {
		pm.registerRelationalPlaceholder("%rel_distance%",1000,(viewer, target) -> {
			if (!viewer.getWorld().equals(target.getWorld())) return "-1";
			Location vLoc = ((Player)viewer.getPlayer()).getLocation();
			Location tLoc = ((Player)target.getPlayer()).getLocation();
			return vLoc.distanceSquared(tLoc);
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
		if (!bool) return;
		try {
			Command command = new BukkitCommand(cmd,"","/"+cmd,Arrays.asList(aliases)) {
				@Override public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, String[] args) {return true;}
			};
			((SimpleCommandMap)getCommandMap.invoke(plugin.getServer())).register(cmd,cmd,command);
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
	public void sendToDiscord(UUID uuid, String msg, String channel, boolean viewCondition, Map<String,Boolean> cfg) {

		Player p = plugin.getServer().getPlayer(uuid);
		if (cfg.getOrDefault("DiscordSRV",false) && isPluginEnabled("DiscordSRV")) sendDiscordSRV(p,msg,channel,viewCondition);
		if (cfg.getOrDefault("EssentialsX",false) && isPluginEnabled("EssentialsDiscord") && !viewCondition) sendEssentialsX(p,msg);
	}

	public void sendDiscordSRV(Player p, String msg, String channel, boolean viewCondition) {
		DiscordSRV discord = DiscordSRV.getPlugin();
		if (msg.contains("[item]")) {
			ChatManager chat = (ChatManager) TabAPI.getInstance().getFeatureManager().getFeature("Chat");
			TabPlayer player = TabAPI.getInstance().getPlayer(p.getUniqueId());
			msg = msg.replace("[item]",chat.hovercheck(chat.createComponent("",player),"item:mainhand",player,null,null).toFlatText());
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

	@Override
	public void addToChatComplete(TabPlayer p, List<String> emojis) {
		NMSStorage nms = NMSStorage.getInstance();
		try {
			Object addAction = actionEnum.getEnumConstants()[0];
			Object packet = chatCompleteConstructor.newInstance(addAction,emojis);
			nms.sendPacket.invoke(nms.PLAYER_CONNECTION.get(nms.getHandle.invoke(p.getPlayer())),packet);
		} catch (Exception e) {e.printStackTrace();}
	}
	@Override
	public void removeFromChatComplete(TabPlayer p, List<String> emojis) {
		NMSStorage nms = NMSStorage.getInstance();
		try {
			Object removeAction = actionEnum.getEnumConstants()[1];
			Object packet = chatCompleteConstructor.newInstance(removeAction,emojis);
			nms.sendPacket.invoke(nms.PLAYER_CONNECTION.get(nms.getHandle.invoke(p.getPlayer())),packet);
		} catch (Exception e) {e.printStackTrace();}
	}
	@Override
	public boolean supportsChatSuggestions() {
		return chatSuggestions;
	}
}