package io.github.tanguygab.tabadditions.shared.features.chat;

import io.github.tanguygab.tabadditions.shared.Platform;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.PlaceholderManager;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MsgManager extends Manager {

    private final String senderOutput;
    private final String viewerOutput;
    private final long cooldown;
    private final List<String> aliases;
    private final boolean msgSelf;
    private final boolean toggleMsgCmd;
    private final boolean replyCmd;
    private final boolean saveLastSenderForReply;

    private final Map<TabPlayer, String> replies = new HashMap<>();
    private final Map<TabPlayer, LocalDateTime> msgCooldown = new HashMap<>();

    public MsgManager(ChatManager cm, String senderOutput, String viewerOutput, long cooldown, List<String> aliases, boolean msgSelf, boolean toggleMsgCmd, boolean replyCmd, boolean saveLastSenderForReply) {
        super(cm);
        this.senderOutput = senderOutput;
        this.viewerOutput = viewerOutput;
        this.cooldown = cooldown;
        this.aliases = aliases;
        this.msgSelf = msgSelf;
        this.toggleMsgCmd = toggleMsgCmd;
        this.replyCmd = replyCmd;
        this.saveLastSenderForReply = saveLastSenderForReply;

        Platform platform = instance.getPlatform();
        platform.registerCommand("msg",true,aliases.toArray(new String[]{}));
        platform.registerCommand("reply",replyCmd,"r");
        platform.registerCommand("togglemsg",toggleMsgCmd);

        PlaceholderManager pm = tab.getPlaceholderManager();
        pm.registerPlayerPlaceholder("%chat-messages%",1000,p->tab.getPlayerCache().getStringList("togglemsg").contains(p.getName().toLowerCase()) ? "Off" : "On");
    }

    public void setCooldown(TabPlayer p) {
        if (this.cooldown != 0 && !p.hasPermission("tabadditions.chat.bypass.cooldown"))
            msgCooldown.put(p,LocalDateTime.now());
    }
    public boolean isOnCooldown(TabPlayer p) {
        return msgCooldown.containsKey(p) && ChronoUnit.SECONDS.between(msgCooldown.get(p), LocalDateTime.now()) < cooldown;
    }
    public long getCooldown(TabPlayer p) {
        return cooldown-ChronoUnit.SECONDS.between(msgCooldown.get(p), LocalDateTime.now());
    }

    public String getSenderOutput() {
        return senderOutput;
    }

    public String getViewerOutput() {
        return viewerOutput;
    }

    public boolean isAlias(String cmd) {
        return aliases.contains(cmd.toLowerCase());
    }

    public boolean isToggleMsgCmdEnabled() {
        return toggleMsgCmd;
    }

    public boolean isReplyCmdEnabled() {
        return replyCmd;
    }

    public boolean canMsgSelf() {
        return msgSelf;
    }

    public void setLastReply(TabPlayer p, String name) {
        replies.put(p,name);
    }
    public String getLastReply(TabPlayer p) {
        return replies.getOrDefault(p, "");
    }
    public boolean saveLastSenderForReply() {
        return saveLastSenderForReply;
    }
}
