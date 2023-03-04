package dev.lexoland.core

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

object SpawnHandler {

    val spawns = mutableListOf<Spawn>()

    fun prepareSpawnFor(player: Player) {
        val spawn = spawns.filter { it.isFree() }.random()
        spawn.assignTo(player)
        spawn.decorate()
        player.teleport(spawn.location)
    }

    class Spawn(
        val location: Location
    ) {

        private var player: Player? = null
        private var armorStand: ArmorStand? = null

        fun reset() {
            armorStand?.remove()
            armorStand = null
            player = null
        }

        fun assignTo(player: Player) {
            this.player = player
        }

        fun decorate() {
            armorStand = location.world.spawn(location.subtract(0.0, 1.6, 0.0), ArmorStand::class.java) {
                it.isVisible = false
                it.isInvulnerable = true
                it.isMarker = true
                it.setGravity(false)
                it.equipment.helmet = ItemStack(Material.PLAYER_HEAD, 1).apply {
                    itemMeta = itemMeta.apply {
                        (this as SkullMeta).playerProfile = player?.playerProfile
                    }
                }
            }
        }

        fun isFree() = this.player == null

    }
}