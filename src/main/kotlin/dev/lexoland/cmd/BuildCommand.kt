package dev.lexoland.cmd

import dev.lexoland.listener.BuildListener
import dev.lexoland.utils.brigadierCommand
import dev.lexoland.utils.executes
import dev.lexoland.utils.respond
import dev.lexoland.utils.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player

val buildCommand = brigadierCommand(
    "build",
    aliases = arrayOf("b")
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