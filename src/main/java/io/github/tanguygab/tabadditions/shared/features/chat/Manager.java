package io.github.tanguygab.tabadditions.shared.features.chat;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabAPI;

public abstract class Manager {

    protected final TABAdditions instance;
    protected final TabAPI tab;
    protected final ChatManager cm;

    public Manager(ChatManager chatManager) {
        instance = TABAdditions.getInstance();
        tab = TabAPI.getInstance();
        cm = chatManager;
    }

}
