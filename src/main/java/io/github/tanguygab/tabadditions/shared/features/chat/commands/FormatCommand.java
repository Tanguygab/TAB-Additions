package io.github.tanguygab.tabadditions.shared.features.chat.commands;

import io.github.tanguygab.tabadditions.shared.features.advancedconditions.AdvancedConditions;
import io.github.tanguygab.tabadditions.shared.features.chat.ChatFormat;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
public class FormatCommand extends ChatFormat {

    @Accessors(fluent = true)
    private final boolean saveOnReload;
    private final String prefix;

    public FormatCommand(String name, String displayName, AdvancedConditions condition, AdvancedConditions viewCondition, String channel, String text, boolean saveOnReload, String prefix) {
        super(name, displayName, condition, viewCondition, channel, text.replace("%chat-format%",displayName));
        this.saveOnReload = saveOnReload;
        this.prefix = prefix;
    }

}