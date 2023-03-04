package dev.lexoland.utils

import com.mojang.brigadier.Message
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import de.leximon.api.command.BrigadierCommand
import de.leximon.api.command.CommandAPI
import de.leximon.api.command.CommandUser
import de.leximon.api.command.Commands
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

fun brigadierCommand(
    name: String,
    permissions: Array<String> = emptyArray(),
    users: Array<CommandUser> = emptyArray(),
    aliases: Array<String> = emptyArray(),
    builder: Commands.() -> Unit
): BrigadierCommand {

    return object : BrigadierCommand() {

        init {
            this.names = aliases + name
            this.permissions = permissions
            this.user = users
        }

        override fun command(r: Commands) {
            builder(r)
        }
    }
}

fun Commands.literal(name: String, builder: Commands.() -> Unit) {
    then(Commands.literal(name).apply(builder))
}

fun Commands.argument(name: String, type: ArgumentType<*>, builder: Commands.() -> Unit) {
    then(Commands.argument(name, type).apply(builder))
}

val EXCEPTION_INVALID_SENDER = DynamicCommandExceptionType { Message { "Command sender must be a $it" } }

inline fun <reified S : CommandSender> Commands.executes(crossinline executor: (S, CommandContext<*>) -> Int) {
    executes { sender, context ->
        if (sender !is S)
            throw EXCEPTION_INVALID_SENDER.create(S::class.simpleName)
        executor(sender, context)
    }
}

fun JavaPlugin.commands(vararg commands: BrigadierCommand) = CommandAPI(this).apply {
    for (command in commands)
        register(command)
    finish()
}