package com.github.shatteredsoftware.shatteredreclamation.listeners

import com.github.shatteredsoftware.shatteredreclamation.ShatteredReclamation
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent

class ChunkUnloadListener(private val plugin: ShatteredReclamation) : Listener {
    @EventHandler
    fun onChunkLoad(event: ChunkUnloadEvent) {
        if (plugin.tracksWorld(event.world)) {
            plugin.processUnload(event.world, event.chunk)
        }
    }
}