package io.github.tanguygab.tabadditions.shared.features.chat.mentions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.kyori.adventure.sound.Sound;

@AllArgsConstructor
public class CustomMention {

    private final String input;
    private final String output;
    private final Condition condition;
    @Getter private final Sound sound;

    protected boolean matches(String msg) {
        return msg.matches(input);
    }

    protected String replace(String msg) {
        return msg.replaceAll(input,output);
    }

    public boolean isConditionMet(TabPlayer player) {
        return condition == null || condition.isMet(player);
    }

}
