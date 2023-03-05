package dev.lexoland.core

import dev.lexoland.PLUGIN
import dev.lexoland.utils.gradient
import dev.lexoland.utils.rgb
import dev.lexoland.utils.text
import java.util.concurrent.TimeUnit
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit

class WorldBorderCountdown : Countdown(20, 60 * 5, ::onFinish) {
    override fun tick() {
        Game.players.values.forEach {
            val time = when {
                timeLeft > 20 * 60 -> "${timeLeft / 60}m ${timeLeft % 60}s"
                else -> "${timeLeft}s"
            }
            it.player.sendActionBar(
                gradient("Die Weltborder verkleinert sich in ", rgb(0x028bed), rgb(0x0052c4))
                    .append(text(time, NamedTextColor.AQUA))
            )
        }
    }
}

private fun onFinish() {
    Game.gameWorld.worldBorder.setSize(10.0, 30)
    Bukkit.getScheduler().runTaskLater(PLUGIN, Runnable {
        SuddenDeathCountdown().start()
    }, 30 * 20)
}

class SuddenDeathCountdown : Countdown(20, 30, ::onSuddenDeath) {
    override fun tick() {
        Game.players.values.forEach {
            it.player.sendActionBar(
                gradient("SuddenDeath started in ", rgb(0xed3700), rgb(0xc40000))
                    .append(text("${timeLeft}s", NamedTextColor.RED))
            )
        }
    }
}

private fun onSuddenDeath() {
    Game.gameWorld.worldBorder.setSize(0.0, TimeUnit.MILLISECONDS, 7500)
}
