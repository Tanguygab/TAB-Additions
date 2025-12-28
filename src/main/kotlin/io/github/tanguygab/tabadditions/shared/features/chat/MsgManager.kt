package io.github.tanguygab.tabadditions.shared.features.chat

import me.neznamy.tab.shared.platform.TabPlayer
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class MsgManager(
    chat: Chat,
    private val senderOutput: String,
    private val viewerOutput: String,
    private val cooldown: Double,
    aliases: List<String>,
    private val msgSelf: Boolean,
    toggleCmd: Boolean,
    replyCmd: Boolean,
    private val saveLastSenderForReply: Boolean
) : ChatManager(chat, toggleCmd, "msg-off", "togglemsg", "chat-pm") {

    private val replies = mutableMapOf<String, String>()
    private val msgCooldown = mutableMapOf<TabPlayer, LocalDateTime>()

    init {
        setToggleCmdMsgs(translation.pmOn, translation.pmOff)

        aliases.plus("msg").forEach {
            tab.platform.registerCustomCommand(it) { sender, args ->
                onCommand(sender, args, false)
            }
        }
        if (replyCmd) listOf("reply", "r").forEach {
            tab.platform.registerCustomCommand(it) { sender, args ->
                onCommand(sender, args, true)
            }
        }
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

    override fun onCommand(sender: TabPlayer, command: String) { toggleCmd(sender) }

    // too tired to rework this better rn
    private fun onCommand(sender: TabPlayer, args: Array<String>, isReply: Boolean) {
        val player: String?
        val msg: String
        if (isReply) {
            player = replies[sender.name]
            msg = args.joinToString(" ")
        } else if (args.isNotEmpty()) {
            player = args[0]
            msg = args.copyOfRange(1, args.size).joinToString(" ")
        } else {
            player = null
            msg = ""
        }

        onMsgCommand(sender, player, msg, isReply)
    }

    private fun onMsgCommand(sender: TabPlayer, player: String?, msg: String, reply: Boolean) {
        if (chat.isMuted(sender)) return
        if (isOnCooldown(sender)) {
            sender.sendMessage(translation.getPmCooldown(getCooldown(sender)))
            return
        }
        setCooldown(sender)

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
