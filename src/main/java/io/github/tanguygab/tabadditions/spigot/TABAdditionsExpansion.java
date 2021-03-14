package io.github.tanguygab.tabadditions.spigot;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.features.rfps.RFPManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TABAdditionsExpansion extends PlaceholderExpansion {

    private final Plugin plugin;

    public TABAdditionsExpansion(Plugin plugin){
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "tabadditions";
    }

    @Override
    public List<String> getPlaceholders() {
        return new ArrayList<>(Arrays.asList("%tabadditions_tag_visible%", "%tabadditions_fakeplayers_amount%"));
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
        if (identifier.equals("fakeplayers_amount") && TABAdditions.getInstance().rfpEnabled)
            return ((RFPManager)TAB.getInstance().getFeatureManager().getFeature("Real Fake Players")).getRFPS().size()+"";


        return "null";
    }

}
