package dev.lexoland.utils

import dev.lexoland.PLUGIN
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.WorldType
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

fun JavaPlugin.listeners(vararg listeners: Listener) = Bukkit.getPluginManager().apply {
    for (listener in listeners)
        registerEvents(listener, this@listeners)
}

fun WorldCreator.presetVoid() = apply {
    environment(World.Environment.NORMAL)
    type(WorldType.FLAT)
    generateStructures(false)
    generatorSettings("{\"lakes\": false, \"features\": true, \"biome\": \"minecraft:the_void\", \"structure_overrides\": [], \"layers\": [{\"height\": 1, \"block\": \"minecraft:air\"}]}")
    biomeProvider(null as String?)
}