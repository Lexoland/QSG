package dev.lexoland.core

import dev.lexoland.PLUGIN
import dev.lexoland.core.GameManager.randomSource
import dev.lexoland.utils.FilterableWeightedList
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.block.Container
import org.bukkit.loot.Lootable

class LootBoxHandler(
    private val lootBoxes: List<LootBox>
) {

    fun setup() {
        lootBoxes.map { it.container }.forEach {
            it.inventory.clear()
            if (it !is Lootable)
                throw IllegalStateException("Container can't posses a loottable")
            it.lootTable = Bukkit.getLootTable(weightedLoot.pickRandom(randomSource))
            it.seed = randomSource.nextLong()
        }
    }

    private val weightedLoot = FilterableWeightedList<NamespacedKey>()
        .add("bad_combat".key(), 20)
        .add("bad_supply".key(), 20)
        .add("bad_food".key(), 20)
        .add("bad_armor".key(), 20)

        .add("normal_combat".key(), 10)
        .add("normal_supply".key(), 10)
        .add("normal_food".key(), 10)
        .add("normal_armor".key(), 10)

        .add("good_combat".key(), 5)
        .add("good_supply".key(), 5)
        .add("good_food".key(), 5)
        .add("good_armor".key(), 5)

    private fun String.key() = NamespacedKey(PLUGIN, "qsg/$this")
}

@Serializable
class LootBox(
    @Contextual val location: Location
) {
    val container get() = location.block.state as Container
}