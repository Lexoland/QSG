package dev.lexoland.listener

import com.destroystokyo.paper.event.block.TNTPrimeEvent
import dev.lexoland.core.GameManager
import dev.lexoland.core.Loot
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.bukkit.entity.TNTPrimed
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent

private val addedText = Component.text("Added container [")
private val removedText = Component.text("Removed container [")

object BuildListener : Listener {

    val allowed = mutableListOf<Player>()

    @EventHandler
    fun cancelBuild(e: BlockPlaceEvent) {
        if (!allowed.contains(e.player)) {
            e.isCancelled = true
            if (e.block.type == Material.TNT) {
                e.itemInHand.subtract()
                e.block.world.spawn(e.block.location, TNTPrimed::class.java) {
                    it.source = e.player
                }
            }
        } else if (e.block.state is Container) {
            e.player.sendMessage(textFactory(addedText, e.block.translationKey(), e.block.location))
            Loot.containers.add(e.block.state as Container)
        }
    }

    @EventHandler
    fun cancelBuild(e: BlockBreakEvent) {
        if (!allowed.contains(e.player)) {
            e.isCancelled = true
        } else if (e.block.state is Container && Loot.containers.contains(e.block.state)) {
            e.player.sendMessage(textFactory(removedText, e.block.translationKey(), e.block.location))
            Loot.containers.remove(e.block.state)
        }
    }

    @EventHandler
    fun cancelTNTPrime(e: TNTPrimeEvent) {
        e.isCancelled = true
    }

    private fun textFactory(prefix: Component, key: String, pos: Location) =
        prefix.append(
            Component.translatable(key)
        ).append(
            Component.text("] at ")
        )
            .append(
                Component.join(
                    JoinConfiguration.commas(true),
                    Component.text(pos.x, NamedTextColor.AQUA),
                    Component.text(pos.y, NamedTextColor.AQUA),
                    Component.text(pos.z, NamedTextColor.AQUA)
                )
            )
}