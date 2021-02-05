package io.github.tanguygab.tabadditions.bungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.github.tanguygab.tabadditions.shared.SharedEvents;
import io.github.tanguygab.tabadditions.shared.SharedTA;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.BungeeTABLoadEvent;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;


public class BungeeEvents implements Listener {

    @EventHandler
    public void onJoin(ServerSwitchEvent e) {
        SharedEvents.JoinEvent(e.getPlayer().getName());
    }

    @EventHandler
    public void onTABLoad(BungeeTABLoadEvent e) {
        ((TABAdditionsBungeeCord)SharedTA.plugin).reload();
    }

    @EventHandler
    public void onMessageReceived(PluginMessageEvent e) {
        if (!e.getTag().equalsIgnoreCase("tabadditions:channel")) return;

        ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());
        String subChannel = in.readUTF();
        if (subChannel.equalsIgnoreCase("PlaceholderAPI")) {
            String type = in.readUTF();
            String player = in.readUTF();
            String value = "";
            TabPlayer p = TAB.getInstance().getPlayer(player);
            String result = "";
            switch (type) {
                case "scoreboard_visible":
                    result = ""+p.isScoreboardVisible();
                    break;
                case "bossbar_visible":
                    result = ""+p.hasBossbarVisible();
                    break;
                case "ntpreview":
                    result = ""+p.isPreviewingNametag();
                    break;
                case "Placeholder":
                    value = in.readUTF();
                    result = new Property(p, value).get();
                    type = value;
                    break;
                case "Property":
                    value = in.readUTF();
                    String placeholder = value.replace("_raw", "");
                    Property prop = p.getProperty(placeholder);
                    if (prop != null) {
                        if (value.endsWith("_raw"))
                            result = prop.getCurrentRawValue();
                        else result = prop.lastReplacedValue;
                    }
                    type = value;
                    break;
            }

            if (!result.equalsIgnoreCase("")) {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("PlaceholderAPI");
                out.writeUTF(type);
                out.writeUTF(result);
                ((ProxiedPlayer)p.getPlayer()).getServer().getInfo().sendData("tabadditions:channel", out.toByteArray());
            }
        }
    }
}
