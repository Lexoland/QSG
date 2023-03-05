package dev.lexoland.core

import dev.lexoland.PLUGIN
import dev.lexoland.utils.gradient
import dev.lexoland.utils.plus
import dev.lexoland.utils.rgb
import dev.lexoland.utils.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit

class WorldBorderCountdown : Countdown(20, 60 * 5, ::onFinish, start = true) {
    override fun tick() {
        Game.eachPlayers {
            val time = when {
                timeLeft > 60 -> "${timeLeft / 60}m ${timeLeft % 60}s"
                else -> "${timeLeft}s"
            }
            it.sendActionBar(
                gradient("Die Weltborder verkleinert sich in ", rgb(0x2aa0f5), rgb(0x028bed)) + text(time, NamedTextColor.AQUA))
        }
    }
}

private fun onFinish() {
    Game.gameWorld.worldBorder.setSize(10.0, 30)
    Bukkit.getScheduler().runTaskLater(PLUGIN, Runnable {
        Game.drawCountdown = DrawCountdown()
    }, 30 * 20)
}

class DrawCountdown : Countdown(20, 30, ::onDraw, start = true) {
    override fun tick() {
        Game.eachPlayers {
            it.sendActionBar(
                gradient("Das Spiel endet in einem Unentschieden in ", rgb(0xed3700), rgb(0xc40000)) + text("${timeLeft}s", NamedTextColor.RED))
        }
    }
}

private fun onDraw() {
    Game.endGame()
}
