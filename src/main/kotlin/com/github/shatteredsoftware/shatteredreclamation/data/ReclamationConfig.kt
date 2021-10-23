package com.github.shatteredsoftware.shatteredreclamation.data

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

data class ReclamationConfig(val threads: ReclamationThreadConfig, val worlds: Map<String, ReclamationWorld>) {
    companion object {
        private val mapper = let {
            val mapper = ObjectMapper(YAMLFactory())
            mapper.registerModule(KotlinModule())
            mapper.findAndRegisterModules()
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
            mapper
        }

        fun load(file: File): ReclamationConfig {
            return mapper.readValue(file)
        }
    }
}