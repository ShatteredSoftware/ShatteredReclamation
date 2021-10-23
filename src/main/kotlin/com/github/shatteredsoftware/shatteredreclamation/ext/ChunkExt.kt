package com.github.shatteredsoftware.shatteredreclamation.ext

import org.bukkit.Chunk

val Chunk.id get() = this.z.toLong() xor (this.x.toLong() shl 32)