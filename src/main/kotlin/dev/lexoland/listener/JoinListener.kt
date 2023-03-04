package dev.lexoland.listener

import dev.lexoland.core.Game
import dev.lexoland.utils.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

object JoinListener : Listener {

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        e.joinMessage(PREFIX + gradient("» ${e.player.name}", rgb(0x00ff00), rgb(0x00aa00)))
        Game.join(e.player)
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        e.quitMessage(PREFIX + gradient("« ${e.player.name}", rgb(0xff0000), rgb(0xaa0000)))
    }

}