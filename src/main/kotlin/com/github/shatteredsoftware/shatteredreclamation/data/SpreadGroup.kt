package com.github.shatteredsoftware.shatteredreclamation.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.shatteredsoftware.shatteredreclamation.action.BlockPlaceAction
import com.github.shatteredsoftware.shatteredreclamation.action.SyncAction
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import java.util.*
import kotlin.random.Random

data class SpreadGroup(
    @JsonProperty("materials", required = true) override val materials: Set<Material>,
    @JsonProperty("chance", required = true) override val chance: Double,
    @JsonProperty("range", required = true) val range: Int,
    @JsonProperty("valid_blocks", required = true) val validBlocks: Set<Material>,
    @JsonProperty("above") val above: Boolean = true,
    @JsonProperty("randomize") val randomize: Boolean = true,
    @JsonProperty("match") val match: Boolean = true,
    @JsonProperty("choices") val choices: Set<Material> = setOf()
) :
    ReclamationGroup {

    override fun apply(chunk: Chunk, block: Block, config: ReclamationConfig): SyncAction? {
        val result = Random.nextFloat()
        if (result < chance) {
            val destination = search(block) ?: return null
            val offsetDestination = if (above) destination.getRelative(BlockFace.UP) else destination
            val type = if (match) block.type else choices.random()
            return BlockPlaceAction(offsetDestination.location, type, randomize)
        }
        return null
    }

    private fun isValid(block: Block): Boolean {
        return block.type in validBlocks
                && block.getRelative(BlockFace.UP).type.isAir
                && block.getRelative(BlockFace.UP, 2).type.isAir
    }

    private fun search(base: Block): Block? {
        val toSearch = LinkedList<Block>()
        val visited = mutableSetOf<Block>()
        toSearch.add(base)
        for (strata in 0..range) {
            val currentSize = toSearch.lastIndex
            for (i in 0..currentSize) {
                val target = toSearch.pop()
                for (direction in BlockFace.values()) {
                    val test = target.getRelative(direction)
                    if (test in visited || !test.chunk.isLoaded) {
                        continue
                    }
                    if (isValid(test)) {
                        return test
                    }
                    toSearch.add(test)
                    visited.add(test)
                }
            }
        }
        return null
    }
}