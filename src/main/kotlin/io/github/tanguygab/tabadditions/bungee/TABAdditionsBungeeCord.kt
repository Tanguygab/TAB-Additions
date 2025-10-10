package io.github.tanguygab.tabadditions.bungee

import io.github.tanguygab.tabadditions.shared.TABAdditions
import net.md_5.bungee.api.ServerPing
import net.md_5.bungee.api.plugin.Plugin

class TABAdditionsBungeeCord : Plugin() {
    private val servers: MutableMap<String, Boolean> = HashMap()

    init {
        TABAdditions.addProperties()
    }

    fun getServerStatus(server: String): String {
        proxy.servers[server]!!.ping { _: ServerPing?, error: Throwable? ->
            servers[server] = error == null
        }
        return if (servers.getOrDefault(server, false)) "Online" else "Offline"
    }

    override fun onEnable() {
        TABAdditions.INSTANCE = TABAdditions(BungeePlatform(this), this, dataFolder)
        TABAdditions.INSTANCE.load()
    }

    override fun onDisable() {
        TABAdditions.INSTANCE.disable()
    }
}