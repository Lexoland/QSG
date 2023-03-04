package dev.lexoland.cmd

import dev.lexoland.listener.BuildListener
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object BuildCommand : CommandExecutor{

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if(sender !is Player) {
            sender.sendMessage("no.")
            return true
        }

        if(BuildListener.allowed.contains(sender)) {
            BuildListener.allowed.remove(sender)
            sender.sendMessage("Building disabled.")
        } else {
            BuildListener.allowed.add(sender)
            sender.sendMessage("Building enabled.")
        }
        return true
    }
}