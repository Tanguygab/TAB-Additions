package io.github.tanguygab.tabadditions.shared;

import io.github.tanguygab.tabadditions.shared.features.chat.ChatItem;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import net.kyori.adventure.platform.AudienceProvider;

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

	public abstract AudienceProvider getKyori();
	public abstract void sendToDiscord(TabPlayer player, String msg, String channel, boolean viewCondition, List<String> plugins);
	public abstract boolean supportsChatSuggestions();
	public abstract void updateChatComplete(TabPlayer p, List<String> emojis, boolean add);
	public abstract ChatItem getItem(TabPlayer p, boolean offhand);
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