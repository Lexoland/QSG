package dev.lexoland

import dev.lexoland.cmd.buildCommand
import dev.lexoland.core.MapData
import dev.lexoland.listener.BuildListener
import dev.lexoland.utils.commands
import dev.lexoland.utils.listeners
import java.util.UUID
import net.kyori.adventure.text.Component
import org.bukkit.World
import org.bukkit.plugin.java.JavaPlugin
import org.slf4j.LoggerFactory

class QSG : JavaPlugin() {

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
        val log = LoggerFactory.getLogger("QSG")

        private val worldToMap = mutableMapOf<UUID, MapData>()

        fun registerMap(world: World, name: Component) {
            worldToMap[world.uid] = MapData(world, name)
        }

        fun getMap(world: World): MapData? {
            return worldToMap[world.uid]
        }
    }
}