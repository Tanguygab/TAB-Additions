package io.github.tanguygab.tabadditions.shared.features.unlimitednametags.listeners

import io.github.tanguygab.tabadditions.shared.features.unlimitednametags.UnlimitedNametags
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent

class ChunkListener(val feature: UnlimitedNametags) : Listener {

    @EventHandler
    fun onChunk(e: ChunkLoadEvent) {
        e.chunk.entities
            .filter { it.persistentDataContainer.has(feature.entityKey) }
            .forEach { it.scheduler.run(feature.bukkit, { _ -> it.remove() }, null) }
    }
}