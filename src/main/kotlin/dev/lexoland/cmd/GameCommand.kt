package dev.lexoland.cmd

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import dev.lexoland.core.Game
import dev.lexoland.core.IGNORE_KEY
import dev.lexoland.utils.brigadierCommand
import dev.lexoland.utils.executes
import dev.lexoland.utils.literal
import dev.lexoland.utils.respond
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

val gameCommand = brigadierCommand(
    "game",
    permissions = arrayOf("qsg.command.game")
) {

    literal("join") {
        executes<Player> { sender, _ ->
            if (Game.players.containsKey(sender.uniqueId))
                throw SimpleCommandExceptionType { "You are already in the game!" }.create()
            sender.persistentDataContainer.set(IGNORE_KEY, PersistentDataType.BYTE, 0.toByte())
            Game.addPlayer(sender)
            sender.respond("You've joined the game!")
            return@executes 1
        }
    }

    literal("leave") {
        executes<Player> { sender, _ ->
            if (Game.players.containsKey(sender.uniqueId))
                throw SimpleCommandExceptionType { "You are not in the game!" }.create()
            sender.persistentDataContainer.set(IGNORE_KEY, PersistentDataType.BYTE, 1.toByte())
            Game.removePlayer(sender)
            sender.respond("You've left the game!")
            return@executes 1
        }
    }

    literal("timer") {
        literal("start") {
            executes { sender, _ ->
                Game.gameStartCountdown.timeLeft = 1
                sender.respond("The game will start now!")
                return@executes 1
            }
        }

        literal("stop") {
            executes { sender, _ ->
                Game.gameStartCountdown.waiting = true
                sender.respond("Game start has been cancelled!")
                return@executes 1
            }
        }
    }
}