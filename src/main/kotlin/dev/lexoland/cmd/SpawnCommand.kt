package dev.lexoland.cmd

import dev.lexoland.utils.brigadierCommand

val spawnCommand = brigadierCommand("spawn") {
    executes { sender, _ ->
        sender.sendMessage("Hello world!")
        return@executes 1
    }
}