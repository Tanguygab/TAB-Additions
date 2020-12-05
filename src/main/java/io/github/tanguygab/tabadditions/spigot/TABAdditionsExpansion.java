package io.github.tanguygab.tabadditions.spigot;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.api.TabPlayer;
import org.bukkit.entity.Player;

public class TABAdditionsExpansion extends PlaceholderExpansion {

    private final TABAdditionsSpigot plugin;

    public TABAdditionsExpansion(TABAdditionsSpigot plugin){
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "tabadditions";
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
        TabPlayer p = TABAPI.getPlayer(player.getUniqueId());
        if (p == null) return "";

        if (identifier.equals("tag_visible")) return !p.hasHiddenNametag()+"";

        return "null";
    }

}
