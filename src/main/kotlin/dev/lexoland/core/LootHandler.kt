package dev.lexoland.core

import dev.lexoland.QSG
import dev.lexoland.core.GameManager.randomSource
import dev.lexoland.utils.FilterableWeightedList
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.block.Container
import org.bukkit.loot.Lootable

class LootHandler(
    private val containers: List<Container>
) {

    fun setup() {
        containers.forEach {
            it.inventory.clear()
            if (it !is Lootable)
                throw IllegalStateException("Container can't posses a loottable")
            it.lootTable = Bukkit.getLootTable(weightedLoot.pickRandom(randomSource))
            it.seed = randomSource.nextLong()
        }
    }

    private val weightedLoot = FilterableWeightedList<NamespacedKey>()
        .add(key("bad_combat"), 20)
        .add(key("bad_supply"), 20)
        .add(key("bad_food"), 20)
        .add(key("bad_armor"), 20)

        .add(key("normal_combat"), 10)
        .add(key("normal_supply"), 10)
        .add(key("normal_food"), 10)
        .add(key("normal_armor"), 10)

        .add(key("good_combat"), 5)
        .add(key("good_supply"), 5)
        .add(key("good_food"), 5)
        .add(key("good_armor"), 5)

    private fun key(key: String) = NamespacedKey(QSG.plugin, "qsg/$key")
}