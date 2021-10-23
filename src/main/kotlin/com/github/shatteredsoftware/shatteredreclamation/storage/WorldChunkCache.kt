package com.github.shatteredsoftware.shatteredreclamation.storage

import com.github.shatteredsoftware.shatteredreclamation.ext.id
import org.apache.commons.lang.SerializationException
import org.bukkit.Chunk
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.file.Files
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterOutputStream

class WorldChunkCache {
    companion object {
        private const val itemSize = Long.SIZE_BYTES * 2

        fun input(original: ByteArray): WorldChunkCache {
            val ostream = ByteArrayOutputStream()
            val inflate = InflaterOutputStream(ostream)
            inflate.write(original)
            inflate.flush()
            inflate.close()

            val arr = ostream.toByteArray()

            val valid = arr.size % itemSize == 0
            if (!valid) {
                throw SerializationException("Trying to decode byte array of invalid size to WorldChunkCache.")
            }

            val cache = WorldChunkCache()
            val items = arr.size / itemSize

            val buffer = ByteBuffer.allocate(arr.size)
            buffer.put(arr)
            buffer.flip()
            val longBuffer = buffer.asLongBuffer()

            for (i in 0 until items) {
                val id = longBuffer.get()
                val lastModified = longBuffer.get()
                cache.map[id] = lastModified
            }

            return cache
        }

        fun readFromFile(file: File): WorldChunkCache {
            return input(Files.readAllBytes(file.toPath()))
        }
    }

    private val map = mutableMapOf<Long, Long>()

    fun loaded(id: Long, time: Long): Long {
        val last = map[id] ?: System.currentTimeMillis()
        map[id] = time
        return time - last
    }

    fun loaded(chunk: Chunk): Long {
        val currentTime = System.currentTimeMillis()
        val last = map[chunk.id] ?: currentTime
        map[chunk.id] = currentTime
        return currentTime - last
    }

    fun lastLoaded(id: Long): Long? {
        return map[id]
    }

    fun lastLoaded(chunk: Chunk): Long? {
        return map[chunk.id]
    }

    fun writeToFile(file: File) {
        val res = output()
        Files.write(file.toPath(), res)
    }

    fun output(): ByteArray {
        val buffer = ByteBuffer.allocate(itemSize * map.size)
        map.forEach { (id, lastModified) ->
            buffer.putLong(id)
            buffer.putLong(lastModified)
        }
        val out = buffer.array()
        val ostream = ByteArrayOutputStream()
        val compress = DeflaterOutputStream(ostream)
        compress.write(out)
        compress.flush()
        compress.close()
        return ostream.toByteArray()
    }

}