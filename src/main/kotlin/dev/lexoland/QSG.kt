package dev.lexoland

import dev.lexoland.cmd.BuildCommand
import dev.lexoland.listener.BuildListener
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
        val pm = Bukkit.getPluginManager()
        pm.registerEvents(BuildListener, this)
        getCommand("build")!!.setExecutor(BuildCommand)
    }

    companion object {
        lateinit var plugin: QSG
    }
}