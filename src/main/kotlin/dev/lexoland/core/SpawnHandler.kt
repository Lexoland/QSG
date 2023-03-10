package dev.lexoland.core

import dev.lexoland.utils.blockCentered
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.util.Vector
import kotlin.math.atan2

class SpawnHandler(world: World, map: Map) {

    val center = map.center.toLocation(world).blockCentered()
    val spawns = map.spawns.map { Spawn(world, it) }

    fun assignSpawn(player: Player): Boolean {
        val spawn = spawns.filter { it.isFree() }.randomOrNull() ?: return false
        spawn.teleportAndEnclose(player)
        return true
    }

    fun uncloseAll() {
        spawns.forEach { it.unclosePlayer() }
    }

    fun resetSpawn(player: Player) {
        val spawn = spawns.firstOrNull() { it.player == player } ?: return
        spawn.reset()
    }

    inner class Spawn(world: World, pos: Vector) {

        val location: Location = pos.toLocation(world).blockCentered().apply {
            val angle = atan2((z - center.z), (x - center.x))
            yaw = Math.toDegrees(angle).toFloat() + 90f
        }
        val closure = listOf(
            location.clone().add(0.0, 2.0, 0.0),
            location.clone().add(0.0, 0.0, 1.0),
            location.clone().add(0.0, 1.0, 1.0),
            location.clone().add(0.0, 0.0, -1.0),
            location.clone().add(0.0, 1.0, -1.0),
            location.clone().add(1.0, 0.0, 0.0),
            location.clone().add(1.0, 1.0, 0.0),
            location.clone().add(-1.0, 0.0, 0.0),
            location.clone().add(-1.0, 1.0, 0.0),
        )
        var player: Player? = null
        var armorStand: ArmorStand? = null

        fun reset() {
            armorStand?.remove()
            armorStand = null
            player = null
        }

        fun enclosePlayer() {
            val p = player!!
            p.teleport(location)
            closure.forEach {
                it.block.type = Material.BARRIER
            }
        }

        fun unclosePlayer() {
            closure.forEach {
                it.block.type = Material.AIR
            }
        }

        fun decorate() {
            armorStand = location.world.spawn(location.clone().subtract(0.0, 1.435, 0.0), ArmorStand::class.java) {
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

        fun teleportAndEnclose(player: Player) {
            this.player = player
            player.inventory.clear()
            player.activePotionEffects.forEach { player.removePotionEffect(it.type) }
            player.level = 0
            player.exp = 0f
            player.foodLevel = 20
            player.health = 20.0
            enclosePlayer()
            decorate()
        }

        fun isFree() = this.player == null
    }
}