package com.github.shatteredsoftware.shatteredreclamation.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.shatteredsoftware.shatteredreclamation.action.BlockPlaceAction
import com.github.shatteredsoftware.shatteredreclamation.action.SyncAction
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import java.util.concurrent.ThreadLocalRandom
import kotlin.random.Random

data class DecayGroup(
    @JsonProperty("materials", required = true) override val materials: Set<Material>,
    @JsonProperty("chance", required = true) override val chance: Double,
    @JsonProperty("into", required = true) val into: Set<Material>,
    @JsonProperty("weight_mod") val weightMod: Double = 0.0,
    @JsonProperty("randomize") val randomize: Boolean = true,
) : ReclamationGroup {

    override fun apply(chunk: Chunk, block: Block, config: ReclamationConfig): SyncAction? {
        val chanceMod = chance + calculateWeight(block)
        if (Random.nextFloat() < chanceMod) {
            val newType = into.random()
            return BlockPlaceAction(block.location, newType, randomize)
        }
        return null
    }

    private fun calculateWeight(block: Block): Double {
        var mod = 0
        var current = block
        while (current.type.isSolid) {
            val next = current.getRelative(BlockFace.UP)
            mod++
            current = next
        }
        return mod * weightMod
    }
}