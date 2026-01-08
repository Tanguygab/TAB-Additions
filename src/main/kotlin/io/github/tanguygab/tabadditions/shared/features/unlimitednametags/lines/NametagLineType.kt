package io.github.tanguygab.tabadditions.shared.features.unlimitednametags.lines

import me.neznamy.tab.shared.Property
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import kotlin.enums.EnumEntries

interface NametagLineType {
    val properties: Map<String, String>
    fun refresh(data: NametagLine.NametagLineData, force: Boolean)
}

fun Property.update(force: Boolean, call: (String) -> Unit) {
    if (update() || force) call(get())
}
fun <T: Enum<T>> EnumEntries<T>.get(string: String) = find { it.name.equals(string, ignoreCase = true) }

class NametagLineText(
    alignment: String,
    backgroundColor: String,
    defaultBackground: String,
    width: String,
    seeThrough: String,
    shadowed: String,
    opacity: String,
    text: String
) : NametagLineType {
    override val properties = mapOf(
        "alignment" to alignment,
        "background-color" to backgroundColor,
        "default-background" to defaultBackground,
        "width" to width,
        "see-through" to seeThrough,
        "shadowed" to shadowed,
        "opacity" to opacity,
        "text" to text
    )

    override fun refresh(data: NametagLine.NametagLineData, force: Boolean) {
        (data.entity as TextDisplay).apply {
            data.type["alignment"]?.update(force) {
                alignment = TextDisplay.TextAlignment.entries.get(it) ?: TextDisplay.TextAlignment.CENTER
            }

            data.type["background-color"]?.update(force) {
                backgroundColor = try { Color.fromARGB(it.hexToInt()) } catch (_: Exception) { null }
            }

            data.type["default-background"]?.update(force) { isDefaultBackground = it.lowercase() in listOf("true", "yes") }
            data.type["width"]?.update(force) { lineWidth = it.toIntOrNull() ?: 200 }
            data.type["see-through"]?.update(force) { isSeeThrough = it.lowercase() in listOf("true", "yes") }
            data.type["shadowed"]?.update(force) { isShadowed = it.lowercase() in listOf("true", "yes") }
            data.type["opacity"]?.update(force) { textOpacity = it.toByteOrNull() ?: -1 }
            data.type["text"]?.update(force) { text(if (it.isEmpty()) null else MiniMessage.miniMessage().deserialize(it)) }
        }
    }
}

class NametagLineBlock(block: String) : NametagLineType {
    override val properties = mapOf("block" to block)

    override fun refresh(data: NametagLine.NametagLineData, force: Boolean) {
        data.type["block"]?.update(force) {
            (data.entity as BlockDisplay).block = try {
                Bukkit.createBlockData(it)
            } catch (_: Exception) {
                Bukkit.createBlockData(Material.STONE)
            }
        }
    }
}

class NametagLineItem(material: String, itemTransform: String) : NametagLineType {
    override val properties = mapOf(
        "material" to material,
        "item-transform" to itemTransform
    )

    override fun refresh(data: NametagLine.NametagLineData, force: Boolean) {
        (data.entity as ItemDisplay).apply {
            data.type["material"]?.update(force) {
                val mat = Material.getMaterial(it.uppercase())
                setItemStack(mat?.let { mat -> ItemStack(mat) })
            }

            data.type["item-transform"]?.update(force) {
                itemDisplayTransform = ItemDisplay.ItemDisplayTransform.entries.get(it) ?: ItemDisplay.ItemDisplayTransform.FIXED
            }
        }
    }
}
