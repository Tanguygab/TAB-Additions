package io.github.tanguygab.tabadditions.velocity

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.RegisteredServer
import io.github.tanguygab.tabadditions.ProjectVariables
import io.github.tanguygab.tabadditions.shared.TABAdditions
import java.nio.file.Path
import javax.inject.Inject

@Plugin(
    id = "tabadditions",
    name = "TAB-Additions",
    version = ProjectVariables.PLUGIN_VERSION,
    authors = ["Tanguygab"],
    description = "More features for the plugin TAB !",
    dependencies = [Dependency(id = "tab")]
)
class TABAdditionsVelocity @Inject constructor(val server: ProxyServer, @DataDirectory dataDirectory: Path) {
    private val dataFolder = dataDirectory.toFile()
    private val servers = mutableMapOf<String, Boolean>()

    init {
        TABAdditions.addProperties()
    }


    fun getServerStatus(server: RegisteredServer): String {
        val name = server.serverInfo.name
        server.ping().thenAcceptAsync { servers[name] = it != null }
        return if (servers.getOrDefault(name, false)) "Online" else "Offline"
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        TABAdditions.INSTANCE = TABAdditions(VelocityPlatform(this), this, dataFolder)
        TABAdditions.INSTANCE.load()
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        TABAdditions.INSTANCE.disable()
    }
}