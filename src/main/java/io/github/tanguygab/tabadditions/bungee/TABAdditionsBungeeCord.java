package io.github.tanguygab.tabadditions.bungee;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public final class TABAdditionsBungeeCord extends Plugin {

    Map<String,Boolean> servers = new HashMap<>();

    public String getServerStatus(String server) {
        getProxy().getServers().get(server).ping((result, error) -> {
            if (error == null)
                servers.put(server,true);
            else servers.put(server,false);
        });
        String output = "Offline";
        if (servers.containsKey(server) && servers.get(server))
            output = "Online";
        return output;
    }

    @Override
    public void onEnable() {
        TABAdditions.setInstance(new TABAdditions(new BungeePlatform(this), this,getDataFolder()));
        TABAdditions.getInstance().load();
        getProxy().registerChannel("tabadditions:channel");
    }

    @Override
    public void onDisable() {
        TABAdditions.getInstance().disable();
    }

}