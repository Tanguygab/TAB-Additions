package io.github.tanguygab.tabadditions.shared.features.chat

import com.loohp.interactivechat.api.InteractiveChatAPI
import io.github.tanguygab.tabadditions.shared.TABAdditions
import io.github.tanguygab.tabadditions.shared.features.chat.commands.CommandManager
import io.github.tanguygab.tabadditions.shared.features.chat.emojis.EmojiManager
import io.github.tanguygab.tabadditions.shared.features.chat.mentions.MentionManager
import me.leoko.advancedban.manager.PunishmentManager
import me.leoko.advancedban.manager.UUIDManager
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.config.file.ConfigurationFile
import me.neznamy.tab.shared.cpu.TimedCaughtTask
import me.neznamy.tab.shared.features.types.JoinListener
import me.neznamy.tab.shared.features.types.RefreshableFeature
import me.neznamy.tab.shared.features.types.UnLoadable
import me.neznamy.tab.shared.placeholders.types.PlayerPlaceholderImpl
import me.neznamy.tab.shared.placeholders.types.RelationalPlaceholderImpl
import me.neznamy.tab.shared.platform.TabPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import java.time.LocalDateTime
import java.util.UUID

class Chat(private val plugin: TABAdditions, config: ConfigurationFile) : RefreshableFeature(), UnLoadable, JoinListener {
    private val tab = TAB.getInstance()

    internal val mm = MiniMessage.miniMessage()
    private val plainTextSerializer = PlainTextComponentSerializer.plainText()
    private val legacySerializer = LegacyComponentSerializer.legacySection()
    private val formats = mutableMapOf<String, ChatFormat>()

    override fun getFeatureName() = "Chat"
    override fun getRefreshDisplayName() = "&aChat&r"

    private val chatPlaceholderFormat = config.getString("chat-placeholder.format", "%msg%")
    private val chatPlaceholderRelational = config.getBoolean("chat-placeholder.relational", false)
    private var chatPlaceholder: PlayerPlaceholderImpl? = null
    private var relChatPlaceholder: RelationalPlaceholderImpl? = null
    private val chatPlaceholderStay = config.getInt("chat-placeholder.stay", 3000)

    private val chatFormatter: ChatFormatter = ChatFormatter(config)
    private val emojiManager: EmojiManager?
    private val mentionManager: MentionManager?
    private val msgManager: MsgManager?
    internal val socialSpyManager: SocialSpyManager?
    private val commandsManager: CommandManager?

    private var cooldownTime = config.getString("cooldown", "0").toDoubleOrNull() ?: .0
    private var cooldown = mutableMapOf<UUID, LocalDateTime>()

    private val toggleCmd = config.getBoolean("/togglechat", true)
    private var toggled: MutableList<UUID>? = null
    private var toggleChatPlaceholder: PlayerPlaceholderImpl? = null
    internal val placeholders = mutableListOf<PlayerPlaceholderImpl>()

    private val ignoreCmd: Boolean
    private val ignored = mutableMapOf<UUID, MutableList<UUID>>()

    private val clearChatEnabled = config.getBoolean("clearchat.enabled", false)
    private val clearChatAmount = config.getInt("clearchat.amount", 100)
    private val clearChatLine = config.getString("clearchat.line", "")

    private val discordFormat = config.getString("discord-support.format", "%msg%")
    private val discordEssX = config.getBoolean("discord-support.EssentialsX", true)
    private val discordSRV = config.getBoolean("discord-support.DiscordSRV", true)

    val bukkitBridgeChatEnabled = plugin.platform.isProxy && config.getBoolean("chat-from-bukkit-bridge", false)

