package io.github.tanguygab.tabadditions.shared.features.chat

import me.neznamy.tab.shared.platform.TabPlayer

class SocialSpyManager(
    chat: Chat,
    private val msgSpy: Boolean,
    private val msgOutput: String?,
    private val channelSpy: Boolean,
    private val channelOutput: String,
    private val viewConditionSpy: Boolean,
    private val viewConditionOutput: String
) : ChatManager(chat, true, "socialspy", "socialspy", "socialspy") {

    init {
        setToggleCmdMsgs(translation.socialSpyOff, translation.socialSpyOn)
        invertToggleCmdPlaceholder()
    }

    fun isSpying(sender: TabPlayer, viewer: TabPlayer, senderFormat: ChatFormat): String {
        if (!hasCmdToggled(viewer) || !viewer.hasPermission("tabadditions.chat.socialspy")) {
            toggled.remove(viewer.uniqueId)
            return ""
        }
        if (channelSpy && senderFormat.channel != chat.getFormat(viewer)?.channel) return "channel"
        if (viewConditionSpy && !senderFormat.isViewConditionMet(sender, viewer)) return "view-condition"
        return ""
    }

    fun process(sender: TabPlayer, viewer: TabPlayer?, message: String, type: String) {
        val output = when (type) {
            "msg" if msgSpy -> msgOutput
            "channel" if channelSpy -> channelOutput.replace("%channel%", chat.getFormat(sender)?.channel ?: "")
            "view-condition" if viewConditionSpy -> viewConditionOutput.replace(
                "%view-condition%",
                chat.getFormat(sender)?.viewCondition?.name ?: ""
            )
            else -> null
        }
        if (output == null) return
        if (type != "msg") {
            chat.sendMessage(viewer, chat.createMessage(sender, viewer, message, output))
            return
        }
        toggled.removeIf { tab.getPlayer(it)?.hasPermission("tabadditions.chat.socialspy") == true}
        toggled.forEach {
            val spy = tab.getPlayer(it)
            if (spy == null || spy === sender) return@forEach
            chat.sendMessage(spy, chat.createMessage(sender, viewer, message, output))
        }
    }

    override fun onCommand(sender: TabPlayer, command: String) {
        if (sender.hasPermission("tabadditions.chat.socialspy"))
            toggleCmd(sender)
    }
}
