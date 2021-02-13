package io.github.tanguygab.tabadditions.bungee;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import net.md_5.bungee.api.plugin.Plugin;

public final class TABAdditionsBungeeCord extends Plugin {
	
    @Override
    public void onEnable() {
        TABAdditions.setInstance(new TABAdditions(new BungeeTA(this), this));
        reload();
        getProxy().getPluginManager().registerCommand(this, new MainCmd("btabadditions","tabadditions.admin","btab+","btaba","btabaddon","btabaddition"));
        getProxy().registerChannel("tabadditions:channel");
    }

    @Override
    public void onDisable() {
        TABAdditions.getInstance().disable();
    }

    public void reload() {
        TABAdditions.getInstance().reload(getDataFolder());
        getProxy().getPluginManager().unregisterListeners(this);
        getProxy().getPluginManager().registerListener(this, new BungeeEvents());
        TABAdditions.getInstance().floodgate = getProxy().getPluginManager().getPlugin("Floodgate") != null;
    }
}