    init {
        val formats = config.getConfigurationSection("formats")
        formats.keys.forEach {
            val name = it.toString()
            val format = formats.getConfigurationSection(name)

            this.formats[name] = ChatFormat(
                name,
                format.getString("name") ?: name,
                tab.placeholderManager.conditionManager.getByNameOrExpression(format.getString("condition")),
                tab.placeholderManager.conditionManager.getByNameOrExpression(format.getString("view-condition")),
                format.getString("channel") ?: "",
                ChatUtils.componentsToMM(format.getConfigurationSection("display"))
            )
        }
        val pm = tab.placeholderManager
        placeholders.add(pm.registerPlayerPlaceholder("%chat-format%", 1000) { getFormat(it as TabPlayer)?.displayName ?: "" })

        config.apply {
            emojiManager = if (getBoolean("emojis.enabled", false))
                EmojiManager(
                    this@Chat,
                    ChatUtils.componentToMM(getConfigurationSection("emojis.output")),
                    getBoolean("emojis.block-without-permission", false),
                    getBoolean("emojis.auto-complete", true),
                    getConfigurationSection("emojis.categories"),
                    getBoolean("emojis./emojis", true),
                    getBoolean("emojis./toggleemojis", true)
                )
            else null

            mentionManager = if (getBoolean("mention.enabled", true))
                MentionManager(
                    this@Chat,
                    getString("mention.input", "@%player%"),
                    ChatUtils.componentToMM(getConfigurationSection("mention.output")),
                    getString("mention.sound", "block.note_block.pling"),
                    getBoolean("mention./togglementions", true),
                    getBoolean("mention.output-for-everyone", true),
                    getConfigurationSection("mention.custom-mentions")
                )
            else null

            msgManager = if (getBoolean("msg.enabled", true))
                MsgManager(
                    this@Chat, ChatUtils.componentToMM(getConfigurationSection("msg.sender")),
                    ChatUtils.componentToMM(getConfigurationSection("msg.viewer")),
                    getString("msg.cooldown", "0").toDoubleOrNull() ?: .0,
                    getStringList("msg./msg-aliases", listOf("tell", "whisper", "w", "m")),
                    getBoolean("msg.msg-self", true),
                    getBoolean("msg./togglemsg", true),
                    getBoolean("msg./reply", true),
                    getBoolean("msg.save-last-sender-for-reply", true)
                )
            else null

            socialSpyManager = if (getBoolean("socialspy.enabled", true))
                SocialSpyManager(
                    this@Chat,
                    getBoolean("socialspy.msgs.spy", true),
                    ChatUtils.componentToMM(getConfigurationSection("socialspy.msgs.output")),
                    getBoolean("socialspy.channels.spy", true),
                    ChatUtils.componentToMM(getConfigurationSection("socialspy.channels.output")),
                    getBoolean("socialspy.view-conditions.spy", true),
                    ChatUtils.componentToMM(getConfigurationSection("socialspy.view-conditions.output"))
                )
            else null

            commandsManager = if (config.getConfigurationSection("commands-formats").keys.isNotEmpty())
                CommandManager(this@Chat, config.getConfigurationSection("commands-formats"))
            else null
        }

        if (chatPlaceholderRelational) relChatPlaceholder = pm.registerRelationalPlaceholder("%rel_chat%", -1) { _, _ -> "" }
        else placeholders.add(pm.registerPlayerPlaceholder("%chat%", -1) { "" }.also { chatPlaceholder = it })

        tab.onlinePlayers.forEach { loadProperties(it) }

        val lang = plugin.translation
        if (clearChatEnabled) tab.platform.registerCustomCommand("clearchat") { sender, _ ->
            if (!sender.hasPermission("tabadditions.chat.clearchat")) return@registerCustomCommand

            val lineBreaks = ("\n" + clearChatLine).repeat(clearChatAmount) + "\n" + lang.getChatCleared(sender)
            tab.onlinePlayers.forEach { _ -> sender.sendMessage(lineBreaks) }
        }

        if (toggleCmd) {
            tab.platform.registerCustomCommand("togglechat") { sender, _ ->
                plugin.toggleCmd(sender, toggled!!, toggleChatPlaceholder, lang.chatOn, lang.chatOff, false)
            }
            toggleChatPlaceholder = pm.registerPlayerPlaceholder(
                "%chat-status%",
                -1
            ) { if (hasChatToggled(it as TabPlayer)) "Off" else "On" }
            placeholders.add(toggleChatPlaceholder!!)
            toggled = plugin.loadData("chat-off", true)
        }

        ignoreCmd = config.getBoolean("/ignore", true)
        if (ignoreCmd) {
            val ignored = plugin.playerData.getConfigurationSection("ignored")
            ignored.keys.forEach { key ->
                val player = key.toString()
                val list = ignored.getStringList(player, listOf())
                    .map { UUID.fromString(it) }
                    .toMutableList()
                this.ignored[UUID.fromString(player)] = list
            }
        }

        if (ignoreCmd) tab.platform.registerCustomCommand("ignore") { sender, args ->
            if (args.isEmpty()) {
                sender.sendMessage(lang.providePlayer)
                return@registerCustomCommand
            }
            val arg = args[0]
            if (sender.name.equals(arg, ignoreCase = true)) {
                sender.sendMessage(lang.cantIgnoreSelf)
                return@registerCustomCommand
            }
            val tabPlayer = plugin.getPlayer(arg)
            if (tabPlayer == null) {
                sender.sendMessage(lang.getPlayerNotFound(arg))
                return@registerCustomCommand
            }
            val playerUUID = tabPlayer.uniqueId
            val ignored = ignored.computeIfAbsent(sender.uniqueId) { mutableListOf() }
            if (playerUUID in ignored) ignored.remove(playerUUID)
            else ignored.add(playerUUID)
            sender.sendMessage(lang.getIgnore(arg, playerUUID in ignored))
        }
    }

