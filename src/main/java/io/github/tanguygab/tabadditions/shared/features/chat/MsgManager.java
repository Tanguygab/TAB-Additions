package io.github.tanguygab.tabadditions.shared.features.chat;

import io.github.tanguygab.tabadditions.shared.Platform;
import me.neznamy.tab.shared.platform.TabPlayer;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class MsgManager extends ChatManager {

    private final String senderOutput;
    private final String viewerOutput;
    private final double cooldown;
    private final boolean msgSelf;
    private final boolean replyCmd;
    private final boolean saveLastSenderForReply;

    private final Map<String, String> replies = new HashMap<>();
    private final Map<TabPlayer, LocalDateTime> msgCooldown = new HashMap<>();

    public MsgManager(Chat chat, String senderOutput, String viewerOutput, double cooldown, List<String> aliases, boolean msgSelf, boolean toggleCmd, boolean replyCmd, boolean saveLastSenderForReply) {
        super(chat,toggleCmd,"msg-off","togglemsg","chat-pm");
        this.senderOutput = senderOutput;
        this.viewerOutput = viewerOutput;
        this.cooldown = cooldown;
        this.msgSelf = msgSelf;
        this.replyCmd = replyCmd;
        this.saveLastSenderForReply = saveLastSenderForReply;

        Platform platform = plugin.getPlatform();
        platform.registerCommand("msg",aliases.toArray(new String[]{}));
        if (replyCmd) platform.registerCommand("reply","r");
    }

    public void setCooldown(TabPlayer p) {
        if (this.cooldown != 0 && !p.hasPermission("tabadditions.chat.bypass.cooldown"))
            msgCooldown.put(p,LocalDateTime.now());
    }
    public boolean isOnCooldown(TabPlayer p) {
        return msgCooldown.containsKey(p) && ChronoUnit.SECONDS.between(msgCooldown.get(p), LocalDateTime.now()) < cooldown;
    }
    public double getCooldown(TabPlayer p) {
        return cooldown-ChronoUnit.SECONDS.between(msgCooldown.get(p), LocalDateTime.now());
    }

    @Override
    public boolean onCommand(TabPlayer sender, String command) {
        if (command.startsWith("/reply") || command.startsWith("/r") || command.startsWith("/msg")) {
            if (replyCmd && (command.startsWith("/reply") || command.startsWith("/r")))
                return false;
            if (chat.isMuted(sender)) return true;
            if (isOnCooldown(sender)) {
                sender.sendMessage(translation.getPmCooldown(getCooldown(sender)), true);
                return true;
            }
            setCooldown(sender);

            if (command.equals("/msg")) {
                sender.sendMessage(translation.providePlayer,true);
                return true;
            }
            if (command.equals("/reply") || command.equals("/r")) {
                sender.sendMessage(translation.pmEmpty, true);
                return true;
            }

            String player = null;
            if (command.startsWith("/reply ") || command.startsWith("/r "))
                player = replies.get(sender.getName());
            else if (command.contains(" ")) player = command.split(" ")[1];
            if (player == null) {
                sender.sendMessage(translation.providePlayer,true);
                return true;
            }

            String msg = command.substring(command.startsWith("/reply ") ? 7 : command.startsWith("/r ") ? 3 : command.startsWith("/msg "+player+" ") ? 6+player.length() : command.length());
            if (msg.equals("")) {
                sender.sendMessage(translation.pmEmpty, true);
                return true;
            }
            TabPlayer receiver = plugin.getPlayer(player);
            if (receiver == null) {
                sender.sendMessage(translation.getPlayerNotFound(player), true);
                return true;
            }
            if (!msgSelf && sender == receiver) {
                sender.sendMessage(translation.cantPmSelf, true);
                return true;
            }
            if (!sender.hasPermission("tabadditions.chat.bypass.togglemsg") && hasCmdToggled(receiver)) {
                sender.sendMessage(translation.hasPmOff, true);
                return true;
            }
            if (chat.isIgnored(sender,receiver)) {
                sender.sendMessage(translation.isIgnored, true);
                return true;
            }
            chat.sendMessage(sender,chat.createMessage(sender,receiver,msg,senderOutput));
            chat.sendMessage(receiver,chat.createMessage(sender,receiver,msg,viewerOutput));
            replies.put(sender.getName(),receiver.getName());
            if (saveLastSenderForReply) replies.put(receiver.getName(),sender.getName());

            if (chat.socialSpyManager != null) chat.socialSpyManager.process(sender,receiver,msg,"msg");
            return true;
        }
        return command.equals("/togglemsg") && plugin.toggleCmd(toggleCmd,sender,toggled,translation.pmOn,translation.pmOff);
    }
}
