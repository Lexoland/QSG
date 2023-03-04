package dev.lexoland.utils

import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

fun JavaPlugin.listeners(vararg listeners: Listener) = Bukkit.getPluginManager().apply {
    for (listener in listeners)
        registerEvents(listener, this@listeners)
}
