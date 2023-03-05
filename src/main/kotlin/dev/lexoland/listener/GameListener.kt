package dev.lexoland.listener

import dev.lexoland.asId
import dev.lexoland.core.Game
import dev.lexoland.core.GameState
import dev.lexoland.utils.PREFIX
import dev.lexoland.utils.plus
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.persistence.PersistentDataType

object GameListener : Listener {

    val KILL_KEY = "qsg/kills".asId()
    val DEATH_KEY = "qsg/deaths".asId()

    @EventHandler
    fun onDeath(e: PlayerDeathEvent) {
        if(Game.state != GameState.IN_GAME)
            return
        Game.spawnHandler.onDeath(e.entity)
        e.player.gameMode = GameMode.SPECTATOR
        e.deathMessage(PREFIX + e.deathMessage()!!.color(NamedTextColor.RED))
        e.entity.persistentDataContainer[DEATH_KEY, PersistentDataType.INTEGER] = 1 + (e.entity.persistentDataContainer[DEATH_KEY, PersistentDataType.INTEGER] ?: 0)

        if(!Game.spawnHandler.hasMoreThanOneSurvivor())
            Game.endGame()
    }

    @EventHandler
    fun onDamage(e: EntityDamageEvent) {
        if(!Game.state.takeAnyDamage && !Game.state.takeNonPlayerDamage)
            e.isCancelled = true
    }

    @EventHandler
    fun onDamage(e: EntityDamageByEntityEvent) {
        val damager = e.damager
        val entity = e.entity
        if (!Game.state.takeAnyDamage && !Game.state.takeNonPlayerDamage && damager is Player)
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
}