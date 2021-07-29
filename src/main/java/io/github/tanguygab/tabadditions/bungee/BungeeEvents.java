package io.github.tanguygab.tabadditions.bungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.github.tanguygab.tabadditions.shared.ConfigType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatManager;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bungeecord.BungeeTABLoadEvent;
import me.neznamy.tab.api.Property;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.TAB;
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
        TabAPI tab = TabAPI.getInstance();
        if (!tab.getFeatureManager().isFeatureEnabled("&aChat&r")) return;
        if (TABAdditions.getInstance().getConfig(ConfigType.CHAT).getBoolean("chat-from-bukkit-bridge",false))
            return;
        e.setCancelled(true);
        ((ChatManager)tab.getFeatureManager().getFeature("&aChat&r")).onChat(tab.getPlayer(((ProxiedPlayer)e.getSender()).getUniqueId()),e.getMessage());
    }

    @EventHandler
    public void onMessageReceived(PluginMessageEvent e) {
        if (!e.getTag().equalsIgnoreCase("tabadditions:channel")) return;
        ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());
        String subChannel = in.readUTF();
        TabAPI tab = TabAPI.getInstance();
        TabPlayer p = tab.getPlayer(((ProxiedPlayer) e.getReceiver()).getUniqueId());
        if (subChannel.equalsIgnoreCase("Chat")) {
            String msg = in.readUTF();
            if (tab.getFeatureManager().isFeatureEnabled("&aChat&r"))
                ((ChatManager)tab.getFeatureManager().getFeature("&aChat&r")).onChat(p,msg);
            return;
        }
        if (subChannel.equalsIgnoreCase("PlaceholderAPI")) {
            String type = in.readUTF().toLowerCase();
            String value = "";
            String result = "";
            switch (type) {
                case "scoreboard_visible":
                    result = ""+tab.getScoreboardManager().hasScoreboardVisible(p);
                    break;
                case "bossbar_visible":
                    result = ""+tab.getBossBarManager().hasBossBarVisible(p);
                    break;
                case "ntpreview":
                    result = ""+p.isPreviewingNametag();
                    break;
                case "replace":
                    String output = TABAdditions.getInstance().parsePlaceholders(value,p,null);
                    Map<Object, String> replacements = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("placeholder-output-replacements." + value);
                    result =  tab.getPlaceholderManager().findReplacement(replacements, output).toString().replace("%value%", output);
                    break;
                case "placeholder":
                    value = in.readUTF();
                    result = TABAdditions.getInstance().parsePlaceholders(value,p,null);
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
