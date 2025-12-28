package io.github.tanguygab.tabadditions.shared

import io.github.tanguygab.tabadditions.shared.features.ConditionalAppearance
import io.github.tanguygab.tabadditions.shared.features.ConditionalNametags
import io.github.tanguygab.tabadditions.shared.features.actionbar.ActionBarManager
import io.github.tanguygab.tabadditions.shared.features.advancedconditions.AdvancedConditions
import io.github.tanguygab.tabadditions.shared.features.chat.Chat
import io.github.tanguygab.tabadditions.shared.features.titles.TitleManager
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.api.event.plugin.TabLoadEvent
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.chat.EnumChatFormat
import me.neznamy.tab.shared.chat.component.TabComponent
import me.neznamy.tab.shared.chat.component.TabTextComponent
import me.neznamy.tab.shared.config.PropertyConfiguration
import me.neznamy.tab.shared.config.file.ConfigurationFile
import me.neznamy.tab.shared.config.file.YamlConfigurationFile
import me.neznamy.tab.shared.event.impl.TabPlaceholderRegisterEvent
import me.neznamy.tab.shared.features.PlaceholderManagerImpl
import me.neznamy.tab.shared.features.types.TabFeature
import me.neznamy.tab.shared.features.types.UnLoadable
import me.neznamy.tab.shared.placeholders.types.PlayerPlaceholderImpl
import me.neznamy.tab.shared.placeholders.types.RelationalPlaceholderImpl
import me.neznamy.tab.shared.placeholders.types.ServerPlaceholderImpl
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.function.BiFunction

