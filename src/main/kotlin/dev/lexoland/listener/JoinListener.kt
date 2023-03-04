package dev.lexoland.listener

import dev.lexoland.core.GameManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

private val prefix = Component.text().content("[TODO]").color(NamedTextColor.RED)

object JoinListener : Listener {

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        e.joinMessage(
            prefix
                .append(e.joinMessage()!!)
                .append(
                    Component.text()
                        .append(Component.text().content("[").color(NamedTextColor.GRAY))
                        .append(Component.text().content(Bukkit.getOnlinePlayers().size.toString()).color(NamedTextColor.GREEN))
                        .append(Component.text().content("/").color(NamedTextColor.GRAY))
                        .append(Component.text().content(Bukkit.getMaxPlayers().toString()).color(NamedTextColor.AQUA))
                        .append(Component.text().content("]").color(NamedTextColor.GRAY))
                ).build()
        )
        GameManager.join(e.player)
    }

}