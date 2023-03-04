package dev.lexoland.cmd

import dev.lexoland.QSG
import dev.lexoland.listener.BuildListener
import dev.lexoland.utils.brigadierCommand
import dev.lexoland.utils.executes
import dev.lexoland.utils.respond
import dev.lexoland.utils.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player

val spawnCommand = brigadierCommand(
    "spawn",
    aliases = arrayOf("s")
) {
    executes<Player> { sender, _ ->
        if (BuildListener.allowed.contains(sender)) {
            BuildListener.allowed.remove(sender)
            sender.respond("Building {}.", text("disabled", NamedTextColor.RED, TextDecoration.ITALIC))
            return@executes 1
        }
        BuildListener.allowed.add(sender)
        sender.respond("Building {}.", text("enabled", NamedTextColor.GREEN, TextDecoration.ITALIC))
        return@executes 1
    }
}

val centerCommand = brigadierCommand(
    "center",
    aliases = arrayOf("c")
) {
    executes<Player> { sender, _ ->
        val map = QSG.getMap(sender.world)
        if(map == null) {
            sender.respond("No map found for world \"{}\".", text(sender.world.name, NamedTextColor.RED, TextDecoration.ITALIC))
            return@executes 1
        }
        map.center = sender.location
        sender.respond("Set map center of \"{}\" to {}, {}, {}.",
            map.name,
            text(sender.location.x, NamedTextColor.GREEN),
            text(sender.location.y, NamedTextColor.GREEN),
            text(sender.location.z, NamedTextColor.GREEN)
        )
        return@executes 1
    }
}