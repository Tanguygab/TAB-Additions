package io.github.tanguygab.tabadditions.spigot;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
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
                "%tabadditions_fakeplayers_amount%"
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

        if (player == null) return "";
        TabPlayer p = tab.getPlayer(player.getUniqueId());
        if (p == null) return "";

        if (identifier.equals("tag_visible")) return !TabAPI.getInstance().getTeamManager().hasHiddenNametag(p)+"";

        return "";
    }

}
