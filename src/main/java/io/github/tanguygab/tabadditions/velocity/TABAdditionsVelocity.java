package io.github.tanguygab.tabadditions.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import io.github.tanguygab.tabadditions.shared.TABAdditions;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Plugin(
        id = "tabadditions",
        name = "TAB-Additions",
        version = "@version@",
        authors = {"Tanguygab"},
        description = "More features for the plugin TAB !",
        dependencies = {
            @Dependency(id = "tab")
        }
)
public final class TABAdditionsVelocity {

    final ProxyServer server;
    private final File dataFolder;
    private final Map<String,Boolean> servers = new HashMap<>();

    @Inject
    public TABAdditionsVelocity(ProxyServer server, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.dataFolder = dataDirectory.toFile();
        TABAdditions.addProperties();
    }


    public String getServerStatus(RegisteredServer server) {
        String name = server.getServerInfo().getName();
        server.ping().thenAcceptAsync(ping->{
            if (ping != null)
                servers.put(name, true);
            else servers.put(name, false);
        });

        return servers.containsKey(name) && servers.get(name) ? "Online" : "Offline";
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        TABAdditions.setInstance(new TABAdditions(new VelocityPlatform(this), this, dataFolder));
        TABAdditions.getInstance().load();
    }
    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        TABAdditions.getInstance().disable();
    }


}