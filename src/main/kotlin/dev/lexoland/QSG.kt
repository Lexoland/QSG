package dev.lexoland

import dev.lexoland.cmd.buildCommand
import dev.lexoland.cmd.mapCommand
import dev.lexoland.cmd.spawnCommand
import dev.lexoland.core.MapData
import dev.lexoland.listener.BuildListener
import dev.lexoland.listener.JoinListener
import dev.lexoland.utils.commands
import dev.lexoland.utils.listeners
import java.util.UUID
import org.bukkit.World
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
            spawnCommand,
            mapCommand
        )
        listeners(
            BuildListener,
            JoinListener
        )
    }

    companion object {
        private val worldToMap = mutableMapOf<UUID, MapData>()

        fun registerMap(map: MapData) = worldToMap.put(map.world.uid, map)

        fun getMap(world: World) = worldToMap[world.uid]
    }
}