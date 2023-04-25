package io.github.tanguygab.tabadditions.shared.features.chat.mentions;

import lombok.Getter;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

public class CustomMention {

    private final String input;
    private final String output;
    private final Condition condition;
    @Getter private final Sound sound;

    public CustomMention(String input, String output, Condition condition, String sound) {
        this.input = input;
        this.output = output;
        this.condition = condition;
        this.sound = Sound.sound(Key.key(sound),Sound.Source.MASTER,1,1);
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

}
