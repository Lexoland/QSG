package dev.lexoland.listener

import com.destroystokyo.paper.event.block.TNTPrimeEvent
import dev.lexoland.core.map
import dev.lexoland.utils.blockPosEqual
import dev.lexoland.utils.respond
import dev.lexoland.utils.textLocation
import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.bukkit.entity.TNTPrimed
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent

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
            val map = e.block.world.map
            if (map != null) {
                map.lootBoxes.add(e.block.location.toVector())
                e.player.respond("Added container to map {} at {}", map.displayName, textLocation(e.block.location))
            }
        }
    }

    @EventHandler
    fun cancelBuild(e: BlockBreakEvent) {
        if (!allowed.contains(e.player)) {
            e.isCancelled = true
        } else if (e.block.state is Container) {
            val map = e.block.world.map
            if (map != null && map.lootBoxes.any { it blockPosEqual e.block.location }) {
                map.lootBoxes.removeIf { it blockPosEqual e.block.location }
                e.player.respond("Removed container from map {} at {}", map.displayName, textLocation(e.block.location))
            }
        }
    }

    @EventHandler
    fun cancelTNTPrime(e: TNTPrimeEvent) {
        e.isCancelled = true
    }
}