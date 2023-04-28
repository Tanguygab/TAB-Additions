package io.github.tanguygab.tabadditions.shared.features.chat;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.TranslationFile;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabPlayer;

import java.util.List;
import java.util.UUID;

public abstract class ChatManager {

    protected final TABAdditions plugin = TABAdditions.getInstance();
    protected final TAB tab = TAB.getInstance();
    protected final TranslationFile translation = plugin.getTranslation();
    protected final Chat chat;

    private final String data;
    protected final boolean toggleCmd;
    protected final List<UUID> toggled;
    public ChatManager(Chat chat, boolean toggleCmd, String data, String cmd, String placeholder) {
        this.chat = chat;
        this.toggleCmd = toggleCmd;
        this.data = data;
        toggled = ChatUtils.registerToggleCmd(toggleCmd,data,cmd,placeholder,p->hasCmdToggled((TabPlayer)p) ? "Off" : "On");
    }

    public void unload() {
        plugin.unloadData(data,toggled,toggleCmd);
    }

    protected boolean hasCmdToggled(TabPlayer p) {
        return p != null && toggled.contains(p.getUniqueId());
    }

    public abstract boolean onCommand(TabPlayer sender, String command);

}
