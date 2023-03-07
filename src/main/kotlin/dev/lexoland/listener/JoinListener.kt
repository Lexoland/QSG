package dev.lexoland.listener

import dev.lexoland.core.Game
import dev.lexoland.utils.*
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent
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
        if (Game.initialized)
            Game.removePlayer(e.player)
    }

    @EventHandler
    fun onLogin(e: PlayerLoginEvent) {
        if (e.result == PlayerLoginEvent.Result.KICK_WHITELIST)
            e.kickMessage(text("Dieser Server ist derzeit noch im Bau! Versuch es ein anderen mal wieder :)", NamedTextColor.AQUA))
    }
}