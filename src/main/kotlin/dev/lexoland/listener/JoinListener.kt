package dev.lexoland.listener

import dev.lexoland.core.Game
import dev.lexoland.core.GameState
import dev.lexoland.utils.PREFIX
import dev.lexoland.utils.gradient
import dev.lexoland.utils.plus
import dev.lexoland.utils.rgb
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

object JoinListener : Listener {

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        e.joinMessage(PREFIX + gradient("» ${e.player.name}", rgb(0x00ff00), rgb(0x00aa00)))
        if (Game.initialized)
            Game.addPlayer(e.player)
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        e.quitMessage(PREFIX + gradient("« ${e.player.name}", rgb(0xff0000), rgb(0xaa0000)))
        if (Game.initialized) {
            Game.removePlayer(e.player)
            if (Game.state != GameState.LOBBY && Game.state != GameState.ENDING)
                if (!Game.spawnHandler.hasMoreThanOneSurvivor())
                    Game.endGame()
        }
    }
}