    private fun loadProperties(player: TabPlayer) {
        player.loadPropertyFromConfig(this, "chatprefix", "")
        player.loadPropertyFromConfig(this, "customchatname", player.name)
        player.loadPropertyFromConfig(this, "chatsuffix", "")
        val exp = tab.placeholderManager.tabExpansion
        placeholders.forEach { exp.setPlaceholderValue(player, it.getIdentifier(), it.getLastValue(player)) }
    }

    override fun refresh(player: TabPlayer, force: Boolean) {}

    override fun unload() {
        emojiManager?.unload()
        mentionManager?.unload()
        msgManager?.unload()
        socialSpyManager?.unload()
        commandsManager?.unload()
    }

    fun getFormat(player: TabPlayer) = formats.values.find { it.isConditionMet(player) }

    fun hasChatToggled(player: TabPlayer) = toggled!!.contains(player.uniqueId)

    override fun onJoin(player: TabPlayer) {
        loadProperties(player)
        if (emojiManager != null && emojiManager.autoCompleteEnabled && !emojiManager.hasCmdToggled(player))
            emojiManager.loadAutoComplete(player)
    }

    fun onChat(sender: TabPlayer, message: String) {
        if (isMuted(sender)) return

        if (cooldown.containsKey(sender.uniqueId)) {
            val time = java.time.temporal.ChronoUnit.SECONDS.between(
                cooldown[sender.uniqueId],
                LocalDateTime.now()
            )
            if (time < cooldownTime) {
                sender.sendMessage(plugin.translation.getCooldown(cooldownTime - time))
                return
            }
        }
        if (cooldownTime != .0 && !sender.hasPermission("tabadditions.chat.bypass.cooldown"))
            cooldown[sender.uniqueId] = LocalDateTime.now()

        if (chatFormatter.shouldBlock(message, sender)) {
            sender.sendMessage(plugin.translation.cantSwear)
            return
        }

        var message = message
        var format: ChatFormat? = null
        if (commandsManager != null) {
            format = commandsManager.getFromPrefix(sender, message)
            if (format != null) message = message.substringAfter(format.prefix)
            else if (commandsManager.contains(sender)) format = commandsManager.getFormat(sender)
        }
        if (format == null) format = getFormat(sender)
        if (format == null) return
        var text = format.text
        plugin.platform.audience(null).sendMessage(createMessage(sender, null, message, text))

        if (plugin.platform.isPluginEnabled("InteractiveChat")) try {
            text = InteractiveChatAPI.markSender(text, sender.uniqueId)
        } catch (_: java.lang.IllegalStateException) {}

        val cpu = tab.cpu
        if (!chatPlaceholderRelational) {
            val placeholderMsg = legacySerializer.serialize(createMessage(sender, null, message, chatPlaceholderFormat))
            chatPlaceholder!!.updateValue(sender, placeholderMsg)

            cpu.processingThread.executeLater(TimedCaughtTask(cpu, {
                if (chatPlaceholder!!.getLastValue(sender) == placeholderMsg) chatPlaceholder!!.updateValue(sender, "")
            }, featureName, "update %chat% for " + sender.name), chatPlaceholderStay)
        }

        for (viewer in tab.onlinePlayers) {
            socialSpyManager?.process(sender, viewer, message, socialSpyManager.isSpying(sender, viewer, format))
            if (!canSee(sender, viewer, format)) continue
            sendMessage(viewer, createMessage(sender, viewer, message, text))
            if (!chatPlaceholderRelational) continue
            val placeholderMsg =
                legacySerializer.serialize(createMessage(sender, viewer, message, chatPlaceholderFormat))
            relChatPlaceholder!!.updateValue(viewer, sender, placeholderMsg)

            cpu.processingThread.executeLater(
                TimedCaughtTask(cpu, {
                    if (relChatPlaceholder!!.getLastValue(viewer, sender) == placeholderMsg)
                        relChatPlaceholder!!.updateValue(viewer, sender, "")
                }, featureName, "update %rel_chat% for ${viewer.name} and ${sender.name}"),
                chatPlaceholderStay
            )
        }

        val discord = mutableListOf<String>()
        if (discordSRV) discord.add("DiscordSRV")
        if (discordEssX) discord.add("EssentialsX")
        if (discord.isEmpty()) return
        val msgToDiscord = plainTextSerializer.serialize(createMessage(sender, null, message, discordFormat))
        if (format.hasNoViewCondition()) plugin.platform.sendToDiscord(sender, msgToDiscord, format.channel, discord)
    }

