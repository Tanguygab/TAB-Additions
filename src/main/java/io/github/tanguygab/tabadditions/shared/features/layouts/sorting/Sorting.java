package io.github.tanguygab.tabadditions.shared.features.layouts.sorting;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.UsageType;

import java.util.*;
import java.util.function.Function;

// (Almost) Everything related to sorting was made by NEZNAMY, I litteraly copy pasted it while editing things and I feel bad ;-;
// https://github.com/NEZNAMY/TAB/tree/v3/src/main/java/me/neznamy/tab/shared/features/sorting/Sorting.java

public class Sorting {

    //map of all registered sorting types
    private final Map<String, Function<String, SortingType>> types = new HashMap<>();

    //if sorting is case senstitive or not
    private boolean caseSensitiveSorting = true;

    //active sorting types
    private List<SortingType> usedSortingTypes;

    private final List<TabPlayer> players;
    private final String layoutname;
    private final TabFeature feature;

    public Sorting(Map<String,Object> slot,List<TabPlayer> players, String layoutname) {
        this.players = players;
        this.layoutname = layoutname;
        feature = TabAPI.getInstance().getFeatureManager().getFeature("&aTAB+ Layout&r");

        caseSensitiveSorting = Boolean.parseBoolean(slot.get("case-sentitive")+"");
        types.put("GROUPS", Groups::new);
        types.put("PERMISSIONS", Permissions::new);
        types.put("PLACEHOLDER",  Placeholder::new);
        types.put("PLACEHOLDER_A_TO_Z", (options) -> new PlaceholderAtoZ(options));
        types.put("PLACEHOLDER_Z_TO_A", (options) -> new PlaceholderZtoA(options));
        types.put("PLACEHOLDER_LOW_TO_HIGH", (options) -> new PlaceholderLowToHigh(options));
        types.put("PLACEHOLDER_HIGH_TO_LOW", (options) -> new PlaceholderHighToLow(options));

        if (slot.get("types") instanceof List)
            usedSortingTypes = compile((List<String>) slot.get("types"));
        else usedSortingTypes = compile(TAB.getInstance().getConfiguration().getConfig().getStringList("scoreboard-teams.sorting-types", new ArrayList<>()));


        TAB.getInstance().getCPUManager().runTaskLater(1000,"handling TAB+ Layout Sorting", feature, UsageType.REFRESHING,()->{
            List<TabPlayer> players1 = new ArrayList<>(players);
            for (TabPlayer p : players1) {
                if (!p.isLoaded()) continue;
                String newPos = getPosition(p);
                p.setProperty(feature,"TAB+-Layout-Sorting-"+layoutname,newPos);
            }
        });
    }

    private List<SortingType> compile(List<String> options){
        List<SortingType> list = new ArrayList<>();
        for (String element : options) {
            String[] arr = element.split(":");
            if (types.containsKey(arr[0].toUpperCase())) {
                SortingType type = types.get(arr[0].toUpperCase()).apply(arr.length == 1 ? "" : arr[1]);
                list.add(type);
            }
        }
        return list;
    }

    public String getPosition(TabPlayer p) {
        String position = "";
        for (SortingType type : getSorting())
            position = position + type.getChars(p);
        return position;
    }

    public List<SortingType> getSorting() {
        return usedSortingTypes;
    }
}