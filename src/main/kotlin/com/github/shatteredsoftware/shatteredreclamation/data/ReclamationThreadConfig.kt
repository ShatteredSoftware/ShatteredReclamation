package com.github.shatteredsoftware.shatteredreclamation.data

import com.fasterxml.jackson.annotation.JsonProperty

data class ReclamationThreadConfig(
    @JsonProperty("delay") val delay: Long = 5,
    @JsonProperty("report_interval") val reportInterval: Double = 60.0,
    @JsonProperty("sync_interval") val syncInterval: Double = 5.0,
    @JsonProperty("max_sync_changes") val maxSyncChanges: Int = 100
)