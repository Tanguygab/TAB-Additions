package io.github.tanguygab.tabadditions.shared.features.chat;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

public class FormatCommand {

    private String command;
    private String name;
    private ChatFormat format;
    private Condition condition;
    private String prefix;

    public FormatCommand(String name, String command, ChatFormat format, Condition condition, String prefix) {
        this.name = name;
        this.command = command;
        this.format = format;
        this.condition = condition;
        this.prefix = prefix;
        TABAdditions.getInstance().getPlatform().registerCommand(command,true);
    }

    public String getCommand() {
        return command;
    }

    public String getName() {
        return name;
    }

    public ChatFormat getFormat() {
        return format;
    }

    public Condition getCondition() {
        return condition;
    }

    public String getPrefix() {
        return prefix;
    }
}
