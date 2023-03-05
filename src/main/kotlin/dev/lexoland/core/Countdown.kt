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
    val onFinished: () -> Unit
) {
    var timeLeft = initialTime
        set(value) {
            field = value
            start()
        }
    lateinit var task: BukkitTask

    fun start() {
        if (this::task.isInitialized && !task.isCancelled)
            return
        task = Bukkit.getScheduler().runTaskTimer(PLUGIN, Runnable {
            if (canCountDown()) {
                timeLeft--
                if (timeLeft == 0) {
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

    fun reset() {
        timeLeft = initialTime
    }

}

class SimpleCountdown(
    rate: Int = 20,
    initialTime: Int,
    val onTick: (Int) -> Unit,
    onFinished: () -> Unit
) : Countdown(rate, initialTime, onFinished) {
    override fun tick() {
        onTick(timeLeft)
    }
}

abstract class BossBarCountdown(
    players: List<Player>,
    rate: Int = 20,
    initialTime: Int,
    onFinish: () -> Unit
) : Countdown(rate, initialTime, onFinish) {
    val bossBar = BossBar.bossBar(text("-"), 1f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS)

    init {
        players.forEach { it.showBossBar(bossBar) }
    }

    fun addPlayer(player: Player) {
        player.showBossBar(bossBar)
    }

    fun removePlayer(player: Player) {
        player.hideBossBar(bossBar)
    }
}
