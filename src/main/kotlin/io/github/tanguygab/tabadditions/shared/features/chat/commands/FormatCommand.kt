package io.github.tanguygab.tabadditions.shared.features.chat.commands

import io.github.tanguygab.tabadditions.shared.features.chat.ChatFormat
import me.neznamy.tab.shared.placeholders.conditions.Condition

class FormatCommand(
    name: String,
    displayName: String,
    condition: Condition?,
    viewCondition: Condition?,
    channel: String,
    text: String,
    val saveOnReload: Boolean,
    val prefix: String
) : ChatFormat(name, displayName, condition, viewCondition, channel, text.replace("%chat-format%", displayName)) 