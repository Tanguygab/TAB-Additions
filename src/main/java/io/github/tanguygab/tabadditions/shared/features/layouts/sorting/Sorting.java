package io.github.tanguygab.tabadditions.shared.features.layouts.sorting;

import io.github.tanguygab.tabadditions.shared.SharedTA;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.Configs;

import java.util.*;

// (Almost) Everything related to sorting was made by NEZNAMY, I litteraly copy pasted it while editing things and I feel bad ;-;
// https://github.com/NEZNAMY/TAB/blob/master/src/main/java/me/neznamy/tab/shared/features/sorting/Sorting.java

public class Sorting {

    private final List<TabPlayer> players;
    private final String layoutname;
    public List<SortingType> types = new ArrayList<>();
    private boolean caseSensitiveSorting = true;

    public Sorting(Map<String,Object> slot,List<TabPlayer> players, String layoutname) {
        this.players = players;
        this.layoutname = layoutname;

        if (slot.get("case-sentitive") != null) caseSensitiveSorting = (boolean) slot.get("case-sensitive");

        if (slot.containsKey("types")) {
            List<String> slottypes = (List<String>) slot.get("types");
            compile(slottypes);
        }
        else types.add(new Groups(""));

        SharedTA.platform.AsyncTask(()->{
            List<TabPlayer> players1 = players;
            for (TabPlayer p : players1) {
                if (!p.isLoaded()) continue;
                String newPos = getPosition(p);
                p.setProperty("Layout-Sorting-"+layoutname,newPos);
            }
        },1000);
    }

    private void compile(List<String> slottypes){
        if (slottypes.isEmpty()) {
            types.add(new Groups(""));
            return;
        }
        for (String type : slottypes) {
            if (type.equals("GROUPS")) types.add(new Groups(""));
            if (type.equals("GROUP_PERMISSIONS")) types.add(new GroupPermission(""));
            if (type.startsWith("PLACEHOLDER_A_TO_Z_")) types.add(new PlaceholderAtoZ(type.replace("PLACEHOLDER_A_TO_Z_","")));
            if (type.startsWith("PLACEHOLDER_Z_TO_A_")) types.add(new PlaceholderZtoA(type.replace("PLACEHOLDER_Z_TO_A_","")));
            if (type.startsWith("PLACEHOLDER_LOW_TO_HIGH_")) types.add(new PlaceholderLowToHigh(type.replace("PLACEHOLDER_LOW_TO_HIGH_","")));
            if (type.startsWith("PLACEHOLDER_HIGH_TO_LOW_")) types.add(new PlaceholderHighToLow(type.replace("PLACEHOLDER_HIGH_TO_LOW_","")));
        }
    }


    public String getPosition(TabPlayer p) {
        String position = "";
        for (SortingType type : types)
            position = position + type.getChars(p);
        return position;
    }

    public static LinkedHashMap<String, String> loadSortingList() {
        LinkedHashMap<String, String> sortedGroups = new LinkedHashMap<>();
        int index = 1;
        List<String> configList = TAB.getInstance().getConfiguration().config.getStringList("group-sorting-priority-list", Arrays.asList("Owner", "Admin", "Mod", "Helper", "Builder", "Premium", "Player", "default"));
        int charCount = String.valueOf(configList.size()).length(); //1 char for <10 groups, 2 chars for <100 etc
        for (Object group : configList){
            String sort = index+"";
            while (sort.length() < charCount) {
                sort = "0" + sort;
            }
            for (String group0 : String.valueOf(group).toLowerCase().split(" ")) {
                sortedGroups.put(group0, sort);
            }
            index++;
        }
        return sortedGroups;
    }
}