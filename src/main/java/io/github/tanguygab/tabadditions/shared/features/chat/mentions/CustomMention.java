package io.github.tanguygab.tabadditions.shared.features.chat.mentions;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

public class CustomMention {

    private final String input;
    private final String output;
    private final Condition condition;
    private final String sound;

    public CustomMention(String input, String output, Condition condition, String sound) {
        this.input = input;
        this.output = output;
        this.condition = condition;
        this.sound = sound;
    }

    protected boolean matches(String msg) {
        return msg.matches(input);
    }

    protected String replace(String msg) {
        return msg.replaceAll(input,output);
    }

    public boolean isConditionMet(TabPlayer player) {
        return condition == null || condition.isMet(player);
    }

    protected String getSound() {
        return sound;
    }

}
