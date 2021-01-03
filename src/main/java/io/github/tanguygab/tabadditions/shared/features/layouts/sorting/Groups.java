package io.github.tanguygab.tabadditions.shared.features.layouts.sorting;

import me.neznamy.tab.api.TabPlayer;
import java.util.LinkedHashMap;

public class Groups extends SortingType {

    private final LinkedHashMap<String, String> sortedGroups;

    public Groups(String sortingPlaceholder) {
        super(sortingPlaceholder);
        sortedGroups = Sorting.loadSortingList();
    }

    @Override
    public String getChars(TabPlayer p) {
        String group = p.getGroup();
        String chars = sortedGroups.get(group.toLowerCase());
        if (chars == null) chars = "9";
        return chars;
    }

    @Override
    public String toString() {
        return "GROUPS";
    }
}