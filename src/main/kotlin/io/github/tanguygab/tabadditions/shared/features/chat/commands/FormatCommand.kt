package io.github.tanguygab.tabadditions.shared.features.chat.commands

import io.github.tanguygab.tabadditions.shared.features.advancedconditions.AdvancedConditions
import io.github.tanguygab.tabadditions.shared.features.chat.ChatFormat

class FormatCommand(
    name: String,
    displayName: String,
    condition: AdvancedConditions?,
    viewCondition: AdvancedConditions?,
    channel: String,
    text: String,
    val saveOnReload: Boolean,
    val prefix: String
) : ChatFormat(name, displayName, condition, viewCondition, channel, text.replace("%chat-format%", displayName)) 