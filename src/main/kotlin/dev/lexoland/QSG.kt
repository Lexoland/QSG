package dev.lexoland

import dev.lexoland.cmd.buildCommand
import dev.lexoland.cmd.spawnCommand
import dev.lexoland.listener.BuildListener
import dev.lexoland.utils.commands
import dev.lexoland.utils.listeners
import org.bukkit.plugin.java.JavaPlugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val LOG = LoggerFactory.getLogger("QSG") as Logger
lateinit var PLUGIN: QSG

class QSG : JavaPlugin() {

    override fun onLoad() {
        PLUGIN = this
    }

    override fun onEnable() {
        LOG.info("Starting!")

        commands(
            buildCommand,
            spawnCommand
        )
        listeners(BuildListener)
    }
}