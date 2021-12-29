package io.github.tanguygab.tabadditions.shared;

import me.neznamy.tab.api.TabPlayer;

import java.util.Map;
import java.util.UUID;

public abstract class Platform {

	public abstract PlatformType getType();

	public abstract Object getSkin(String[] props);

	public abstract boolean isPluginEnabled(String plugin);

	public abstract String getVersion();

	public abstract void registerPlaceholders();

	public abstract void registerCommand(String cmd, boolean bool, String... aliases);

	public abstract void sendTitle(TabPlayer p, String title, String subtitle, int fadein, int stay, int fadeout);

	public abstract void reload();

	public abstract void loadFeatures();

	public abstract void disable();

    public abstract void sendToDiscord(UUID uniqueId, String msg, String channel, boolean viewCondition, Map<String, Boolean> cfg);
}