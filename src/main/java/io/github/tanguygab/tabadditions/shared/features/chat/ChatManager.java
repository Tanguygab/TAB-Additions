package io.github.tanguygab.tabadditions.shared.features.chat;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.TranslationFile;
import lombok.AllArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabPlayer;

import java.util.List;
import java.util.UUID;

public abstract class ChatManager {

    protected final TABAdditions plugin = TABAdditions.getInstance();
    protected final TAB tab = TAB.getInstance();
    protected final TranslationFile translation = plugin.getTranslation();
    protected final Chat chat;
    protected final boolean toggleCmd;
    protected final List<UUID> toggled;
    public ChatManager(Chat chat, boolean toggleCmd, String data, String cmd, String placeholder) {
        this.chat = chat;
        this.toggleCmd = toggleCmd;
        toggled = ChatUtils.registerToggleCmd(toggleCmd,data,cmd,placeholder,p->hasCmdToggled((TabPlayer)p) ? "Off" : "On");
    }

    protected boolean hasCmdToggled(TabPlayer p) {
        return toggled.contains(p.getUniqueId());
    }

    public abstract boolean onCommand(TabPlayer sender, String command);

}
