package io.github.tanguygab.tabadditions.bungee;

import io.github.tanguygab.tabadditions.shared.SharedTA;
import net.md_5.bungee.api.plugin.Plugin;


public final class TABAdditionsBungeeCord extends Plugin {

	private static TABAdditionsBungeeCord instance;
	
    @Override
    public void onEnable() {
        SharedTA.platform = "Bungee";
        SharedTA.plugin = this;
        reload();
        getProxy().getPluginManager().registerCommand(this, new MainCmd("btabadditions","tabadditions.admin","btab+","btaba","tabaddon","tabaddition"));
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