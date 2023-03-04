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
        .add("bad/chest_1".key(), 20)
        .add("bad/chest_2".key(), 20)

        .add("normal/chest_1".key(), 10)
        .add("normal/chest_2".key(), 10)

        .add("good/chest_1".key(), 5)
        .add("good/chest_2".key(), 5)

    private fun String.key() = NamespacedKey(PLUGIN, "qsg/$this")
}

@Serializable
class LootBox(
    @Contextual val location: Location
) {
    val container get() = location.block.state as Container
}