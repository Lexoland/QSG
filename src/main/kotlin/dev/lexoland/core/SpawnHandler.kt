package dev.lexoland.core

import kotlin.math.atan
import kotlin.math.tan
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class SpawnHandler(
    private val center: Location,
    private val spawns: MutableList<Spawn>
) {

    fun prepareSpawnFor(player: Player) {
        val spawn = spawns.filter { it.isFree() }.random()
        spawn.assignTo(player)
        spawn.decorate()
        player.teleport(spawn.loc)
    }

    inner class Spawn(
        location: Location,
    ) {

        val loc = calcRotationPos(location)

        private fun calcRotationPos(location: Location): Location {
            val loc = location.clone()
            val center = center.clone()

            val angle = atan(tan((loc.x - center.x) / (loc.z - center.z)))
            loc.yaw = Math.toDegrees(angle).toFloat()

            return loc
        }

        private val closure = listOf(
            loc.clone().add(0.0, 2.0, 0.0),
            loc.clone().add(0.0, 0.0, 1.0),
            loc.clone().add(0.0, 1.0, 1.0),
            loc.clone().add(0.0, 0.0, -1.0),
            loc.clone().add(0.0, 1.0, -1.0),
            loc.clone().add(1.0, 0.0, 0.0),
            loc.clone().add(1.0, 1.0, 0.0),
            loc.clone().add(-1.0, 0.0, 0.0),
            loc.clone().add(-1.0, 1.0, 0.0),
        )

        private var player: Player? = null
        private var armorStand: ArmorStand? = null

        fun reset() {
            armorStand?.remove()
            armorStand = null
            player = null
        }

        fun onDeath() {
            armorStand?.remove()
            armorStand = null
        }

        fun enclose() {
            val p = player!!
            p.teleport(loc)
            p.addPotionEffect(PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 255))
            closure.forEach {
                it.block.type = Material.BARRIER
            }
        }

        fun unclose() {
            val p = player!!
            p.removePotionEffect(PotionEffectType.JUMP)
            closure.forEach {
                it.block.type = Material.AIR
            }
        }

        fun assignTo(player: Player) {
            this.player = player
        }

        fun decorate() {
            armorStand = loc.world.spawn(loc.clone().subtract(0.0, 1.6, 0.0), ArmorStand::class.java) {
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