package dev.lexoland.core

import dev.lexoland.utils.jsonFormat
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.World
import java.io.File
import java.util.*

private val maps = mutableMapOf<UUID, Map>()

@Serializable
data class Map(
    @Contextual
    val world: World,
    val name: String,
    @Contextual
    val displayName: Component
) {

    @Contextual
    lateinit var center: Location
    val spawns = mutableListOf<Spawn>()
    val lootBoxes = mutableListOf<LootBox>()
}

val World.map get() = maps[uid]

private val mapDirectory = File("maps")

fun saveMaps() {
    mapDirectory.mkdirs()
    for (map in maps.values) {
        val file = File(mapDirectory, "${map.name}.json")
        file.writeText(jsonFormat.encodeToString(map))
    }
}

fun loadMaps() {
    for (file in mapDirectory.listFiles()!!) {
        if (!file.isFile)
            continue
        val map = jsonFormat.decodeFromString(Map.serializer(), file.readText())
        maps[map.world.uid] = map
    }
}