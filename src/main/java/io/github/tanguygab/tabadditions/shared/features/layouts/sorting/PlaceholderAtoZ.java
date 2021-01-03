package io.github.tanguygab.tabadditions.shared.features.layouts.sorting;

import me.neznamy.tab.api.TabPlayer;

public class PlaceholderAtoZ extends SortingType {

	public PlaceholderAtoZ(String sortingPlaceholder) {
		super(sortingPlaceholder);
	}

	@Override
	public String getChars(TabPlayer p) {
		return setPlaceholders(sortingPlaceholder, p);
	}
	
	@Override
	public String toString() {
		return "PLACEHOLDER_A_TO_Z";
	}
}