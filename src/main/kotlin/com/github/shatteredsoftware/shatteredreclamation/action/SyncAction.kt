package com.github.shatteredsoftware.shatteredreclamation.action

import org.bukkit.Location

abstract class SyncAction(val location: Location) {
    abstract fun apply()
}