package com.github.shatteredsoftware.shatteredreclamation.listeners

import com.github.shatteredsoftware.shatteredreclamation.ShatteredReclamation
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent

class ChunkLoadListener(private val plugin: ShatteredReclamation) : Listener {
    @EventHandler
    fun onChunkLoad(event: ChunkLoadEvent) {
        if (plugin.tracksWorld(event.world)) {
            plugin.processChunk(event.world, event.chunk)
        }
    }
}