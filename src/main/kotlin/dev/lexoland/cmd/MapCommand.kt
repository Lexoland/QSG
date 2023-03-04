package dev.lexoland.cmd

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.SuggestionProvider
import de.leximon.api.command.Arguments
import de.leximon.api.command.Suggestions
import dev.lexoland.PLUGIN
import dev.lexoland.asId
import dev.lexoland.core.Map
import dev.lexoland.core.map
import dev.lexoland.core.maps
import dev.lexoland.core.registerMap
import dev.lexoland.utils.*
import org.bukkit.Bukkit
import org.bukkit.WorldCreator
import org.bukkit.entity.Player

val EXCEPTION_NOT_ON_MAP = SimpleCommandExceptionType { "You are not on a map" }
val EXCEPTION_INVALID_MAP = SimpleCommandExceptionType { "Invalid map" }
val EXCEPTION_NO_SPAWN = SimpleCommandExceptionType { "There is no spawn at your location" }
val SUGGESTIONS_MAPS: SuggestionProvider<*> = Suggestions.list({ maps.values }, { it.name }).build()

val mapCommand = brigadierCommand("map") {
    literal("create") {
        argument("name", StringArgumentType.word()) {
            argument("displayName", Arguments.Component.component()) {
                executes<Player> { sender, context ->
                    val name = StringArgumentType.getString(context, "name")
                    val displayName = Arguments.Component.getComponent(context, "displayName")
                    Bukkit.getScheduler().runTask(PLUGIN) { _ ->
                        val world = WorldCreator.ofNameAndKey(name, name.asId())
                            .presetVoid()
                            .createWorld()!!

                        registerMap(Map(name, displayName))
                        sender.respond("Map {} created.", displayName)
                        sender.teleportAsync(world.spawnLocation)
                    }
                    return@executes 1
                }
            }
        }
    }

    literal("tp") {
        argument("map", StringArgumentType.word()) {
            suggests(SUGGESTIONS_MAPS)
            executes<Player> { sender, context ->
                val name = StringArgumentType.getString(context, "map")
                val map = maps[name] ?: throw EXCEPTION_INVALID_MAP.create()
                val world = Bukkit.createWorld(WorldCreator.ofNameAndKey(name, name.asId()))!!

                sender.teleportAsync(map.center.toLocation(world))
                return@executes 1
            }
        }
    }

    literal("center") {
        executes<Player> { sender, _ ->
            val map = sender.world.map ?: throw EXCEPTION_NOT_ON_MAP.create()
            map.center = sender.location.toVector()
            sender.respond("Center set to {}.", textLocation(sender.location))
            return@executes 1
        }
    }

    literal("spawn") {
        literal("add") {
            executes<Player> { sender, _ ->
                val map = sender.world.map ?: throw EXCEPTION_NOT_ON_MAP.create()
                map.spawns.add(sender.location.toVector())
                sender.respond("Spawn set to {}.", textLocation(sender.location))
                return@executes 1
            }
        }

        literal("remove") {
            executes<Player> { sender, _ ->
                val map = sender.world.map ?: throw EXCEPTION_NOT_ON_MAP.create()
                val spawn = map.spawns.firstOrNull { it blockPosEqual sender.location }
                    ?: throw EXCEPTION_NO_SPAWN.create()

                map.spawns.remove(spawn)
                sender.respond("Spawn removed.")
                return@executes 1
            }
        }
    }
}