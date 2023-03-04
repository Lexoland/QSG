package dev.lexoland.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.CommandSender

val PREFIX = text("[", NamedTextColor.AQUA) +
        text("QSG", NamedTextColor.AQUA, TextDecoration.BOLD) +
        text("] ", NamedTextColor.AQUA)

fun text(content: String, color: TextColor? = null, vararg deco: TextDecoration = emptyArray()) = Component.text(content, Style.style { b ->
    color?.let { b.color(it) }
    if (deco.isNotEmpty())
        b.decorate(*deco)
})
fun text(value: Any, color: TextColor? = null, vararg deco: TextDecoration = emptyArray()) = text(value.toString(), color, *deco)

fun CommandSender.respond(
    message: String,
    vararg args: Component,
    color: TextColor = NamedTextColor.YELLOW,
    prefix: Component = PREFIX
) = sendMessage(message.format(*args, color = color, prefix = prefix))

fun String.format(
    vararg args: Component,
    color: TextColor? = null,
    prefix: Component? = null
): Component {
    val text = text(this, color)

    var i = 0
    val replacedText = text.replaceText(TextReplacementConfig.builder()
        .match("\\{(\\d*)}")
        .replacement { t, _ ->
            val target = t.group(1)
            if (target.isEmpty())
                return@replacement args[i++]
            return@replacement args[target.toInt()]
        }
        .build())

    return prefix?.append(replacedText) ?: replacedText
}

operator fun Component.plus(other: Component) = append(other)