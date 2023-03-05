package dev.lexoland.core

import dev.lexoland.LOG
import dev.lexoland.PLUGIN
import dev.lexoland.asId
import dev.lexoland.utils.*
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.entity.Player
import java.io.File
import java.util.*
import kotlin.math.ceil
import kotlin.random.asKotlinRandom

const val MIN_PLAYERS = 2

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
        if (players.size < MIN_PLAYERS) { // just in case
            GameStartCountdown.reset()
            GameStartCountdown.update()
            return
        }
        for (qsgPlayer in players.values)
            qsgPlayer.player.hideBossBar(GameStartCountdown.bossBar)
        broadcast("Das Spiel startet jetzt!", color = NamedTextColor.GRAY)
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
        if (Game.players.size < MIN_PLAYERS) {
            waiting = true
        } else if (waiting) {
            waiting = false
            reset()
        }
    }

    override fun canCountDown() = !waiting

    override fun tick() {
        if (waiting) {
            val dots = when (Bukkit.getCurrentTick() / 20 % 4) {
                0 -> "."
                1 -> ".."
                2 -> "..."
                else -> ""
            }
            bossBar.name(gradient("Warte auf Spieler$dots", rgb(0xffff00), rgb(0xffc800)))
            bossBar.progress(1f)
            bossBar.color(BossBar.Color.YELLOW)
            bossBar.overlay(BossBar.Overlay.PROGRESS)
            return
        }
        bossBar.name(gradient("Spiel startet in ", rgb(0x00ff00), rgb(0x6aff00)) + text("${ceil(timeLeft / 20f).toInt()}s", NamedTextColor.AQUA))
        bossBar.progress(timeLeft.toFloat() / initialTime)
        bossBar.color(BossBar.Color.GREEN)
        bossBar.overlay(BossBar.Overlay.PROGRESS)
        when(timeLeft) {
            30 * 20 -> broadcastRemainingTime(30)
            15 * 20 -> broadcastRemainingTime(10)
            5 * 20 -> broadcastRemainingTime(5)
            4 * 20 -> broadcastRemainingTime(4)
            3 * 20 -> broadcastRemainingTime(3)
            2 * 20 -> broadcastRemainingTime(2)
            1 * 20 -> broadcastRemainingTime(1)
        }
    }

    private fun broadcastRemainingTime(time: Int) {
        broadcast("Das Spiel startet in {}", text("${time}s", NamedTextColor.AQUA), color = NamedTextColor.GRAY)
        Bukkit.getOnlinePlayers().forEach { it.playSound(it.location, Sound.UI_BUTTON_CLICK, 1000f, 2f) }
    }

}

fun Player.teleportToLobby() {
    this.teleport(Bukkit.getWorld("world")!!.spawnLocation.blockCentered())
}

fun Player.teleportToGameCenter() {
    this.teleport(Game.spawnHandler.center)
}

fun Player.teleportToGameSpawn() {
    this.teleport(Game.spawnHandler.spawns.random().location)
}