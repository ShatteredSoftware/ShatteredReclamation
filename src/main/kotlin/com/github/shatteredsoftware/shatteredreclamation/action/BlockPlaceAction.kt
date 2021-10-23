package com.github.shatteredsoftware.shatteredreclamation.action

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.Directional
import org.bukkit.block.data.Orientable
import org.bukkit.block.data.type.Stairs

class BlockPlaceAction(location: Location, private val type: Material, private val randomize: Boolean = true) : SyncAction(location) {
    private val doublePlants = setOf(Material.ROSE_BUSH, Material.TALL_GRASS, Material.SUNFLOWER, Material.PEONY, Material.LILAC, Material.LARGE_FERN)

    override fun apply() {
        if (!location.chunk.isLoaded) {
            return
        }
        location.block.type = type
        val data = location.block.blockData
        if (data is Bisected) {
            if (type in doublePlants) {
                val top = location.block.getRelative(BlockFace.UP)
                top.type = type
                val topData = location.block.blockData
                if (topData is Bisected) {
                    topData.half = Bisected.Half.TOP
                }
            }
            else if (randomize) {
                data.half = Bisected.Half.values().random()
            }
        }
        if (randomize && data is Orientable) {
            data.axis = data.axes.random()
        }
        if (randomize && data is Directional) {
            data.facing = data.faces.random()
        }
        if (randomize && data is Stairs) {
            data.half = Bisected.Half.values().random()
            data.shape = Stairs.Shape.values().random()
        }
    }
}