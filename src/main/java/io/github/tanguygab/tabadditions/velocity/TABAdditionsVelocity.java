package io.github.tanguygab.tabadditions.velocity;

import com.google.common.eventbus.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import io.github.tanguygab.tabadditions.ProjectVariables;
import io.github.tanguygab.tabadditions.shared.TABAdditions;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Plugin(
        id = "tabadditions",
        name = "TAB-Additions",
        version = ProjectVariables.PLUGIN_VERSION,
        authors = {"Tanguygab"},
        description = "More features for the plugin TAB !",
        dependencies = {
                @Dependency(id = "tab"),
                @Dependency(id = "mckotlin-velocity")
        }
)
public class TABAdditionsVelocity {

    public final ProxyServer server;
    private final File dataFolder;
    public final Map<String, Boolean> servers = new HashMap<>();

    @Inject
    public TABAdditionsVelocity(ProxyServer server, @DataDirectory Path dataDirectory) {
        this.server = server;
        dataFolder = dataDirectory.toFile();
        TABAdditions.Companion.addProperties();
    }



    public String getServerStatus(RegisteredServer server) {
        String name = server.getServerInfo().getName();
        server.ping().thenAcceptAsync(ping -> servers.put(name, ping != null));
        return servers.getOrDefault(name, false) ? "Online" : "Offline";
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent e) {
        TABAdditions.INSTANCE = new TABAdditions(new VelocityPlatform(this), this, dataFolder);
        TABAdditions.INSTANCE.load();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent e) {
        TABAdditions.INSTANCE.disable();
    }
}