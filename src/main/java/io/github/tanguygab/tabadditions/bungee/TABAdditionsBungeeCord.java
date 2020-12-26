package io.github.tanguygab.tabadditions.bungee;

import io.github.tanguygab.tabadditions.shared.SharedTA;
import io.github.tanguygab.tabadditions.shared.layouts.LayoutManager;
import net.md_5.bungee.api.plugin.Plugin;


public final class TABAdditionsBungeeCord extends Plugin {

	private static TABAdditionsBungeeCord instance;
	
    @Override
    public void onEnable() {
        SharedTA.platform = new BungeeTA(this);
        SharedTA.plugin = this;
        reload();
        getProxy().getPluginManager().registerCommand(this, new MainCmd("btabadditions","tabadditions.admin","btab+","btaba","tabaddon","tabaddition"));
    }

    @Override
    public void onDisable() {
        if (LayoutManager.getInstance() != null) LayoutManager.getInstance().unregister();
    }

    public static TABAdditionsBungeeCord getInstance() {
    	return instance;
    }

    public void reload() {
        SharedTA.reload(getDataFolder());
        getProxy().getPluginManager().unregisterListeners(this);
        getProxy().getPluginManager().registerListener(this, new BungeeEvents());
    }
}