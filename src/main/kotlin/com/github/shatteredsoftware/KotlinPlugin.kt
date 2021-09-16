package com.github.shatteredsoftware

import org.bukkit.plugin.java.JavaPlugin

class KotlinPlugin : JavaPlugin() {
    companion object {
        private var internalInstance: KotlinPlugin? = null

        @JvmStatic
        val instance: KotlinPlugin get() = internalInstance ?: throw IllegalStateException("Could not get instance; is the plugin enabled?")
    }

    init {
        internalInstance = this
    }

    override fun onEnable() {
        logger.info("Hello")
    }
}