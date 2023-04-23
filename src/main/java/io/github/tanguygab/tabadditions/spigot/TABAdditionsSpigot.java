package io.github.tanguygab.tabadditions.spigot;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import org.bukkit.plugin.java.JavaPlugin;


public class TABAdditionsSpigot extends JavaPlugin {

    @Override
    public void onEnable() {
        TABAdditions.setInstance(new TABAdditions(new SpigotPlatform(this), this,getDataFolder()));
        TABAdditions.getInstance().load();
    }

    @Override
    public void onDisable() {
        TABAdditions.getInstance().disable();
    }

}
