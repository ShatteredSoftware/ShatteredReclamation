package com.github.shatteredsoftware.shatteredreclamation

import com.github.shatteredsoftware.shatteredreclamation.storage.WorldChunkCache
import org.junit.jupiter.api.Test

class TestWorldChunkCache {
    @Test
    fun `should serialize and deserialize properly`() {
        val wcc = WorldChunkCache()
        wcc.loaded(1, 200)
        wcc.loaded(2, 100)
        wcc.loaded(3, 0)
        val out = wcc.output()
        println(out.asList())
        val back = WorldChunkCache.input(out)
        assert(back.lastLoaded(1) == 200L)
        assert(back.lastLoaded(2) == 100L)
        assert(back.lastLoaded(3) == 0L)
    }
}