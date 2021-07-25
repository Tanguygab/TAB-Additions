package io.github.tanguygab.tabadditions.shared.features.layouts.sorting;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;

/**
 * Sorting by a placeholder by values defined in list
 */
public class Placeholder extends SortingType {

    //map Value-Number where number is used in team name based on value
    private final LinkedHashMap<String, String> sortingMap;

    /**
     * Constructs new instance with given parameter
     * @param options - placeholders to sort by
     */
    public Placeholder(String options) {
        sortingMap = new LinkedHashMap<>();
        int index = 1;
        List<String> values = Arrays.asList(options.split(","));
        int charCount = String.valueOf(values.size()).length(); //1 char for <10 values, 2 chars for <100 etc
        for (String value : values) {
            String sort = String.valueOf(index);
            while (sort.length() < charCount) {
                sort = "0" + sort;
            }
            sortingMap.put(value.replace('&', '\u00a7'), sort);
            index++;
        }
    }

    @Override
    public String getChars(TabPlayer p) {
        String output = setPlaceholders(p);
        if (output.contains("&")) output = output.replace('&', '\u00a7');
        String sortingValue = sortingMap.get(output);
        if (sortingValue == null) sortingValue = String.valueOf(sortingMap.size()+1);
        return sortingValue;
    }

    @Override
    public String toString() {
        return "PLACEHOLDER_A_TO_Z";
    }
}
