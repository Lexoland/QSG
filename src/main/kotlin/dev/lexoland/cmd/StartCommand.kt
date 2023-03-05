package dev.lexoland.cmd

import dev.lexoland.core.Game
import dev.lexoland.utils.brigadierCommand
import dev.lexoland.utils.literal

val timerCommand = brigadierCommand(
    "timer",
    permissions = arrayOf("qsg.command.timer")
) {
    literal("start") {
        executes { _, _ ->
            Game.gameStartCountdown.timeLeft = 1
            return@executes 1
        }
    }

    literal("stop") {
        executes { _, _ ->
            Game.gameStartCountdown.waiting = true
            return@executes 1
        }
    }
}