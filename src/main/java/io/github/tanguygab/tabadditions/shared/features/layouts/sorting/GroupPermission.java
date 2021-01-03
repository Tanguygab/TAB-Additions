package io.github.tanguygab.tabadditions.shared.features.layouts.sorting;

import me.neznamy.tab.api.TabPlayer;
import java.util.LinkedHashMap;

public class GroupPermission extends SortingType {

    private final LinkedHashMap<String, String> sortedGroups;

    public GroupPermission(String sortingPlaceholder) {
        super(sortingPlaceholder);
        sortedGroups = Sorting.loadSortingList();
    }

    @Override
    public String getChars(TabPlayer p) {
        String chars = null;
        for (String localgroup : sortedGroups.keySet()) {
            if (p.hasPermission("tab.sort." + localgroup)) {
                chars = sortedGroups.get(localgroup.toLowerCase());
                break;
            }
        }
        if (chars == null) chars = "9";
        return chars;
    }

    @Override
    public String toString() {
        return "GROUP_PERMISSIONS";
    }
}