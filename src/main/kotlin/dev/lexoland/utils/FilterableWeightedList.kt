package dev.lexoland.utils

import java.util.function.Consumer
import java.util.function.Predicate
import java.util.stream.Stream
import kotlin.math.pow
import kotlin.random.Random

class FilterableWeightedList<U> {
    private val entries: MutableList<Entry<U>> = ArrayList()
    private var filter: Predicate<U>? = null

    fun addFilter(filter: Predicate<U>?) {
        if (this.filter != null) {
            this.filter = filter?.let { this.filter!!.and(it) }
        } else this.filter = filter
    }

    fun setFilter(filter: Predicate<U>?) {
        this.filter = filter
    }

    fun add(element: U, weight: Int): FilterableWeightedList<U> {
        return add(Entry(element, weight))
    }

    private fun add(entry: Entry<U>): FilterableWeightedList<U> {
        entries.add(entry)
        return this
    }

    fun shuffle(random: Random): FilterableWeightedList<U> {
        entries.forEach(Consumer { entry: Entry<U> ->
            entry.shuffledOrder = random.nextDouble()
        })
        entries.sortWith(Comparator.comparingDouble { it.shuffledOrder })
        return this
    }

    fun pickRandom(random: Random): U {
        return this.shuffle(random).stream().findFirst().orElse(null)
    }

    fun stream(): Stream<U> {
        return if (filter != null)
            entries.stream().map { it.element }.filter(filter)
        else entries.stream().map { it.element }
    }

    val isEmpty: Boolean
        get() = entries.isEmpty()

    class Entry<T> internal constructor(val element: T, private val weight: Int) {
        var shuffledOrder = 0.0
            set(random) {
                field = -random.pow((1.0 / weight))
            }

        override fun toString(): String {
            return "$weight:$element"
        }
    }
}
