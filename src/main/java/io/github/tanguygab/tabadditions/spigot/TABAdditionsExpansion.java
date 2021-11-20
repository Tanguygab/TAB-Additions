package io.github.tanguygab.tabadditions.spigot;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatManager;
import io.github.tanguygab.tabadditions.shared.features.rfps.RFPManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TABAdditionsExpansion extends PlaceholderExpansion {

    private final Plugin plugin;
    private final TabAPI tab;

    public TABAdditionsExpansion(Plugin plugin) {
        this.plugin = plugin;
        tab = TabAPI.getInstance();
    }

    @Override
    public String getIdentifier() {
        return "tabadditions";
    }

    @Override
    public List<String> getPlaceholders() {
        return new ArrayList<>(Arrays.asList(
                "%tabadditions_tag_visible%",
                "%tabadditions_fakeplayers_amount%",
                "%tabadditions_chat_mentions%",
                "%tabadditions_chat_messages%",
                "%tabadditions_chat_socialspy%"
        ));
    }

    @Override
    public String getAuthor() {
        return "Tanguygab";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier){

        if (identifier.equals("fakeplayers_amount") && TABAdditions.getInstance().rfpEnabled)
            return ((RFPManager)tab.getFeatureManager().getFeature("Real Fake Players")).getRFPS().size()+"";

        if (player == null) return "";
        TabPlayer p = tab.getPlayer(player.getUniqueId());
        if (p == null) return "";

        if (identifier.equals("tag_visible")) return !TabAPI.getInstance().getTeamManager().hasHiddenNametag(p)+"";

        if (identifier.startsWith("chat_")) {
            ChatManager cm = (ChatManager) TabAPI.getInstance().getFeatureManager().getFeature("Chat");
            if (identifier.equals("chat_mentions"))
                return cm.mentionDisabled.contains(p.getName().toLowerCase()) ? "Off" : "On";
            if (identifier.equals("chat_messages"))
                return TabAPI.getInstance().getPlayerCache().getStringList("togglemsg").contains(p.getName().toLowerCase()) ? "Off" : "On";
            if (identifier.equals("chat_socialspy"))
                return cm.spies.contains(p.getName().toLowerCase()) ? "On" : "Off";
        }

        return "";
    }

}