class TABAdditions(
    val platform: Platform,
    private val plugin: Any?,
    private val dataFolder: File?
) {
    private val tab = TAB.getInstance()

    lateinit var config: ConfigurationFile
    lateinit var chatConfig: ConfigurationFile
    lateinit var translation: TranslationFile

    private val features = mutableListOf<String>()


    fun load() {
        loadFiles()
        tab.eventBus?.register(TabPlaceholderRegisterEvent::class.java) { onPlaceholderRegister(it) }
        reload()
        tab.eventBus?.register(TabLoadEvent::class.java){ platform.runTask { reload() } }
    }

    fun loadFiles() {
        try {
            config = YamlConfigurationFile(
                TABAdditions::class.java.getClassLoader().getResourceAsStream("config.yml"),
                File(dataFolder, "config.yml")
            )
            chatConfig = YamlConfigurationFile(
                TABAdditions::class.java.getClassLoader().getResourceAsStream("chat.yml"),
                File(dataFolder, "chat.yml")
            )

            val translationFile = File(dataFolder, "translation.yml")
            if (!translationFile.exists()) translationFile.createNewFile()
            translation = TranslationFile(null, translationFile)
        } catch (e: IOException) {
            platform.disable()
            throw RuntimeException(e)
        }
    }


    fun reload() {
        platform.reload()
        loadFiles()
        loadPlaceholders()
        loadFeatures()
    }

    fun disable() {
        val fm = tab.featureManager

        features.forEach {
            val feature = fm.getFeature<TabFeature>(it)
            if (feature is UnLoadable) feature.unload()
            fm.unregisterFeature(it)
        }
        features.clear()
    }

    fun registerFeature(feature: TabFeature) {
        val fm = tab.featureManager
        features.add(feature.featureName)
        fm.registerFeature(feature.featureName, feature)
    }

    private fun loadFeatures() {
        if (config.getBoolean("actionbars.enabled", false)) registerFeature(ActionBarManager())
        if (config.getBoolean("titles.enabled", false)) registerFeature(TitleManager())
        if (chatConfig.getBoolean("enabled", false)) registerFeature(Chat(chatConfig))
        if (config.getBoolean("conditional-nametags.enabled", false) && tab.nameTagManager != null)
            registerFeature(
            ConditionalNametags(
                config.getBoolean("conditional-nametags.show-by-default", true),
                config.getBoolean("conditional-nametags.relational", false)
            )
        )
        if (!platform.isProxy && config.getBoolean("conditional-appearance.enabled", false))
            registerFeature(ConditionalAppearance(plugin, config.getBoolean("conditional-appearance.show-by-default", true)))
    }

    private fun loadPlaceholders() {
        val pm = tab.placeholderManager
        platform.registerPlaceholders(pm)

        AdvancedConditions.clearConditions()
        val conditions = config.getMap<String, Map<String, String>>("advanced-conditions")
        conditions.forEach { (name: String, conditions: Map<String, String>) -> AdvancedConditions(name, conditions) }
        AdvancedConditions.finishSetups()
    }

    private fun onPlaceholderRegister(e: TabPlaceholderRegisterEvent) {
        val identifier = e.identifier
        val pm = tab.placeholderManager
        if (identifier.startsWith("%rel_viewer:")) {
            val placeholder = pm.getPlaceholder("%" + identifier.substring(12))

            e.relationalPlaceholder = when (placeholder) {
                is RelationalPlaceholderImpl -> BiFunction { viewer: TabPlayer, target: TabPlayer ->
                    placeholder.getLastValue(
                        target as me.neznamy.tab.shared.platform.TabPlayer,
                        viewer as me.neznamy.tab.shared.platform.TabPlayer
                    )
                }
                is PlayerPlaceholderImpl -> BiFunction { viewer: TabPlayer, _: TabPlayer ->
                    placeholder.getLastValue(viewer as me.neznamy.tab.shared.platform.TabPlayer)
                }
                else -> e.relationalPlaceholder
            }
            return
        }
    }

    fun parsePlaceholders(string: String?, player: me.neznamy.tab.shared.platform.TabPlayer): String {
        if (string == null) return ""
        if ("%" !in string) return EnumChatFormat.color(string)
        return parsePlaceholders(string, player, null)
    }

    fun parsePlaceholders(
        string: String,
        sender: me.neznamy.tab.shared.platform.TabPlayer,
        viewer: me.neznamy.tab.shared.platform.TabPlayer?,
        placeholders: List<String> = PlaceholderManagerImpl.detectPlaceholders(string)
    ): String {
        var str = string
        for (pl in placeholders) {
            val output = when (val placeholder = tab.placeholderManager.getPlaceholder(pl)) {
                is PlayerPlaceholderImpl -> placeholder.getLastValue(sender)
                is ServerPlaceholderImpl -> placeholder.lastValue
                is RelationalPlaceholderImpl -> if (viewer == null) "" else placeholder.getLastValue(viewer, sender)
                else -> pl
            }
            str = str.replace(pl, output)
        }
        return EnumChatFormat.color(str)
    }

    fun getPlayer(name: String) = tab.onlinePlayers.find { it.name.equals(name, ignoreCase = true) }

    val playerData: ConfigurationFile
        get() = tab.configuration.playerData

    fun loadData(data: String, enabled: Boolean) = if (enabled) playerData
        .getStringList(data, listOf<String>())
        .map { UUID.fromString(it) }
        .toMutableList()
    else mutableListOf()

    fun unloadData(data: String, list: List<UUID>, enabled: Boolean) {
        if (enabled) playerData.set(data, list.map { it.toString() })
    }

    fun toggleCmd(
        player: me.neznamy.tab.shared.platform.TabPlayer,
        toggled: MutableList<UUID>,
        on: String,
        off: String
    ) = toggleCmd(player, toggled, null, on, off, false)

    fun toggleCmd(
        player: me.neznamy.tab.shared.platform.TabPlayer,
        toggled: MutableList<UUID>,
        placeholder: PlayerPlaceholderImpl?,
        on: String,
        off: String,
        invert: Boolean
    ): Boolean {
        val contains = player.uniqueId in toggled
        if (contains) toggled.remove(player.uniqueId)
        else toggled.add(player.uniqueId)

        placeholder?.updateValue(player, if (contains) "On" else "Off")
        player.sendMessage(if (contains != invert) on else off)
        return true
    }

    fun toFlatText(component: TabComponent): String {
        if (component !is TabTextComponent) return component.toLegacyText()

        val modifier = component.modifier
        val builder = StringBuilder()

        if (modifier.color != null) builder.append("#").append(modifier.color!!.hexCode)
        builder.append(modifier.magicCodes)
        builder.append(component.text)
        component.extra.forEach { builder.append(toFlatText(it)) }
        return builder.toString()
    }

    companion object {
        lateinit var INSTANCE: TABAdditions
        fun addProperties() {
            PropertyConfiguration.VALID_PROPERTIES.addAll(
                listOf(
                    "chatprefix",
                    "customchatname",
                    "chatsuffix",
                    "join-actionbar",
                    "join-title",
                    "appearance-condition",
                    "nametag-condition"
                )
            )
        }
    }
}