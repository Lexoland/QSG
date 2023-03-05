package dev.lexoland.core

import dev.lexoland.LOG
import dev.lexoland.PLUGIN
import dev.lexoland.asId
import dev.lexoland.utils.PREFIX
import dev.lexoland.utils.plus
import dev.lexoland.utils.text
import net.kyori.adventure.bossbar.BossBar
import java.time.Duration
import kotlin.random.asKotlinRandom
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import org.bukkit.*
import org.bukkit.entity.Player
import java.io.File
import java.util.*
import kotlin.math.ceil

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
    val players = mutableMapOf<UUID, QSGPlayer>()

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

    fun addPlayer(player: Player) {
        val qsgPlayer = QSGPlayer(player)
        players[player.uniqueId] = qsgPlayer
        if (started) {
            qsgPlayer.setToSpectator()
            return
        }
        qsgPlayer.setToPlayer()
        GameStartCountdown.update()
        player.showBossBar(GameStartCountdown.bossBar)
//        spawnHandler.prepareSpawnFor(player)
//
//        if(!spawnHandler.hasFreeSpawns())
//            startGame()
    }

    fun removePlayer(player: Player) {
        player.hideBossBar(GameStartCountdown.bossBar)
        players.remove(player.uniqueId)
        GameStartCountdown.update()
    }

    fun startGame() {
        for (qsgPlayer in players.values)
            qsgPlayer.player.hideBossBar(GameStartCountdown.bossBar)
        Bukkit.broadcast(PREFIX + text("Das Spiel spielt", NamedTextColor.WHITE))
//        started = true
//        inGame = true
//        preparation = true
//        preparationTime = 15
//        lootBoxHandler.setup()
//
//        var countdown = 10
//        Bukkit.getScheduler().runTaskTimer(PLUGIN, { task ->
//            if (countdown == 0) {
//                task.cancel()
//                spawnHandler.spawns.forEach {
//                    it.player!!.inventory.clear()
//                    it.player!!.gameMode = GameMode.SURVIVAL
//                    it.unclose()
//                }
//                startPreparationTime()
//                return@runTaskTimer
//            }
//            spawnHandler.spawns.forEach {
//                val p = it.player ?: return@forEach
//                p.sendTitlePart(TitlePart.TITLE, text("Spiel startet in", NamedTextColor.WHITE))
//                p.sendTitlePart(
//                    TitlePart.SUBTITLE,
//                    text("$countdown", NamedTextColor.WHITE)
//                        .append(text("s", NamedTextColor.GRAY))
//                )
//                p.sendTitlePart(TitlePart.TIMES, Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ofSeconds(1)))
//            }
//            countdown--
//        }, 0, 1)
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
        Bukkit.getOnlinePlayers().forEach { addPlayer(it) }
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


object GameStartCountdown : Countdown(1, 30 * 20, Game::startGame) {
    val bossBar = BossBar.bossBar(text("-"), 1f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS)
    var waiting = true

    fun update() {
        if (Game.players.size < 2) {
            waiting = true
        } else if (waiting) {
            waiting = false
            reset()
        }
    }

    override fun canCountDown() = !waiting

    override fun tick() {
        if (waiting) {
            val dots = text(when (Bukkit.getCurrentTick() / 20 % 4) {
                0 -> "."
                1 -> ".."
                2 -> "..."
                else -> ""
            }, NamedTextColor.YELLOW)
            bossBar.name(text("Warte auf Spieler", NamedTextColor.YELLOW) + dots)
            bossBar.progress(1f)
            bossBar.color(BossBar.Color.YELLOW)
            bossBar.overlay(BossBar.Overlay.PROGRESS)
            return
        }
        bossBar.name(
            text("Spiel startet in ", NamedTextColor.GREEN) + text(
                "${ceil(timeLeft / 20f)}s",
                NamedTextColor.AQUA
            )
        )
        bossBar.progress(timeLeft.toFloat() / initialTime)
        bossBar.color(BossBar.Color.GREEN)
        bossBar.overlay(BossBar.Overlay.PROGRESS)
    }

}

fun Player.teleportToLobby() {
    this.teleport(Bukkit.getWorld("world")!!.spawnLocation)
}

fun Player.teleportToGameCenter() {
    this.teleport(Game.spawnHandler.center)
}

fun Player.teleportToGameSpawn() {
    this.teleport(Game.spawnHandler.spawns.random().location)
}