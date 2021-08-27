package io.github.tanguygab.tabadditions.bungee;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatCmds;
import me.neznamy.tab.api.TabAPI;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;

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
        getProxy().getPluginManager().registerCommand(this, new MainCmd("btabadditions","tabadditions.admin","btab+","btaba","btabaddon","btabaddition"));
        getProxy().getPluginManager().registerCommand(this, new TabPlusCmds("btab+cmds", null, "msg", "ignore", "r", "reply","togglemsg","emojis","clearchat"));

    }

    @Override
    public void onDisable() {
        TABAdditions.getInstance().disable();
    }

}