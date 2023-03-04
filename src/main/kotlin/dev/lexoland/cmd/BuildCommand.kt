package dev.lexoland.cmd

import de.leximon.api.command.CommandUser
import dev.lexoland.listener.BuildListener
import dev.lexoland.utils.*
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player

val buildCommand = brigadierCommand(
    "build",
    permissions = arrayOf("qsg.command.build"),
    users = arrayOf(CommandUser.PLAYER),
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