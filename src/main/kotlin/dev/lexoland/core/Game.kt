package dev.lexoland.core

import dev.lexoland.LOG
import dev.lexoland.PLUGIN
import dev.lexoland.asId
import dev.lexoland.utils.blockCentered
import dev.lexoland.utils.broadcast
import dev.lexoland.utils.gradient
import dev.lexoland.utils.hsv
import dev.lexoland.utils.plus
import dev.lexoland.utils.rgb
import dev.lexoland.utils.text
import dev.lexoland.utils.times
import java.io.File
import java.util.Random
import java.util.UUID
import kotlin.math.ceil
import kotlin.random.asKotlinRandom
import kotlin.time.Duration.Companion.seconds
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.entity.Player

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
    lateinit var safeTimeCountdown: SafeTimeCountdown
    lateinit var worldBorderCountdown: WorldBorderCountdown

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
        if (state != GameState.LOBBY) {
            safeTimeCountdown?.let { player.showBossBar(it.bossBar) }
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
    }

    fun startGame() {
        if (players.size < MIN_PLAYERS) { // just in case
            gameStartCountdown.reset()
            gameStartCountdown.update()
            return
        }
        broadcast("Das Spiel startet jetzt!", color = NamedTextColor.GRAY)
        eachPlayers { spawnHandler.assignSpawn(it) }
        lootBoxHandler.setup()
        state = GameState.PREPARATION

        fun display(time: Int, large: Boolean) = eachPlayers {
            val color = hsv(time / 10f * 0.33f, 1f, 1f)
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

        SimpleCountdown(20, 10, { time ->
            display(time, time <= 3)
        }, {
            display(0, true)
            state = GameState.SAFE_TIME
            spawnHandler.uncloseAll()

            worldBorderCountdown = WorldBorderCountdown()
            safeTimeCountdown = SafeTimeCountdown()
        }).start()
    }

    fun endGame() {
        state = GameState.ENDING
        worldBorderCountdown.stop()

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