package io.github.tanguygab.tabadditions.shared.features;

import io.github.tanguygab.tabadditions.shared.ConfigType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.commands.TitleCmd;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Title extends TabFeature {

    public Title() {
        super("&aTitle&r");
    }

    @Override
    public void onJoin(TabPlayer p) {
        p.loadPropertyFromConfig(this,"title");
        Map<String,Object> tSection = TABAdditions.getInstance().getConfig(ConfigType.TITLE).getConfigurationSection("titles." + p.getProperty("title").get());
        if (tSection != null && tSection.size() >= 5) {
            List<Object> titleProperties = new ArrayList<>();
            for (Object property : tSection.keySet())
                titleProperties.add(tSection.get(property));
            new TitleCmd(p.getName(), new String[]{}, titleProperties);
        }
    }

    public List<String> getLists() {
        List<String> list = new ArrayList<>();
        for (Object key : TABAdditions.getInstance().getConfig(ConfigType.TITLE).getConfigurationSection("titles").keySet())
            list.add(key.toString());
        return list;
    }
}
