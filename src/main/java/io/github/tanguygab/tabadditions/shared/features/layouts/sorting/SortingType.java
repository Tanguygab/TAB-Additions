package io.github.tanguygab.tabadditions.shared.features.layouts.sorting;

import java.util.List;

import io.github.tanguygab.tabadditions.shared.SharedTA;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

public abstract class SortingType {

	protected final int DEFAULT_NUMBER = 500000000;
	protected String sortingPlaceholder;
	private final List<String> usedPlaceholders;
	
	public SortingType(String sortingPlaceholder){
		this.sortingPlaceholder = sortingPlaceholder;
		usedPlaceholders = TAB.getInstance().getPlaceholderManager().getUsedPlaceholderIdentifiersRecursive(sortingPlaceholder);
	}
	
	protected String setPlaceholders(String string, TabPlayer p) {
		return SharedTA.parsePlaceholders(string,p);
	}
	
	public abstract String getChars(TabPlayer p);
}
