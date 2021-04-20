package io.github.tanguygab.tabadditions.shared;

public abstract class Platform {

	public abstract PlatformType getType();

	public abstract Object getSkin(String[] props);

	public abstract boolean isPluginEnabled(String plugin);

	public abstract void reload();

	public abstract void disable();

}