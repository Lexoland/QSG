package dev.lexoland.cmd

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.SuggestionProvider
import de.leximon.api.command.Arguments
import de.leximon.api.command.Suggestions
import dev.lexoland.PLUGIN
import dev.lexoland.asKey
import dev.lexoland.core.*
import dev.lexoland.core.Map
import dev.lexoland.utils.*
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.WorldCreator
import org.bukkit.entity.Player
import java.util.*

val EXCEPTION_NOT_ON_MAP = SimpleCommandExceptionType { "You are not on a map" }
val EXCEPTION_INVALID_MAP = SimpleCommandExceptionType { "Invalid map" }
val EXCEPTION_NO_SPAWN = SimpleCommandExceptionType { "There is no spawn at your location" }
val SUGGESTIONS_MAPS: SuggestionProvider<*> = Suggestions.list({ maps.values }, { it.name }).build()

val mapCommand = brigadierCommand(
    "map",
    permissions = arrayOf("qsg.command.map")
) {
    literal("create") {
        argument("name", StringArgumentType.word()) {
            argument("displayName", Arguments.Component.component()) {
                executes<Player> { sender, context ->
                    val name = StringArgumentType.getString(context, "name").lowercase(Locale.ROOT)
                    val displayName = Arguments.Component.getComponent(context, "displayName")
                    Bukkit.getScheduler().runTask(PLUGIN) { _ ->
                        sender.respond("Generating world...")
                        val world = WorldCreator.ofKey(name.asKey())
                            .presetVoid()
                            .createWorld()!!

                        registerMap(Map(name, displayName))
                        sender.respond("Map {} has been created created.", displayName)
                        sender.teleport(world.spawnLocation)
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

                sender.respond("Teleporting to map {}...", map.displayName)
                val world = Bukkit.createWorld(WorldCreator.ofKey(name.asKey()))!!
                sender.teleport(map.center.toLocation(world).blockCentered())
                sender.respond("Teleported!")
                return@executes 1
            }
        }
    }

    literal("center") {
        executes<Player> { sender, _ ->
            val map = sender.world.map ?: throw EXCEPTION_NOT_ON_MAP.create()
            map.center = sender.location.toBlockVector()
            sender.world.worldBorder.center = sender.location
            sender.respond("Center set to {}.", textLocation(sender.location))
            return@executes 1
        }
    }

    literal("spawn") {
        literal("add") {
            executes<Player> { sender, _ ->
                val map = sender.world.map ?: throw EXCEPTION_NOT_ON_MAP.create()
                map.spawns.add(sender.location.toBlockVector())
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

    literal("enable") {
        argument("value", BoolArgumentType.bool()) {
            executes<Player> { sender, context ->
                val map = sender.world.map ?: throw EXCEPTION_NOT_ON_MAP.create()
                map.enabled = BoolArgumentType.getBool(context, "value")
                sender.respond(
                    "Map {} has been {}.", map.displayName,
                    if (map.enabled) text("enabled", NamedTextColor.GREEN, TextDecoration.ITALIC) else text("disabled", NamedTextColor.RED, TextDecoration.ITALIC)
                )
                return@executes 1
            }
        }
    }

    literal("saveAll") {
        executes { sender, _ ->
            saveMaps()
            sender.respond("All maps have been saved.")
            return@executes 1
        }
    }
}