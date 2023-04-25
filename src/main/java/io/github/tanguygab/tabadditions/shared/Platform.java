package io.github.tanguygab.tabadditions.shared;

import io.github.tanguygab.tabadditions.shared.features.chat.ChatItem;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import net.kyori.adventure.audience.Audience;

import java.util.List;
import java.util.UUID;

public abstract class Platform {

	public abstract boolean isProxy();
	public abstract void runTask(Runnable run);
	public abstract void reload();
	public abstract void registerPlaceholders(PlaceholderManager pm);
	public abstract void registerCommand(String cmd, String... aliases);
	public abstract boolean isPluginEnabled(String plugin);
	public abstract void disable();

	public abstract void sendTitle(TabPlayer p, String title, String subtitle, int fadein, int stay, int fadeout);
	public abstract void sendActionbar(TabPlayer p, String text);

	public abstract Audience getAudience(TabPlayer p);
	public abstract void sendToDiscord(UUID uuid, String msg, String channel, boolean viewCondition, List<String> plugins);
	public abstract boolean supportsChatSuggestions();
	public abstract void updateChatComplete(TabPlayer p, List<String> emojis, boolean add);
	public abstract ChatItem getItem(TabPlayer p, boolean offhand);
	protected String getItemName(String displayname, String type) {
		if (displayname != null) return displayname;
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