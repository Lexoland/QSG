package dev.lexoland.core

import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Container

class MapData(
    val world: World,
    val name: Component
) {

    lateinit var center: Location
    val spawns = mutableListOf<SpawnHandler.Spawn>()
    val containers = mutableListOf<Container>()
}