package io.github.tanguygab.tabadditions.bungee;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public final class TABAdditionsBungeeCord extends Plugin {

    private final Map<String,Boolean> servers = new HashMap<>();

    public TABAdditionsBungeeCord() {
        TABAdditions.addProperties();
    }

    public String getServerStatus(String server) {
        getProxy().getServers().get(server).ping((result, error) -> {
            if (error == null)
                servers.put(server,true);
            else servers.put(server,false);
        });
        return servers.containsKey(server) && servers.get(server) ? "Online" : "Offline";
    }

    @Override
    public void onEnable() {
        TABAdditions.setInstance(new TABAdditions(new BungeePlatform(this), this,getDataFolder()));
        TABAdditions.getInstance().load();
    }

    @Override
    public void onDisable() {
        TABAdditions.getInstance().disable();
    }

}