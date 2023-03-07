package dev.lexoland.core

import dev.lexoland.PLUGIN
import dev.lexoland.utils.jsonFormat
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import net.kyori.adventure.text.Component
import org.bukkit.World
import org.bukkit.util.Vector
import java.io.File

@Serializable
data class Map(
    val name: String,
    val displayName: @Contextual Component
) {
    var enabled = false
    var center: @Contextual Vector = Vector(0, 64, 0)
    val spawns = mutableListOf<@Contextual Vector>()
    val lootBoxes = mutableListOf<@Contextual Vector>()
}

val maps = mutableMapOf<String, Map>()

val World.map get() = maps[name]

fun randomMap() = maps.values.filter { it.enabled }.randomOrNull()

fun registerMap(map: Map) {
    maps[map.name] = map
}

private val mapDirectory = File(PLUGIN.dataFolder, "maps")

fun saveMaps() {
    mapDirectory.mkdirs()
    for (map in maps.values) {
        val file = File(mapDirectory, "${map.name}.json")
        file.writeText(jsonFormat.encodeToString(map))
    }
}

fun loadMaps() {
    if (!mapDirectory.exists())
        return
    for (file in mapDirectory.listFiles()!!) {
        if (!file.isFile)
            continue
        val map = jsonFormat.decodeFromString(Map.serializer(), file.readText())
        maps[map.name] = map
    }
}