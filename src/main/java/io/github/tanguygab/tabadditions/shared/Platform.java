package io.github.tanguygab.tabadditions.shared;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.PlaceholderManager;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class Platform {

	public abstract PlatformType getType();

	public abstract boolean isPluginEnabled(String plugin);

	public abstract String getVersion();

	public abstract void registerPlaceholders(PlaceholderManager pm);

	public abstract void registerCommand(String cmd, boolean bool, String... aliases);

	public abstract void sendTitle(TabPlayer p, String title, String subtitle, int fadein, int stay, int fadeout);

	public abstract void sendSound(TabPlayer p, String sound);

	public abstract void reload();

	public abstract void loadFeatures();

	public abstract void disable();

    public abstract void sendToDiscord(UUID uniqueId, String msg, String channel, boolean viewCondition, Map<String, Boolean> cfg);

	public abstract void addToChatComplete(TabPlayer p, List<String> emojis);
	public abstract void removeFromChatComplete(TabPlayer p, List<String> emojis);
	public abstract boolean supportsChatSuggestions();
}