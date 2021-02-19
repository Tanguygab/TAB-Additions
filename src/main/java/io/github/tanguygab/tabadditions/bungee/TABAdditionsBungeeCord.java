package io.github.tanguygab.tabadditions.bungee;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import net.md_5.bungee.api.plugin.Plugin;

public final class TABAdditionsBungeeCord extends Plugin {
	
    @Override
    public void onEnable() {
        TABAdditions.setInstance(new TABAdditions(new BungeePlatform(this), this,getDataFolder()));
        TABAdditions.getInstance().load();
        getProxy().getPluginManager().registerCommand(this, new MainCmd("btabadditions","tabadditions.admin","btab+","btaba","btabaddon","btabaddition"));
        getProxy().registerChannel("tabadditions:channel");
    }

    @Override
    public void onDisable() {
        TABAdditions.getInstance().disable();
    }

}