package io.github.tanguygab.tabadditions.spigot

import io.github.tanguygab.tabadditions.shared.TABAdditions
import org.bukkit.plugin.java.JavaPlugin

class TABAdditionsSpigot : JavaPlugin() {
    init {
        TABAdditions.addProperties()
    }

    override fun onEnable() {
        TABAdditions.INSTANCE = TABAdditions(SpigotPlatform(this), this, dataFolder)
        TABAdditions.INSTANCE.load()
    }

    override fun onDisable() {
        TABAdditions.INSTANCE.disable()
    }
}