    fun sendMessage(player: TabPlayer?, component: Component) {
        plugin.platform.audience(player).sendMessage(component)
    }

    fun createMessage(sender: TabPlayer, viewer: TabPlayer?, message: String, text: String): Component {
        var output = plugin.parsePlaceholders(text, sender, viewer)
            .replace("%msg%", process(sender, viewer, message))
        output = ChatUtils.toMMColors(output)
        return mm.deserialize(output)
    }

    private fun process(sender: TabPlayer, viewer: TabPlayer?, message: String): String {
        var message = mm.escapeTags(message)
        if (emojiManager != null) message = emojiManager.process(sender, viewer, message)
        if (mentionManager != null && viewer != null) message = mentionManager.process(sender, viewer, message)
        return chatFormatter.process(message, sender)
    }

    fun isMuted(p: TabPlayer) = plugin.platform.isPluginEnabled("AdvancedBan")
            && PunishmentManager.get().isMuted(
        if (UUIDManager.get().mode != UUIDManager.FetcherMode.DISABLED) p.uniqueId.toString().replace("-", "")
        else p.name.lowercase())

    fun isIgnored(sender: TabPlayer, viewer: TabPlayer) = !sender.hasPermission("tabadditions.chat.bypass.ignore")
            && sender.uniqueId in ignored.getOrDefault(viewer.uniqueId, listOf())

    private fun canSee(sender: TabPlayer, viewer: TabPlayer?, f: ChatFormat): Boolean {
        if (sender === viewer) return true
        if (viewer == null) return f.channel.isEmpty() && f.hasNoViewCondition()
        if (f.channel != getFormat(viewer)!!.channel) return false
        return f.isViewConditionMet(sender, viewer)
    }
}
