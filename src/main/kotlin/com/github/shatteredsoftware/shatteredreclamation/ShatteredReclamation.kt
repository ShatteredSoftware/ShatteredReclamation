package com.github.shatteredsoftware.shatteredreclamation

import com.github.shatteredsoftware.shatteredreclamation.action.SyncAction
import com.github.shatteredsoftware.shatteredreclamation.data.ReclamationConfig
import com.github.shatteredsoftware.shatteredreclamation.data.ReclamationGroup
import com.github.shatteredsoftware.shatteredreclamation.data.ReclamationWorld
import com.github.shatteredsoftware.shatteredreclamation.listeners.ChunkLoadListener
import com.github.shatteredsoftware.shatteredreclamation.storage.WorldChunkCache
import com.github.shynixn.mccoroutine.launch
import com.github.shynixn.mccoroutine.launchAsync
import kotlinx.coroutines.delay
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class ShatteredReclamation : JavaPlugin() {
    companion object {
        private var internalInstance: ShatteredReclamation? = null

        @JvmStatic
        val instance: ShatteredReclamation
            get() = internalInstance ?: throw IllegalStateException("Could not get instance; is the plugin enabled?")
    }

    lateinit var internalConfig: ReclamationConfig
    val config get() = internalConfig

    val processActions: Queue<SyncAction> = LinkedList()

    private val caches: MutableMap<String, Pair<ReclamationWorld, WorldChunkCache>> = mutableMapOf()

    val worldsFolder = File(this.dataFolder, "worlds")

    init {
        internalInstance = this
    }

    override fun onEnable() {
        loadConfig()

        config.worlds.forEach { (name, config) ->
            this.caches[name] = config to loadChunkCache(name)
        }

        var actions = 0
        this.server.pluginManager.registerEvents(ChunkLoadListener(this), this)
        object : BukkitRunnable() {
            override fun run() {
                var processCount = 0
                while (this@ShatteredReclamation.processActions.size > 0 && processCount < config.threads.maxSyncChanges) {
                    val action = this@ShatteredReclamation.processActions.poll()
                    val chunk = action.location.chunk
                    if (!chunk.world.getChunkAt(chunk.x, chunk.z).isLoaded) {
                        continue
                    }
                    action.apply()
                    actions++
                    processCount++
                }
            }
        }.runTaskTimer(this, 0, (ceil(20 * config.threads.syncInterval)).roundToLong())

        if (config.threads.reportInterval != 0.0) {
            object : BukkitRunnable() {
                override fun run() {
                    logger.info("Placed $actions in the last minute.")
                    actions = 0
                }
            }.runTaskTimer(this, 20L * 60, (20 * config.threads.reportInterval).roundToLong())
        }
    }

    fun getWorld(worldName: String): Pair<ReclamationWorld, WorldChunkCache>? {
        return this.caches[worldName]
    }

    fun tracksWorld(world: World): Boolean {
        return world.name in this.caches
    }

    fun processChunk(world: World, chunk: Chunk) {
        launchAsync {
            processChunkAsync(world, chunk)
        }
    }

    private suspend fun processChunkAsync(world: World, chunk: Chunk) {
        val possibleWorld = getWorld(world.name) ?: return
        val (config, cache) = possibleWorld
        val delta = cache.loaded(chunk)
        if (delta < config.period * 1000 * 60) {
            return
        }
        val times = ceil(delta.toDouble() / config.period.toDouble()).roundToInt()
        for (x in 0..15) {
            for (y in world.minHeight..world.maxHeight) {
                for (z in 0..15) {
                    delay(this.config.threads.delay)
                    if (!world.getChunkAt(chunk.x, chunk.z).isLoaded) {
                        return
                    }
                    val block = chunk.getBlock(x, y, z)
                    processGroups(config.spread, chunk, block, times)
                    processGroups(config.decay, chunk, block, times)
                }
            }
        }
    }

    private fun processGroups(groups: List<ReclamationGroup>, chunk: Chunk, block: Block, times: Int) {
        for (group in groups) {
            if (group.materials.contains(block.type)) {
                for (i in 0..times) {
                    val action = group.apply(chunk, block, this.config) ?: continue
                    this.processActions.add(action)
                }
            }
        }
    }

    private fun loadChunkCache(worldName: String): WorldChunkCache {
        worldsFolder.mkdirs()
        val file = File(worldsFolder, "$worldName.cache")
        return if (file.exists()) {
            WorldChunkCache.readFromFile(file)
        } else WorldChunkCache()
    }

    override fun onDisable() {
        this.caches.forEach { (name, configCache) ->
            val (_, cache) = configCache
            cache.writeToFile(File(this.worldsFolder, "$name.cache"))
        }
    }

    private fun loadConfig() {
        this.dataFolder.mkdirs()
        val configFile = File(this.dataFolder, "config.yml")
        if (!configFile.exists()) {
            val configStream = this.javaClass.getResourceAsStream("/config.yml")
                ?: throw FileNotFoundException("Could not find internal config!")
            val configByes = configStream.readAllBytes()
            configStream.close()

            FileOutputStream(configFile).use {
                it.write(configByes)
            }
        }
        internalConfig = ReclamationConfig.load(configFile)
    }

    fun processUnload(world: World, chunk: Chunk) {
        val possibleWorld = getWorld(world.name) ?: return
        val (_, cache) = possibleWorld
        cache.loaded(chunk)
    }
}