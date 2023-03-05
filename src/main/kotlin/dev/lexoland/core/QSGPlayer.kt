package dev.lexoland.core

import org.bukkit.GameMode
import org.bukkit.entity.Player

class QSGPlayer(
    val player: Player
) {
    var spectating = false

    fun setToSpectator() {
        spectating = true
        player.gameMode = GameMode.SPECTATOR
        player.teleportToGameCenter()
    }

    fun setToPlayer() {
        spectating = false
        player.gameMode = GameMode.SURVIVAL
        player.inventory.clear()
        player.exp = 0.0f
        player.level = 0
        player.foodLevel = 20

        if (Game.started)
            player.teleportToGameSpawn()
        else player.teleportToLobby()
    }
}

val Player.qsg get() = Game.players[uniqueId]