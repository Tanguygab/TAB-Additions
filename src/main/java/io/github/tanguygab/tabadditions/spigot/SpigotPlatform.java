package io.github.tanguygab.tabadditions.spigot;

import github.scarsz.discordsrv.DiscordSRV;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatItem;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import net.essentialsx.api.v2.services.discord.DiscordService;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import io.github.tanguygab.tabadditions.shared.Platform;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.List;

public class SpigotPlatform extends Platform {

	private final TABAdditionsSpigot plugin;
	private SpigotListener listener;
	private final BukkitAudiences kyori;
	private static final DecimalFormat format = new DecimalFormat("#.##");

	private final boolean chatSuggestions;
	private Method getCommandMap;

	public SpigotPlatform(TABAdditionsSpigot plugin) {
		this.plugin = plugin;
		kyori = BukkitAudiences.create(plugin);
		chatSuggestions = TAB.getInstance().getServerVersion().getNetworkId() >= 762;
		try {
			getCommandMap = plugin.getServer().getClass().getMethod("getCommandMap");
		} catch (Exception ignored) {}
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
	public void registerCommand(String cmd, String... aliases) {
		try {
			Command command = new BukkitCommand(cmd,"","/"+cmd,List.of(aliases)) {
				@Override public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, String[] args) {return true;}
			};
			((SimpleCommandMap)getCommandMap.invoke(plugin.getServer())).register(cmd,"chat",command);
		} catch (Exception e) {e.printStackTrace();}
	}

	@Override
	public boolean isPluginEnabled(String plugin) {
		return this.plugin.getServer().getPluginManager().isPluginEnabled(plugin);
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
		HandlerList.unregisterAll(listener);
		plugin.getServer().getPluginManager().registerEvents(listener = new SpigotListener(), plugin);
	}

	@Override
	public void disable() {
		plugin.getPluginLoader().disablePlugin(plugin);
	}

	@Override
	public void runTask(Runnable run) {
		plugin.getServer().getScheduler().runTask(plugin,run);
	}

	@Override
	public Audience getAudience(TabPlayer p) {
		return kyori.player(p.getUniqueId());
	}

	@Override
	public void sendToDiscord(TabPlayer player, String msg, String channel, boolean viewCondition, List<String> plugins) {
		Player p = (Player) player.getPlayer();
		if (plugins.contains("DiscordSRV") && isPluginEnabled("DiscordSRV")) sendDiscordSRV(p,msg,channel,viewCondition);
		if (plugins.contains("EssentialsX") && isPluginEnabled("EssentialsDiscord") && !viewCondition) sendEssentialsX(p,msg);
	}
	private void sendDiscordSRV(Player p, String msg, String channel, boolean viewCondition) {
		DiscordSRV discord = DiscordSRV.getPlugin();
		if (!viewCondition)
			discord.processChatMessage(p, msg, discord.getMainChatChannel(), false);
		else if (!discord.getOptionalChannel(channel).equals(discord.getMainChatChannel()))
			discord.processChatMessage(p, msg, discord.getOptionalChannel(channel),false);
	}
	private void sendEssentialsX(Player p, String msg) {
		DiscordService api = plugin.getServer().getServicesManager().load(DiscordService.class);
		assert api != null;
		api.sendChatMessage(p, msg);
	}

	@Override
	public boolean supportsChatSuggestions() {
		return chatSuggestions;
	}

	@Override
	public void updateChatComplete(TabPlayer p, List<String> emojis, boolean add) {
		if (add) ((Player)p.getPlayer()).addCustomChatCompletions(emojis);
		else ((Player)p.getPlayer()).removeCustomChatCompletions(emojis);
	}

	@Override
	public ChatItem getItem(TabPlayer p, boolean offhand) {
		PlayerInventory inv = ((Player)p.getPlayer()).getInventory();
		ItemStack item;
		try {item = offhand ? inv.getItemInOffHand() : inv.getItemInMainHand();}
		catch (Exception e) {item = inv.getItemInHand();}
		return new ChatItem(item.getType().getKey().toString(),
				getItemName(item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : null,item.getType().toString()),
				item.getAmount(),
				item.hasItemMeta() ? item.getItemMeta().getAsString() : null);
	}

}