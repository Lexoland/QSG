package dev.lexoland.listener

import dev.lexoland.asId
import dev.lexoland.core.GameManager
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
        if(!GameManager.inGame)
            return
        GameManager.spawnHandler.onDeath(e.entity)
        e.player.gameMode = GameMode.SPECTATOR
        e.deathMessage(PREFIX + e.deathMessage()!!.color(NamedTextColor.RED))
        e.entity.persistentDataContainer[DEATH_KEY, PersistentDataType.INTEGER] = 1 + (e.entity.persistentDataContainer[DEATH_KEY, PersistentDataType.INTEGER] ?: 0)

        if(!GameManager.spawnHandler.hasMoreThanOneSurvivor())
            GameManager.endGame()
    }

    @EventHandler
    fun cancelDamage(e: EntityDamageEvent) {
        if(!GameManager.inGame)
            e.isCancelled = true
    }

    @EventHandler
    fun cancelDamage(e: EntityDamageByEntityEvent) {
        if(!GameManager.preparation)
            return
        val damager = e.damager
        val entity = e.entity
        if(damager is Player && entity is Player)
            if(e.finalDamage > entity.health)
                e.damager.persistentDataContainer[KILL_KEY, PersistentDataType.INTEGER] = 1 + (e.damager.persistentDataContainer[KILL_KEY, PersistentDataType.INTEGER] ?: 0)
    }

    @EventHandler
    fun cancelFood(e: FoodLevelChangeEvent) {
        if(!GameManager.inGame)
            return
        e.isCancelled = true
    }

    @EventHandler
    fun cancelChestOpen(e: InventoryOpenEvent) {
        if(e.inventory.holder != e.player && !GameManager.started)
            e.isCancelled = true
    }
}