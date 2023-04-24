package io.github.tanguygab.tabadditions.shared.features.chat;

import lombok.Getter;
import me.neznamy.tab.shared.features.types.CommandListener;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.features.types.UnLoadable;
import me.neznamy.tab.shared.platform.TabPlayer;

public class Chat extends TabFeature implements UnLoadable, JoinListener, CommandListener {

    @Getter private final String featureName = "Chat";

    @Override
    public void unload() {

    }

    @Override
    public void onJoin(TabPlayer tabPlayer) {

    }

    @Override
    public boolean onCommand(TabPlayer tabPlayer, String s) {
        return false;
    }

    public void onChat(TabPlayer sender, String message) {

    }
}
