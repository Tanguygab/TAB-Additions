package io.github.tanguygab.tabadditions.shared;

import dev.simplix.protocolize.api.Protocolize;
import dev.simplix.protocolize.api.inventory.PlayerInventory;
import dev.simplix.protocolize.api.item.BaseItemStack;
import dev.simplix.protocolize.api.player.ProtocolizePlayer;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatItem;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import net.kyori.adventure.audience.Audience;

import java.util.List;

public abstract class Platform {

	public abstract boolean isProxy();
	public abstract void runTask(Runnable run);
	public abstract void reload();
	public abstract void registerPlaceholders(PlaceholderManager pm);
	public abstract void registerCommand(String cmd, String... aliases);
	public abstract boolean isPluginEnabled(String plugin);
	public abstract void disable();

	public abstract void sendTitle(TabPlayer p, String title, String subtitle, int fadeIn, int stay, int fadeout);
	public abstract void sendActionbar(TabPlayer p, String text);

	public abstract Audience audience(TabPlayer player);
	public abstract void sendToDiscord(TabPlayer player, String msg, String channel, List<String> plugins);
	public abstract boolean supportsChatSuggestions();
	public abstract void updateChatComplete(TabPlayer p, List<String> emojis, boolean add);

	public ChatItem getItem(TabPlayer p, boolean offhand) {
		ProtocolizePlayer player = Protocolize.playerProvider().player(p.getUniqueId());
		PlayerInventory inv = player.proxyInventory();
		BaseItemStack item = inv.item(offhand ? inv.heldItem() : 45);
		String displayName = getItemName(item.displayName(),item.itemType().toString());
		return new ChatItem(item.itemType().toString(),displayName,item.amount(),item.nbtData().toString());
	}

	protected String getItemName(String displayName, String type) {
		if (displayName != null) return displayName;
		type = type.replace("_", " ").toLowerCase();
		StringBuilder type2 = new StringBuilder();
		List<String> typelist = List.of(type.split(" "));
		for (String str : typelist) {
			type2.append(str.substring(0, 1).toUpperCase()).append(str.substring(1));
			if (typelist.indexOf(str) != typelist.size() - 1) type2.append(" ");
		}
		return type2.toString();
	}

}