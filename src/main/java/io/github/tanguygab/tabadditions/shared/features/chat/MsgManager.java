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
    private final List<String> aliases;

    private final Map<String, String> replies = new HashMap<>();
    private final Map<TabPlayer, LocalDateTime> msgCooldown = new HashMap<>();

    public MsgManager(Chat chat, String senderOutput, String viewerOutput, double cooldown, List<String> aliases, boolean msgSelf, boolean toggleCmd, boolean replyCmd, boolean saveLastSenderForReply) {
        super(chat,toggleCmd,"msg-off","togglemsg","chat-pm");
        setToggleCmdMsgs(translation.pmOn,translation.pmOff);
        this.senderOutput = senderOutput;
        this.viewerOutput = viewerOutput;
        this.cooldown = cooldown;
        this.msgSelf = msgSelf;
        this.replyCmd = replyCmd;
        this.saveLastSenderForReply = saveLastSenderForReply;
        this.aliases = aliases;

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
    public boolean isMsgCmd(String command, boolean exact) {
        return command.equals("/msg") || aliases.contains(command.substring(1))
                || (!exact && (command.startsWith("/msg ") || aliases.contains(command.split(" ")[0].substring(1))));
    }
    public boolean isReplyCmd(String command, boolean exact) {
        return command.equals("/reply") || command.equals("/r")
                || (!exact && (command.startsWith("/reply ") || command.startsWith("/r ")));
    }

    @Override
    public boolean onCommand(TabPlayer sender, String command) {
        if (isReplyCmd(command,false) || isMsgCmd(command,false)) {
            if (!replyCmd && (isReplyCmd(command,true) || isReplyCmd(command,false)))
                return false;
            if (chat.isMuted(sender)) return true;
            if (isOnCooldown(sender)) {
                sender.sendMessage(translation.getPmCooldown(getCooldown(sender)), true);
                return true;
            }
            setCooldown(sender);

            String[] args = command.split(" ");
            String player;
            String msg;
            if (isReplyCmd(command,false)) {
                player = replies.get(sender.getName());
                msg = args.length > 1 ? command.substring(args[0].length()+1) : "";
            } else {
                player = args.length > 1 ? args[1] : null;
                msg = args.length > 2 ? command.substring(args[0].length()+player.length()+2) : "";
            }

            onMsgCommand(sender,player,msg,isReplyCmd(command,false));
            return true;
        }
        return command.equals("/togglemsg") && toggleCmd(sender);
    }

    private void onMsgCommand(TabPlayer sender, String player, String msg, boolean reply) {
        if (player == null) {
            sender.sendMessage(reply ? translation.noPlayerToReplyTo : translation.providePlayer,true);
            return;
        }
        if (msg.isEmpty()) {
            sender.sendMessage(translation.pmEmpty, true);
            return;
        }
        TabPlayer receiver = plugin.getPlayer(player);
        if (receiver == null || receiver.isVanished() && !sender.hasPermission("tab.seevanished")) {
            sender.sendMessage(translation.getPlayerNotFound(player), true);
            return;
        }
        if (!msgSelf && sender == receiver) {
            sender.sendMessage(translation.cantPmSelf, true);
            return;
        }
        if (!sender.hasPermission("tabadditions.chat.bypass.togglemsg") && hasCmdToggled(receiver)) {
            sender.sendMessage(translation.hasPmOff, true);
            return;
        }
        if (chat.isIgnored(sender,receiver)) {
            sender.sendMessage(translation.isIgnored, true);
            return;
        }
        chat.sendMessage(sender,chat.createMessage(sender,receiver,msg,senderOutput));
        chat.sendMessage(receiver,chat.createMessage(sender,receiver,msg,viewerOutput));
        replies.put(sender.getName(),receiver.getName());
        if (saveLastSenderForReply) replies.put(receiver.getName(),sender.getName());

        if (chat.socialSpyManager != null) chat.socialSpyManager.process(sender,receiver,msg,"msg");
    }
}
