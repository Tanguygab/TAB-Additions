package io.github.tanguygab.tabadditions.shared.features.chat

import me.neznamy.tab.shared.platform.TabPlayer
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class MsgManager(
    chat: Chat,
    private val senderOutput: String,
    private val viewerOutput: String,
    private val cooldown: Double,
    private val aliases: List<String>,
    private val msgSelf: Boolean,
    toggleCmd: Boolean,
    private val replyCmd: Boolean,
    private val saveLastSenderForReply: Boolean
) : ChatManager(chat, toggleCmd, "msg-off", "togglemsg", "chat-pm") {

    private val replies = mutableMapOf<String, String>()
    private val msgCooldown = mutableMapOf<TabPlayer, LocalDateTime>()

    init {
        setToggleCmdMsgs(translation.pmOn, translation.pmOff)

        plugin.platform.registerCommand("msg", *aliases.toTypedArray())
        if (replyCmd) plugin.platform.registerCommand("reply", "r")
    }

    fun setCooldown(player: TabPlayer) {
        if (cooldown != .0 && !player.hasPermission("tabadditions.chat.bypass.cooldown"))
            msgCooldown[player] = LocalDateTime.now()
    }

    fun isOnCooldown(player: TabPlayer) = msgCooldown.containsKey(player) && ChronoUnit.SECONDS.between(
        msgCooldown[player],
        LocalDateTime.now()
    ) < cooldown

    fun getCooldown(player: TabPlayer) = cooldown - ChronoUnit.SECONDS.between(msgCooldown[player], LocalDateTime.now())

    fun isMsgCmd(command: String, exact: Boolean): Boolean {
        return command == "/msg"
                || command.substring(1) in aliases
                || !exact
                && (command.startsWith("/msg ") || command.split(" ")[0].substring(1) in aliases)
    }

    fun isReplyCmd(command: String, exact: Boolean): Boolean {
        return command == "/reply"
                || command == "/r"
                || !exact
                && (command.startsWith("/reply ") || command.startsWith("/r "))
    }

    override fun onCommand(sender: TabPlayer, command: String): Boolean {
        if (isReplyCmd(command, false) || isMsgCmd(command, false)) {
            if (!replyCmd && (isReplyCmd(command, true) || isReplyCmd(command, false))) return false
            if (chat.isMuted(sender)) return true
            if (isOnCooldown(sender)) {
                sender.sendMessage(translation.getPmCooldown(getCooldown(sender)))
                return true
            }
            setCooldown(sender)

            val args = command.split(" ")
            val player: String
            val msg: String
            if (isReplyCmd(command, false)) {
                player = replies[sender.name]!!
                msg = if (args.size > 1) command.substringAfter(args[0] + " ") else ""
            } else {
                player = (if (args.size > 1) args[1] else null)!!
                msg = if (args.size > 2) command.substringAfter(args[0] + " $player ") else ""
            }

            onMsgCommand(sender, player, msg, isReplyCmd(command, false))
            return true
        }
        return command == "/togglemsg" && toggleCmd(sender)
    }

    private fun onMsgCommand(sender: TabPlayer, player: String?, msg: String, reply: Boolean) {
        if (player == null) {
            sender.sendMessage(if (reply) translation.noPlayerToReplyTo else translation.providePlayer)
            return
        }
        if (msg.isEmpty()) {
            sender.sendMessage(translation.pmEmpty)
            return
        }
        val receiver = plugin.getPlayer(player)
        if (receiver == null || receiver.isVanished && !sender.hasPermission("tab.seevanished")) {
            sender.sendMessage(translation.getPlayerNotFound(player))
            return
        }
        if (!msgSelf && sender === receiver) {
            sender.sendMessage(translation.cantPmSelf)
            return
        }
        if (!sender.hasPermission("tabadditions.chat.bypass.togglemsg") && hasCmdToggled(receiver)) {
            sender.sendMessage(translation.hasPmOff)
            return
        }
        if (chat.isIgnored(sender, receiver)) {
            sender.sendMessage(translation.isIgnored)
            return
        }
        chat.sendMessage(sender, chat.createMessage(sender, receiver, msg, senderOutput))
        chat.sendMessage(receiver, chat.createMessage(sender, receiver, msg, viewerOutput))
        replies[sender.name] = receiver.name
        if (saveLastSenderForReply) replies[receiver.name] = sender.name

        chat.socialSpyManager?.process(sender, receiver, msg, "msg")
    }
}
