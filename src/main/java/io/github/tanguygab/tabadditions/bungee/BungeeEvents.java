package io.github.tanguygab.tabadditions.bungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.github.tanguygab.tabadditions.shared.ConfigType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.TAFeature;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatManager;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.BungeeTABLoadEvent;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.Placeholder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.Map;


public class BungeeEvents implements Listener {

    @EventHandler
    public void onTABLoad(BungeeTABLoadEvent e) {
        TABAdditions.getInstance().getPlatform().reload();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(ChatEvent e) {
        if (e.isCommand() || e.isCancelled()) return;
        TAB tab = TAB.getInstance();
        if (!tab.getFeatureManager().isFeatureEnabled(TAFeature.CHAT.toString())) return;
        if (TABAdditions.getInstance().getConfig(ConfigType.CHAT).getBoolean("chat-from-bukkit-bridge",false))
            return;
        e.setCancelled(true);
        ((ChatManager)tab.getFeatureManager().getFeature(TAFeature.CHAT.toString())).onChat(tab.getPlayer(((ProxiedPlayer)e.getSender()).getUniqueId()),e.getMessage());
    }

    @EventHandler
    public void onMessageReceived(PluginMessageEvent e) {
        if (!e.getTag().equalsIgnoreCase("tabadditions:channel")) return;
        ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());
        String subChannel = in.readUTF();
        TAB tab = TAB.getInstance();
        TabPlayer p = tab.getPlayer(((ProxiedPlayer) e.getReceiver()).getUniqueId());
        if (subChannel.equalsIgnoreCase("Chat")) {
            String msg = in.readUTF();
            if (tab.getFeatureManager().isFeatureEnabled(TAFeature.CHAT.toString()))
                ((ChatManager)tab.getFeatureManager().getFeature(TAFeature.CHAT.toString())).onChat(p,msg);
            return;
        }
        if (subChannel.equalsIgnoreCase("PlaceholderAPI")) {
            String type = in.readUTF().toLowerCase();
            String value = "";
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
                case "replace":
                    if (tab.getConfiguration().getPremiumConfig() != null) {
                        String output = tab.getPlatform().replaceAllPlaceholders(value,p);
                        Map<Object, String> replacements = tab.getConfiguration().getPremiumConfig().getConfigurationSection("placeholder-output-replacements." + value);
                        result =  Placeholder.findReplacement(replacements, output).replace("%value%", output);
                    }
                    break;
                case "placeholder":
                    value = in.readUTF();
                    result = new Property(p, value).get();
                    type = value;
                    break;
                case "property":
                    value = in.readUTF();
                    String placeholder = value.replace("_raw", "");
                    Property prop = p.getProperty(placeholder);
                    if (prop != null) {
                        if (value.endsWith("_raw"))
                            result = prop.getCurrentRawValue();
                        else result = prop.get();
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
