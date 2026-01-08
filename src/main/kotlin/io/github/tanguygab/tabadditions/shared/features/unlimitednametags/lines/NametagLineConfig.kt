package io.github.tanguygab.tabadditions.shared.features.unlimitednametags.lines

import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.config.file.ConfigurationSection
import me.neznamy.tab.shared.placeholders.conditions.Condition

fun ConfigurationSection.getOptString(path: String, default: String) = getObject(path)?.toString() ?: default

data class NametagLineConfig(
    val type: NametagLineType,
    val displayCondition: Condition?,
    val billboard: String,
    val brightnessBlock: String,
    val brightnessSky: String,
    val height: String,
    val width: String,
    val glow: String,
    val shadowRadius: String,
    val shadowStrength: String,
    val viewRange: String,

//    val interpolationDelay: String,
//    val interpolationDuration: String,
//    val teleportDuration: String,
//    val transformation: ,
//    val transformationMatrix: ,
) {
    companion object {
        fun fromSection(section: ConfigurationSection) = NametagLineConfig(
            when (section.getString("type", "text")) {
                "block" -> NametagLineBlock(section.getString("block", "STONE"))
                "item" -> NametagLineItem(
                    section.getString("material", "STONE"),
                    section.getString("item-transform") ?: "FIXED"
                )

                else -> NametagLineText(
                    section.getString("alignment") ?: "CENTER",
                    section.getString("background-color") ?: "#40000000",
                    section.getString("default-background") ?: "false",
                    section.getOptString("width", "200"),
                    section.getOptString("see-through", "true"),
                    section.getOptString("shadowed", "true"),
                    section.getOptString("opacity", "-1"),
                    section.getString("text", "<red>Missing text")
                )
            },
            TAB.getInstance().placeholderManager.conditionManager.getByNameOrExpression(section.getString("condition")),
            section.getString("billboard") ?: "FIXED",
            section.getOptString("brightness.block", ""),
            section.getOptString("brightness.sky", ""),
            section.getOptString("height", "0"),
            section.getOptString("width", "0"),
            section.getOptString("glow", "-1"),
            section.getOptString("shadow.radius", "0"),
            section.getOptString("shadow.strength", "1"),
            section.getOptString("view-range", "1")
        )
    }
}