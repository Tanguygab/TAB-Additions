package io.github.tanguygab.tabadditions.shared.features.unlimitednametags

import io.github.tanguygab.tabadditions.shared.features.unlimitednametags.lines.NametagLineConfig
import me.neznamy.tab.shared.config.file.ConfigurationSection

data class UnlimitedNametagsConfig(
    val toggleCommand: String,
    val rotation: Boolean,
    val lines: Map<String, NametagLineConfig>
) {
    companion object {
        fun fromSection(section: ConfigurationSection) = UnlimitedNametagsConfig(
            section.getString("toggle-command", "/nametags"),
            section.getBoolean("rotation", true),
            section.getConfigurationSection("lines").let { lines ->
                lines.keys.map { it.toString() }.associateWith { NametagLineConfig.fromSection(lines.getConfigurationSection(it)) }
            }
        )
    }
}