package dev.lexoland

import dev.lexoland.cmd.buildCommand
import dev.lexoland.cmd.mapCommand
import dev.lexoland.cmd.timerCommand
import dev.lexoland.core.Game
import dev.lexoland.core.loadMaps
import dev.lexoland.core.saveMaps
import dev.lexoland.listener.BuildListener
import dev.lexoland.listener.GameListener
import dev.lexoland.listener.JoinListener
import dev.lexoland.utils.commands
import dev.lexoland.utils.listeners
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val LOG = LoggerFactory.getLogger("QSG") as Logger
lateinit var PLUGIN: QSG

fun String.asId() = NamespacedKey(PLUGIN, this)

class QSG : JavaPlugin() {

    override fun onLoad() {
        PLUGIN = this
    }

    override fun onEnable() {
        LOG.info("Starting!")
        commands(
            buildCommand,
            mapCommand,
            timerCommand
        )
        listeners(
            BuildListener,
            JoinListener,
            GameListener
        )

        Bukkit.getScheduler().runTask(this) { _ ->
            loadMaps()
            Game.setup()
        }
    }

    override fun onDisable() {
        saveMaps()
    }
}