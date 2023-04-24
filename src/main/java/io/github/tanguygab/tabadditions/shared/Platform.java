package io.github.tanguygab.tabadditions.shared;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import net.kyori.adventure.audience.Audience;

public abstract class Platform {

	public abstract boolean isProxy();

	public abstract void registerPlaceholders(PlaceholderManager pm);

	public abstract void sendTitle(TabPlayer p, String title, String subtitle, int fadein, int stay, int fadeout);

	public abstract void sendActionbar(TabPlayer p, String text);

	public abstract void reload();

	public abstract void disable();

	public abstract void runTask(Runnable run);

	public abstract Audience getAudience(TabPlayer p);
}