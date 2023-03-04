package dev.lexoland.cmd

import com.mojang.brigadier.arguments.StringArgumentType
import dev.lexoland.PLUGIN
import dev.lexoland.asId
import dev.lexoland.core.Map
import dev.lexoland.core.Spawn
import dev.lexoland.core.map
import dev.lexoland.core.registerMap
import dev.lexoland.utils.argument
import dev.lexoland.utils.brigadierCommand
import dev.lexoland.utils.executes
import dev.lexoland.utils.literal
import dev.lexoland.utils.respond
import dev.lexoland.utils.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.WorldType
import org.bukkit.entity.Player

val mapCommand = brigadierCommand("map") {
    literal("create") {
        argument("name", StringArgumentType.string()) {
            executes<Player> { sender, context ->
                val name = StringArgumentType.getString(context, "name")
                Bukkit.getScheduler().runTask(PLUGIN) { _ ->
                    val world = WorldCreator.ofNameAndKey(name, name.asId())
                        .environment(World.Environment.CUSTOM)
                        .type(WorldType.FLAT)
                        .generateStructures(false)
                        .generatorSettings("{\"lakes\": false, \"features\": true, \"biome\": \"minecraft:the_void\", \"structure_overrides\": [], \"layers\": [{\"height\": 1, \"block\": \"minecraft:air\"}]}")
                        .biomeProvider(null as String?)
                        .createWorld()

                    val text = GsonComponentSerializer.gson().deserialize(name)
                    registerMap(Map(world!!, name, text))
                    sender.respond("Map {} created.", text)
                    sender.teleportAsync(world.spawnLocation)
                }
                return@executes 1
            }
        }
    }

    literal("center") {
        executes<Player> { sender, _ ->
            val map = sender.world.map
            if (map != null) {
                map.center = sender.location
                sender.respond("Center set to {}, {}, {}.",
                    text(map.center.x, NamedTextColor.AQUA),
                     text(map.center.y, NamedTextColor.AQUA),
                    text(map.center.z, NamedTextColor.AQUA))
            } else {
                sender.respond("You are not in a map.")
            }
            return@executes 1
        }
    }

    literal("spawn") {
        literal("add") {
            executes<Player> { sender, _ ->
                val map = sender.world.map
                if (map != null) {
                    map.spawns.add(Spawn(sender.location))
                    sender.respond("Spawn set to {}, {}, {}.",
                        text(sender.location.x, NamedTextColor.AQUA),
                        text(sender.location.y, NamedTextColor.AQUA),
                        text(sender.location.z, NamedTextColor.AQUA))
                } else {
                    sender.respond("You are not in a map.")
                }
                return@executes 1
            }
        }

        literal("remove") {
            executes<Player> { sender, _ ->
                val map = sender.world.map
                if (map != null) {
                    val spawn = map.spawns.find { it.location == sender.location }
                    if (spawn != null) {
                        map.spawns.remove(spawn)
                        sender.respond("Spawn removed.")
                    } else {
                        sender.respond("There is no spawn at your location.")
                    }
                } else {
                    sender.respond("You are not in a map.")
                }
                return@executes 1
            }
        }
    }
}