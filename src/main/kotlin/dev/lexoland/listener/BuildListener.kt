package dev.lexoland.listener

import com.destroystokyo.paper.event.block.TNTPrimeEvent
import dev.lexoland.core.map
import dev.lexoland.utils.blockPosEqual
import dev.lexoland.utils.text
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.TextComponent
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

private val addedText = Component.text().content("Added container to map \"")
private val removedText = Component.text().content("Removed container from map \"")

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
                e.player.sendMessage(textFactory(addedText, map.displayName, e.block.translationKey(), e.block.location))
                map.lootBoxes.add(e.block.location.toVector())
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
                e.player.sendMessage(textFactory(removedText, map.displayName, e.block.translationKey(), e.block.location))
                map.lootBoxes.removeIf { it blockPosEqual e.block.location }
            }
        }
    }

    @EventHandler
    fun cancelTNTPrime(e: TNTPrimeEvent) {
        e.isCancelled = true
    }

    private fun textFactory(prefix: TextComponent.Builder, world: Component, key: String, pos: Location) =
        prefix
            .append(
                world
            )
            .append(
                text("\": [")
            ).append(
                Component.translatable(key)
            ).append(
                text("] at ")
            )
            .append(
                Component.join(
                    JoinConfiguration.commas(true),
                    text(pos.x, NamedTextColor.AQUA),
                    text(pos.y, NamedTextColor.AQUA),
                    text(pos.z, NamedTextColor.AQUA)
                )
            )
}