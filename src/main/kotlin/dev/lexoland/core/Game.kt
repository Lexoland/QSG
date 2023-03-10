package dev.lexoland.core

import dev.lexoland.LOG
import dev.lexoland.asKey
import dev.lexoland.utils.*
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import java.io.File
import java.util.*
import kotlin.math.ceil
import kotlin.random.asKotlinRandom
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

val IGNORE_KEY = "ignore".asKey()
const val MIN_PLAYERS = 2

object Game {

    val randomSource = Random().asKotlinRandom()

    lateinit var gameWorld: World
    lateinit var map: Map
    lateinit var lootBoxHandler: LootBoxHandler
    lateinit var spawnHandler: SpawnHandler

    var state = GameState.LOBBY
    val players = mutableMapOf<UUID, QSGPlayer>()

    val gameStartCountdown = GameStartCountdown()
    var preparationCountdown: SimpleCountdown? = null
    var safeTimeCountdown: SafeTimeCountdown? = null
    var worldBorderCountdown: WorldBorderCountdown? = null
    var drawCountdown: DrawCountdown? = null

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
        val ignore = player.persistentDataContainer.getOrDefault(IGNORE_KEY, PersistentDataType.BYTE, 0.toByte()) == 1.toByte()
        if (ignore)
            return

        val qsgPlayer = QSGPlayer(player)
        players[player.uniqueId] = qsgPlayer
        if (state != GameState.LOBBY) {
            safeTimeCountdown?.takeIf { it.running }?.addPlayer(player)
            qsgPlayer.setToSpectator()
            return
        }
        qsgPlayer.setToPlayer()
        gameStartCountdown.update()
        gameStartCountdown.addPlayer(player)
    }

    fun removePlayer(player: Player) {
        players.remove(player.uniqueId)
        gameStartCountdown.removePlayer(player)
        gameStartCountdown.update()
        safeTimeCountdown?.removePlayer(player)
        spawnHandler.resetSpawn(player)

        if (state != GameState.LOBBY && state != GameState.ENDING)
            if (!moreThanOneSurvivorAlive())
                endGame()
    }

    fun startGame() {
        if (players.size < MIN_PLAYERS) { // just in case
            gameStartCountdown.reset()
            gameStartCountdown.update()
            return
        }
        broadcast("Das Spiel startet jetzt!", color = NamedTextColor.GRAY)
        players.values.shuffled().forEach {
            if (!spawnHandler.assignSpawn(it.player))
                it.setToSpectator()
        }
        lootBoxHandler.setup()
        state = GameState.PREPARATION

        fun display(time: Int, large: Boolean) = eachPlayers {
            val color = hsv((1f - time / 10f) * 0.33f, 1f, 1f)
            if (large) {
                it.showTitle(Title.title(
                    text(if (time == 0) "Los!" else time, color = color),
                    Component.space(),
                    times(0.seconds, 0.seconds, 1.seconds)
                ))
                it.playSound(it.location, Sound.BLOCK_NOTE_BLOCK_PLING, 100f, if (time == 0) 1f else 0f)
                return@eachPlayers
            }
            it.showTitle(Title.title(
                Component.space(),
                text(time, color = color),
                times(0.seconds, 2.seconds, 0.seconds)
            ))
        }

        preparationCountdown = SimpleCountdown(20, 10, { time ->
            display(time, time <= 3)
        }, {
            display(0, true)
            state = GameState.SAFE_TIME
            spawnHandler.uncloseAll()

            worldBorderCountdown = WorldBorderCountdown()
            safeTimeCountdown = SafeTimeCountdown()
        }, start = true)
    }

    fun endGame(winner: Player? = players.values.firstOrNull { !it.spectating }?.player) {
        state = GameState.ENDING
        preparationCountdown?.stop()
        safeTimeCountdown?.stop()
        worldBorderCountdown?.stop()
        drawCountdown?.stop()

        val winnerName = winner?.name ?: "Niemand"

        eachPlayers {
            it.showTitle(Title.title(
                gradient("Das Spiel ist vorbei!", rgb(0xffc800), rgb(0xffa600)),
                gradient("Der Gewinner ist ", rgb(0xff8400), rgb(0xff7300)) + gradient(winnerName, rgb(0x00c8ff), rgb(0x009954)),
                times(500.milliseconds, 4.seconds, 500.milliseconds)
            ))
        }

        SimpleCountdown(20, 5, { time ->
            broadcast("Das Spiel endet in {}", text("${time}s", NamedTextColor.RED), color = NamedTextColor.GRAY)
        }, {
            restart()
            broadcast("Das Spiel wurde beendet!", color = NamedTextColor.GRAY)
        }).start()
    }

    private fun restart() {
        state = GameState.LOBBY
        eachPlayers { it.teleportToLobby() }
        setup()
        gameStartCountdown.reset()
        players.clear()
        Bukkit.getOnlinePlayers().forEach { addPlayer(it) }
    }

    fun moreThanOneSurvivorAlive() = players.values.count { !it.spectating } > 1

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
        return Bukkit.createWorld(WorldCreator.ofKey(GAME_WORLD_NAME.asKey()))
    }

    inline fun eachPlayers(block: (Player) -> Unit) = players.values.forEach { block(it.player) }
}


class GameStartCountdown : BossBarCountdown(
    Game.players.values.map { it.player },
    1, 30 * 20,
    Game::startGame,
    start = true
) {
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
            30 * 20 - 1 -> broadcastRemainingTime(30)
            15 * 20 -> broadcastRemainingTime(15)
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

class SafeTimeCountdown : BossBarCountdown(
    Game.players.values.map { it.player },
    1, 30 * 20,
    { Game.state = GameState.IN_GAME },
    start = true
) {
    override fun tick() {
        bossBar.name(gradient("Sicherheitszeit endet in ", rgb(0xff0000), rgb(0xbb0000)) + text("${ceil(timeLeft / 20f).toInt()}s", rgb(0xff7777)))
        bossBar.progress(timeLeft.toFloat() / initialTime)
        bossBar.color(BossBar.Color.RED)
        bossBar.overlay(BossBar.Overlay.PROGRESS)
    }
}

enum class GameState(
    val openContainers: Boolean = false,
    val takeNonPlayerDamage: Boolean = false,
    val takeAnyDamage: Boolean = false,
    val canBuild: Boolean = false,
    val hunger: Boolean = false
) {
    LOBBY,
    PREPARATION,
    SAFE_TIME(openContainers = true, takeNonPlayerDamage = true, canBuild = true, hunger = true),
    IN_GAME(openContainers = true, takeAnyDamage = true, canBuild = true, hunger = true),
    ENDING
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