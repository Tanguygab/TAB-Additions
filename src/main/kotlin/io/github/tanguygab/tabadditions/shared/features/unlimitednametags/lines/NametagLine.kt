package io.github.tanguygab.tabadditions.shared.features.unlimitednametags.lines

import io.github.tanguygab.tabadditions.shared.features.unlimitednametags.UnlimitedNametags
import me.neznamy.tab.shared.Property
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.data.World
import me.neznamy.tab.shared.features.types.JoinListener
import me.neznamy.tab.shared.features.types.QuitListener
import me.neznamy.tab.shared.features.types.RefreshableFeature
import me.neznamy.tab.shared.features.types.UnLoadable
import me.neznamy.tab.shared.features.types.WorldSwitchListener
import me.neznamy.tab.shared.platform.TabPlayer
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

class NametagLine(
    private val feature: UnlimitedNametags,
    private val config: NametagLineConfig
) : RefreshableFeature(), UnLoadable, JoinListener, QuitListener, WorldSwitchListener {

    override fun getRefreshDisplayName() = "&aUnlimited Nametags&r"
    override fun getFeatureName() = "Unlimited Nametags"

    private val players = mutableMapOf<TabPlayer, NametagLineData>()
    private val loading = mutableListOf<TabPlayer>()
    val TabPlayer.bukkit get() = player as Player

    init {
        config.displayCondition?.let { addUsedPlaceholder(it.placeholderIdentifier) }
        TAB.getInstance().onlinePlayers.forEach { onJoin(it) }
    }

    override fun unload() {
        if (feature.bukkit.isEnabled) players.values.forEach {
            it.entity.scheduler.run(feature.bukkit, { _ -> it.entity.remove() }, null)
        }
        players.clear()
    }

    override fun onJoin(player: TabPlayer) {
        refresh(player, true)
    }

    override fun onQuit(player: TabPlayer) {
        val entity = players[player]?.entity
        entity?.scheduler?.run(feature.bukkit, { entity.remove() }, null)
        players.remove(player)
    }

    override fun refresh(player: TabPlayer, force: Boolean) {
        if (player in loading) return
        var data = players[player]
        if (config.displayCondition?.isMet(player) == false) {
            data?.entity?.remove()
            players.remove(player)
            return
        }
        val loc = player.bukkit.location
        loading.add(player)
        Bukkit.getRegionScheduler().run(feature.bukkit, loc) {
            if (data == null) {
                val entity = loc.world.spawnEntity(loc, when (config.type) {
                    is NametagLineItem -> EntityType.ITEM_DISPLAY
                    is NametagLineBlock -> EntityType.BLOCK_DISPLAY
                    else -> EntityType.TEXT_DISPLAY
                }) as Display
                entity.persistentDataContainer.set(feature.entityKey, PersistentDataType.BOOLEAN, true)
                data = NametagLineData(player, entity, config.type.properties.mapValues { Property(this, player, it.value) })
                players[player] = data
                player.bukkit.addPassenger(entity)
            }
            loading.remove(player)

            config.type.refresh(data, force)
            data.entity.apply {
                data.billboard.update(true) { billboard = Display.Billboard.entries.get(it) ?: Display.Billboard.FIXED }

                if (data.brightnessBlock.update().or(data.brightnessSky.update()) || force) {
                    brightness = if (data.brightnessBlock.get().isBlank() && data.brightnessSky.get().isBlank()) null
                    else {
                        Display.Brightness(
                            data.brightnessBlock.get().toIntOrNull() ?: 0,
                            data.brightnessSky.get().toIntOrNull() ?: 0
                        )
                    }
                }

                data.height.update(true) { displayHeight = it.toFloatOrNull() ?: 0f }
                data.width.update(true) { displayWidth = it.toFloatOrNull() ?: 0f }
                data.glow.update(true) { glowColorOverride = try { Color.fromRGB(it.hexToInt()) } catch (_: Exception) { null } }
                data.shadowRadius.update(true) { shadowRadius = it.toFloatOrNull() ?: 0f }
                data.shadowStrength.update(true) { shadowStrength = it.toFloatOrNull() ?: 0f }
                data.viewRange.update(true) { viewRange = it.toFloatOrNull() ?: 0f }
            }
        }
    }

    fun rotate(player: TabPlayer) {
        players[player]?.entity?.setRotation(player.bukkit.yaw, player.bukkit.pitch)
    }

    override fun onWorldChange(player: TabPlayer, from: World, to: World) {
        val entity = players[player]?.entity
        entity?.scheduler?.run(feature.bukkit, {
            entity.teleport(player.bukkit.location)
            player.bukkit.addPassenger(entity)
        }, null)
    }

    inner class NametagLineData(player: TabPlayer, val entity: Display, val type: Map<String, Property>) {
        val billboard = Property(this@NametagLine, player, config.billboard)
        val brightnessBlock = Property(this@NametagLine, player, config.brightnessBlock)
        val brightnessSky = Property(this@NametagLine, player, config.brightnessSky)
        val height = Property(this@NametagLine, player, config.height)
        val width = Property(this@NametagLine, player, config.width)
        val glow = Property(this@NametagLine, player, config.glow)
        val shadowRadius = Property(this@NametagLine, player, config.shadowRadius)
        val shadowStrength = Property(this@NametagLine, player, config.shadowStrength)
        val viewRange = Property(this@NametagLine, player, config.viewRange)
    }
}

