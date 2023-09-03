package io.github.tanguygab.tabadditions.shared.features.chat;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import io.github.tanguygab.tabadditions.shared.TranslationFile;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholderImpl;
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
    protected List<UUID> toggled;
    private PlayerPlaceholderImpl placeholder;
    private boolean invertPlaceholder = false;
    private String toggledOn;
    private String toggledOff;

    public ChatManager(Chat chat) {
        this(chat,false,null,null,null);
    }

    public ChatManager(Chat chat, boolean toggleCmd, String data, String cmd, String placeholder) {
        this.chat = chat;
        this.toggleCmd = toggleCmd;
        this.data = data;
        if (!toggleCmd) return;
        plugin.getPlatform().registerCommand(cmd);
        this.placeholder = tab.getPlaceholderManager().registerPlayerPlaceholder("%"+placeholder+"%",-1,p->hasCmdToggled((TabPlayer)p) ? "Off" : "On");
        toggled = plugin.loadData(data,true);
    }

    public void unload() {
        plugin.unloadData(data,toggled,toggleCmd);
    }

    protected void setToggleCmdMsgs(String toggledOn, String toggledOff) {
        this.toggledOn = toggledOn;
        this.toggledOff = toggledOff;
    }
    protected void invertToggleCmdPlaceholder() {
        invertPlaceholder = true;
    }
    protected boolean hasCmdToggled(TabPlayer p) {
        return p != null && toggled.contains(p.getUniqueId());
    }

    public boolean toggleCmd(TabPlayer player) {
        return plugin.toggleCmd(toggleCmd,player,toggled,placeholder,toggledOn,toggledOff,invertPlaceholder);
    }

    public abstract boolean onCommand(TabPlayer sender, String command);

}
