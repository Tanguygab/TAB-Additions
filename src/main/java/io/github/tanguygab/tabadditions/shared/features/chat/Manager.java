package io.github.tanguygab.tabadditions.shared.features.chat;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.shared.TAB;

public abstract class Manager {

    protected final TABAdditions plugin;
    protected final TAB tab;
    protected final ChatManager cm;

    public Manager(ChatManager chatManager) {
        plugin = TABAdditions.getInstance();
        tab = TAB.getInstance();
        cm = chatManager;
    }

}
