package dev.lexoland.core

import dev.lexoland.asId
import dev.lexoland.core.Game.randomSource
import dev.lexoland.utils.FilterableWeightedList
import dev.lexoland.utils.rgb
import dev.lexoland.utils.text
import java.util.Locale
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.block.Container
import org.bukkit.loot.Lootable

class LootBoxHandler(world: World, map: Map) {

    private val lootBoxes = map.lootBoxes.map { it.toLocation(world) }

    fun setup() {
        lootBoxes.map { it.block.state }.filterIsInstance<Container>().forEach {
            it.inventory.clear()
            if (it !is Lootable)
                throw IllegalStateException("Container can't posses a loottable")
            val chest = weightedLoot.pickRandom(randomSource)
            it.lootTable = chest.toLootTable()
            it.seed = randomSource.nextLong()
            it.customName(chest.customName().appendSpace().append(Component.translatable(it.block.type.translationKey())))
        }
    }

    private val weightedLoot = FilterableWeightedList<Chest>()
        .add(Chest(Type.CRAP, "chest_1"), 20)
        .add(Chest(Type.CRAP, "chest_2"), 20)

        .add(Chest(Type.BASIC, "chest_1"), 10)
        .add(Chest(Type.BASIC, "chest_2"), 10)

        .add(Chest(Type.SIGMA, "chest_1"), 5)
        .add(Chest(Type.SIGMA, "chest_2"), 5)

    inner class Chest(
        private val type: Type,
        private val key: String
    ) {
        fun customName() = text(type.name.lowercase(Locale.getDefault()).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }, type.color)
        fun toLootTable() = Bukkit.getLootTable("${type.key}/$key".asId())
    }

    enum class Type(
        val key: String,
        val color: TextColor
    ) {
        CRAP("crap", rgb(0x824800)),
        BASIC("basic", rgb(0x008ae6)),
        SIGMA("sigma", rgb(0x00e01a))
    }
}
