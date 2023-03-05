package dev.lexoland.core

import dev.lexoland.LOG
import dev.lexoland.PLUGIN
import dev.lexoland.asId
import dev.lexoland.utils.text
import java.time.Duration
import java.util.Random
import kotlin.random.asKotlinRandom
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.entity.ExperienceOrb
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import java.io.File

object Game {

    val randomSource = Random().asKotlinRandom()

    lateinit var gameWorld: World
    lateinit var map: Map
    lateinit var lootBoxHandler: LootBoxHandler
    lateinit var spawnHandler: SpawnHandler
    var started = false
    var inGame = false
    var preparation = true
    var preparationTime = 15

    val initialized get() = this::map.isInitialized

    fun setup() {
        map = randomMap().let {
            if(it == null) {
                LOG.error("No map found!")
                return
            }
            it
        }
        swapGameWorld(map)?.let {
            gameWorld = it
            spawnHandler = SpawnHandler(gameWorld, map)
            lootBoxHandler = LootBoxHandler(gameWorld, map)
            LOG.info("Selected map ${map.name}")
            return
        }
        LOG.error("Failed to swap map: ${map.name}")
    }

    fun join(player: Player) {
        if (started) {
            player.gameMode = GameMode.SPECTATOR
            player.teleport(map.center.toLocation(gameWorld))
            return
        }

        player.gameMode = GameMode.SURVIVAL
        player.inventory.clear()
        player.exp = 0.0f
        player.level = 0
        player.foodLevel = 20
        spawnHandler.prepareSpawnFor(player)

        if(!spawnHandler.hasFreeSpawns())
            startGame()
    }

    private fun startGame() {
        spawnHandler.spawns.forEach {
            it.enclose()
        }

        started = true
        inGame = true
        preparation = true
        preparationTime = 15
        lootBoxHandler.setup()

        var countdown = 10
        Bukkit.getScheduler().runTaskTimer(PLUGIN, { task ->
            if (countdown == 0) {
                task.cancel()
                spawnHandler.spawns.forEach {
                    it.player!!.inventory.clear()
                    it.player!!.gameMode = GameMode.SURVIVAL
                    it.unclose()
                }
                startPreparationTime()
                return@runTaskTimer
            }
            spawnHandler.spawns.forEach {
                val p = it.player ?: return@forEach
                p.sendTitlePart(TitlePart.TITLE, text("Spiel startet in", NamedTextColor.WHITE))
                p.sendTitlePart(
                    TitlePart.SUBTITLE,
                    text("$countdown", NamedTextColor.WHITE)
                        .append(text("s", NamedTextColor.GRAY))
                )
                p.sendTitlePart(TitlePart.TIMES, Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ofSeconds(1)))
            }
            countdown--
        }, 0, 1)
    }

    private fun startPreparationTime() {
        Bukkit.getScheduler().runTaskTimer(PLUGIN, { task ->
            if (preparationTime == 0) {
                preparation = false
                task.cancel()
                return@runTaskTimer
            }
            spawnHandler.spawns.forEach {
                val p = it.player ?: return@forEach
                p.sendActionBar(text("Vorbereitungszeit: ${preparationTime}s", NamedTextColor.WHITE))
            }
            preparationTime--
        }, 0, 1)
    }

    fun endGame() {
        inGame = false
        preparation = true

        var countdown = 5
        Bukkit.getScheduler().runTaskTimer(PLUGIN, { task ->
            if (countdown == 0) {
                task.cancel()
                restart()
                return@runTaskTimer
            }
            Bukkit.getOnlinePlayers().forEach {
                val p = it.player ?: return@forEach
                p.sendMessage(text("Das Spiel endet in ${countdown}s", NamedTextColor.WHITE))
            }
            countdown--
        }, 0, 20)
    }

    private fun restart() {
        setup()
        Bukkit.getOnlinePlayers().forEach { join(it) }
    }


    private const val GAME_WORLD_NAME = "game"

    private fun swapGameWorld(map: Map): World? {
        if (!Bukkit.unloadWorld(GAME_WORLD_NAME, false))
            LOG.warn("Could not unload world: $GAME_WORLD_NAME")
        val gameWorldDir = File(GAME_WORLD_NAME)
        if (!gameWorldDir.deleteRecursively())
            LOG.warn("Could not delete world directory $gameWorldDir")
        File(map.name).copyRecursively(gameWorldDir)
        if (!File(gameWorldDir, "uid.dat").delete()) {
            LOG.error("Could not delete uid.dat in $gameWorldDir!")
            return null
        }
        return Bukkit.createWorld(WorldCreator.ofKey(GAME_WORLD_NAME.asId()))
    }
}
