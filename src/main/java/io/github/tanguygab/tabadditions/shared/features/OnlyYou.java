package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class OnlyYou implements JoinEventListener {

    private final TabFeature feature;

    public OnlyYou(TabFeature feature) {
        feature.setDisplayName("&aOnly You");
        this.feature = feature;
    }

    @Override
    public void onJoin(TabPlayer tabPlayer) {
        Player p = Bukkit.getServer().getPlayer(tabPlayer.getUniqueId());
        for (TabPlayer p2 : TAB.getInstance().getPlayers())
            p.hidePlayer((Plugin) TABAdditions.getInstance().getPlugin(), Bukkit.getServer().getPlayer(p2.getUniqueId()));
    }

    @Override
    public TabFeature getFeatureType() {
        return feature;
    }
}
