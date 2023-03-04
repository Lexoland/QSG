package dev.lexoland.core

import java.util.Random
import kotlin.random.asKotlinRandom
import org.bukkit.GameMode
import org.bukkit.entity.Player

object GameManager {

    val randomSource = Random().asKotlinRandom()
    val maps = mutableListOf<MapData>()

    lateinit var map: MapData
    lateinit var spawnHandler: SpawnHandler
    lateinit var lootHandler: LootHandler
    var started = false

    fun setup() {
        map = maps.random(randomSource)
        spawnHandler = SpawnHandler(map.center, map.spawns)
        lootHandler = LootHandler(map.containers)
    }

    fun startGame() {

    }

    fun join(player: Player) {
        if(started) {
            player.gameMode = GameMode.SPECTATOR
            // TODO: Add spectator stuff
            return
        }

        player.gameMode = GameMode.SURVIVAL
        player.inventory.clear()
        spawnHandler.prepareSpawnFor(player)
    }
}
