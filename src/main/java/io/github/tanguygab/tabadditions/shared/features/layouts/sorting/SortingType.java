package io.github.tanguygab.tabadditions.shared.features.layouts.sorting;

import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.features.PlaceholderManager;

public abstract class SortingType {

	protected final int DEFAULT_NUMBER = 500000000;
	protected String sortingPlaceholder;
	private List<String> usedPlaceholders;
	
	public SortingType(String sortingPlaceholder){
		this.sortingPlaceholder = sortingPlaceholder;
		usedPlaceholders = PlaceholderManager.getUsedPlaceholderIdentifiersRecursive(sortingPlaceholder);
	}
	
	protected String setPlaceholders(String string, TabPlayer p) {
		return Shared.platform.replaceAllPlaceholders(string,p);
	}
	
	public abstract String getChars(TabPlayer p);
}
