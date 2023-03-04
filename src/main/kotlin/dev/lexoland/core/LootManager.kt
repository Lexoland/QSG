package dev.lexoland.core

import org.bukkit.block.Container
import org.bukkit.loot.LootTable
import org.bukkit.loot.Lootable

object LootManager {

    val containers = mutableListOf<Container>()

    fun setup() {
        containers.forEach {
            it.inventory.clear()
            if (it !is Lootable)
                throw IllegalStateException("Container can't posses a loottable")
            it.lootTable = getRandomLoottable()
        }
    }

    private fun getRandomLoottable(): LootTable {
        TODO("NOT IMPLEMENTED")
    }
}