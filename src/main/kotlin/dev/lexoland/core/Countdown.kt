package dev.lexoland.core

import dev.lexoland.PLUGIN
import dev.lexoland.utils.text
import net.kyori.adventure.bossbar.BossBar
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask

abstract class Countdown(
    val rate: Int = 20,
    val initialTime: Int,
    val onFinished: () -> Unit,
    start: Boolean = false
) {
    var timeLeft = initialTime + 1
        set(value) {
            field = value
            start()
        }
    lateinit var task: BukkitTask

    init {
        if (start)
            start()
    }

    fun start() {
        if (this::task.isInitialized && !task.isCancelled)
            return
        task = Bukkit.getScheduler().runTaskTimer(PLUGIN, Runnable {
            if (canCountDown()) {
                timeLeft--
                if (timeLeft == 0) {
                    finished()
                    onFinished()
                    task.cancel()
                    return@Runnable
                }
            }
            tick()
        }, 0, rate.toLong())
    }

    open fun canCountDown() = true

    abstract fun tick()

    protected open fun finished() {}

    fun reset() {
        timeLeft = initialTime + 1
    }

}

class SimpleCountdown(
    rate: Int = 20,
    initialTime: Int,
    val onTick: (Int) -> Unit,
    onFinished: () -> Unit,
    start: Boolean = false
) : Countdown(rate, initialTime, onFinished, start) {
    override fun tick() {
        onTick(timeLeft)
    }
}

abstract class BossBarCountdown(
    players: List<Player>,
    rate: Int = 20,
    initialTime: Int,
    onFinish: () -> Unit,
    start: Boolean = false
) : Countdown(rate, initialTime, onFinish, start) {
    val players = players.toMutableList()
    val bossBar = BossBar.bossBar(text("-"), 1f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS)

    init {
        players.forEach { it.showBossBar(bossBar) }
    }

    override fun finished() {
        players.forEach { it.hideBossBar(bossBar) }
    }

    fun addPlayer(player: Player) {
        players.add(player)
        player.showBossBar(bossBar)
    }

    fun removePlayer(player: Player) {
        players.remove(player)
        player.hideBossBar(bossBar)
    }
}
