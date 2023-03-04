package dev.lexoland

import de.leximon.api.command.CommandAPI
import dev.lexoland.cmd.buildCommand
import dev.lexoland.listener.BuildListener
import dev.lexoland.utils.commands
import dev.lexoland.utils.listeners
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.slf4j.LoggerFactory

class QSG : JavaPlugin() {

    val log = LoggerFactory.getLogger("QSG")

    override fun onLoad() {
        plugin = this
    }

    override fun onEnable() {
        log.info("Starting!")

        commands(buildCommand)
        listeners(BuildListener)
    }

    companion object {
        lateinit var plugin: QSG
    }
}