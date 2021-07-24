package io.github.tanguygab.tabadditions.shared.features.layouts.sorting;

import java.util.List;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

public abstract class SortingType {

	protected final int DEFAULT_NUMBER = 500000000;
	protected String sortingPlaceholder;
	private final List<String> usedPlaceholders;
	
	public SortingType(String sortingPlaceholder){
		this.sortingPlaceholder = sortingPlaceholder;
		usedPlaceholders = TAB.getInstance().getPlaceholderManager().detectPlaceholders(sortingPlaceholder);
	}
	
	protected String setPlaceholders(String string, TabPlayer p) {
		return TABAdditions.getInstance().parsePlaceholders(string,p, TabAPI.getInstance().getFeatureManager().getFeature("&aTAB+ Layout&r"));
	}
	
	public abstract String getChars(TabPlayer p);
}
