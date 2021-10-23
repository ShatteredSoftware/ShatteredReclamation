package com.github.shatteredsoftware.shatteredreclamation.data

import com.github.shatteredsoftware.shatteredreclamation.action.SyncAction
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.Block

interface ReclamationGroup {
    val materials: Set<Material>
    val chance: Double
    fun apply(chunk: Chunk, block: Block, config: ReclamationConfig): SyncAction?
}