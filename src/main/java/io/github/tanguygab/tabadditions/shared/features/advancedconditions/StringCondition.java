package io.github.tanguygab.tabadditions.shared.features.advancedconditions;

import me.neznamy.tab.shared.platform.TabPlayer;

import java.util.function.BiFunction;

public class StringCondition extends SimpleCondition {

    private final BiFunction<String, String, Boolean> function;

    public StringCondition(String[] arr, BiFunction<String, String, Boolean> function) {
        super(arr);
        this.function = function;
    }

    @Override
    public boolean isMet(TabPlayer viewer, TabPlayer target) {
        return function.apply(parseLeftSide(viewer,target), parseRightSide(viewer,target));
    }
}
