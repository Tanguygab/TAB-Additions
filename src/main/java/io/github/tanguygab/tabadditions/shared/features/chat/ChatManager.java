package io.github.tanguygab.tabadditions.shared.features.chat;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import lombok.AllArgsConstructor;
import me.neznamy.tab.shared.TAB;

@AllArgsConstructor
public abstract class ChatManager {

    protected final TABAdditions plugin = TABAdditions.getInstance();
    protected final TAB tab = TAB.getInstance();
    protected final Chat chat;

}
