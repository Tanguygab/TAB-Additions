package io.github.tanguygab.tabadditions.bungee;

import io.github.tanguygab.tabadditions.shared.SharedTA;
import io.github.tanguygab.tabadditions.shared.features.layouts.LayoutManager;
import net.md_5.bungee.api.plugin.Plugin;

public final class TABAdditionsBungeeCord extends Plugin {
	
    @Override
    public void onEnable() {
        SharedTA.platform = new BungeeTA(this);
        SharedTA.plugin = this;
        reload();
        getProxy().getPluginManager().registerCommand(this, new MainCmd("btabadditions","tabadditions.admin","btab+","btaba","btabaddon","btabaddition"));
        getProxy().registerChannel("tabadditions:channel");
    }

    @Override
    public void onDisable() {
        if (LayoutManager.getInstance() != null) LayoutManager.getInstance().unregister();
    }

    public void reload() {
        SharedTA.reload(getDataFolder());
        getProxy().getPluginManager().unregisterListeners(this);
        getProxy().getPluginManager().registerListener(this, new BungeeEvents());

        SharedTA.floodgate = getProxy().getPluginManager().getPlugin("Floodgate") != null;
    }
}