package dev.lexoland.core

import dev.lexoland.PLUGIN
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask

abstract class Countdown(
    val rate: Int = 20,
    val initialTime: Int,
    val onFinished: () -> Unit
) {
    var timeLeft = initialTime
        set(value) {
            field = value
            startTask()
        }
    lateinit var task: BukkitTask

    init { startTask() }

    private fun startTask() {
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