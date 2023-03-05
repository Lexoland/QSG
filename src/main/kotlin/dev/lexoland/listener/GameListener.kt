package dev.lexoland.listener

import dev.lexoland.asId
import dev.lexoland.core.Game
import dev.lexoland.core.GameState
import dev.lexoland.core.qsg
import dev.lexoland.core.teleportToGameCenter
import dev.lexoland.utils.*
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.*
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.persistence.PersistentDataType

object GameListener : Listener {

    val KILL_KEY = "qsg/kills".asId()
    val DEATH_KEY = "qsg/deaths".asId()

    @EventHandler
    fun onDeath(e: PlayerDeathEvent) {
        if(Game.state != GameState.IN_GAME)
            return
        val player = e.player
        val qsgPlayer = player.qsg ?: return

        e.isCancelled = true
        e.droppedExp += 5
        broadcast(PREFIX + text("☠ ", rgb(0xFF0000)) + e.deathMessage()!!.color(rgb(0xaa0000)))
        Game.spawnHandler.resetSpawn(player)
        player.persistentDataContainer[DEATH_KEY, PersistentDataType.INTEGER] = 1 + (player.persistentDataContainer[DEATH_KEY, PersistentDataType.INTEGER] ?: 0)
        player.dropInventory()

        qsgPlayer.setToSpectator()
        if (player.lastDamageCause?.cause == EntityDamageEvent.DamageCause.VOID)
            player.teleportToGameCenter()

        if(!Game.moreThanOneSurvivorAlive())
            Game.endGame(e.player.killer)
    }

    @EventHandler
    fun onDamage(e: EntityDamageEvent) {
        val entity = e.entity
        if (entity is Player && entity.qsg?.spectating == true && e.cause == EntityDamageEvent.DamageCause.VOID) {
            e.isCancelled = true
            entity.teleportToGameCenter()
            return
        }
        if(!Game.state.takeAnyDamage && !Game.state.takeNonPlayerDamage)
            e.isCancelled = true
    }

    @EventHandler
    fun onDamage(e: EntityDamageByEntityEvent) {
        val damager = e.damager
        val entity = e.entity
        if (!Game.state.takeAnyDamage && Game.state.takeNonPlayerDamage && (damager is Player || (damager is Projectile && damager.shooter is Player)))
            e.isCancelled = true
        if(Game.state == GameState.IN_GAME) {
            if (damager is Player && entity is Player && e.finalDamage > entity.health)
                e.damager.persistentDataContainer[KILL_KEY, PersistentDataType.INTEGER] = 1 + (e.damager.persistentDataContainer[KILL_KEY, PersistentDataType.INTEGER] ?: 0)
        }
    }

    @EventHandler
    fun cancelFood(e: FoodLevelChangeEvent) {
        if(Game.state.hunger)
            return
        e.isCancelled = true
    }

    @EventHandler
    fun cancelChestOpen(e: InventoryOpenEvent) {
        if(e.inventory.holder != e.player && !(Game.state.openContainers))
            e.isCancelled = true
    }

    @EventHandler
    fun onExplode(e: EntityExplodeEvent) {
        e.blockList().clear()
    }
}