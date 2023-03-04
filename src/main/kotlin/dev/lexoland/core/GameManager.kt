package dev.lexoland.core

import dev.lexoland.LOG
import dev.lexoland.PLUGIN
import dev.lexoland.utils.text
import java.time.Duration
import java.util.Random
import kotlin.random.asKotlinRandom
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.ExperienceOrb
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile

object GameManager {

    val randomSource = Random().asKotlinRandom()

    lateinit var map: Map
    lateinit var spawnHandler: SpawnHandler
    private lateinit var lootBoxHandler: LootBoxHandler
    var started = false
    var inGame = false
    var preparation = true

    private var startCountdown = 10
    private var preparationTime = 15

    fun setup() {
        map = randomMap().let {
            if(it == null) {
                LOG.error("No map found!")
                return
            }
            it
        }
        LOG.info("Selected map ${map.name}")
        spawnHandler = SpawnHandler(map.center, map.spawns)
        lootBoxHandler = LootBoxHandler(map.lootBoxes)
    }

    fun join(player: Player) {
        if(started) {
            player.gameMode = GameMode.SPECTATOR
            player.teleportAsync(map.center.add(0.0, 10.0, 0.0))
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
        startCountdown = 10
        preparationTime = 15
        lootBoxHandler.setup()

        Bukkit.getScheduler().runTaskTimer(PLUGIN, { task ->
            if (startCountdown == 0) {
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
                    text("$startCountdown", NamedTextColor.WHITE)
                        .append(text("s", NamedTextColor.GRAY))
                )
                p.sendTitlePart(TitlePart.TIMES, Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ofSeconds(1)))
            }
            startCountdown--
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
        startCountdown = 5

        Bukkit.getScheduler().runTaskTimer(PLUGIN, { task ->
            if (startCountdown == 0) {
                task.cancel()
                restart()
                return@runTaskTimer
            }
            map.world.players.forEach {
                val p = it.player ?: return@forEach
                p.sendMessage(text("Das Spiel endet in ${startCountdown}s", NamedTextColor.WHITE))
            }
            startCountdown--
        }, 0, 20)
    }

    private fun restart() {
        map.world.entities.forEach {
            if(it is Item || it is Projectile || it is ExperienceOrb)
                it.remove()
        }
        setup()
        map.world.players.forEach { join(it) }
    }
}
