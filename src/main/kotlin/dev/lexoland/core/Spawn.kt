package dev.lexoland.core

import kotlin.math.atan
import kotlin.math.tan
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
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
    val spawns: MutableList<Spawn>
) {
    fun prepareSpawnFor(player: Player) {
        val spawn = spawns.filter { it.isFree() }.random()
        spawn.assignTo(player)

        spawn.decorate()
        player.teleport(spawn.location.clone().apply {
            val angle = atan(tan((x - center.x) / (z - center.z)))
            yaw = Math.toDegrees(angle).toFloat()
        })
    }

    fun hasFreeSpawns() = spawns.any { it.isFree() }

    fun hasMoreThanOneSurvivor() = spawns.count { !it.isFree() } > 1

    fun onDeath(player: Player) {
        val spawn = spawns.first { it.player == player }
        spawn.reset()
    }
}

@Serializable
class Spawn(@Contextual val location: Location) {

    @Transient
    private val closure = listOf(
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

    @Transient
    var player: Player? = null
    @Transient private var armorStand: ArmorStand? = null

    fun reset() {
        armorStand?.remove()
        armorStand = null
        player = null
    }

    fun enclose() {
        val p = player!!
        p.teleport(location)
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
        armorStand = location.world.spawn(location.clone().subtract(0.0, 1.6, 0.0), ArmorStand::class.java) {
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