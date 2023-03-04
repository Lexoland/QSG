package dev.lexoland.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.CommandSender

val PREFIX = text("[", rgb(0x43a180)) +
        text("QSG", rgb(0x00ffa6), TextDecoration.BOLD) +
        text("] ", rgb(0x43a180))
fun text(content: String, color: TextColor? = null, vararg deco: TextDecoration = emptyArray()) = Component.text(content, Style.style { b ->
    color?.let { b.color(it) }
    if (deco.isNotEmpty())
        b.decorate(*deco)
})
fun text(value: Any, color: TextColor? = null, vararg deco: TextDecoration = emptyArray()) = text(value.toString(), color, *deco)


fun gradient(
    content: String,
    c1: TextColor, c2: TextColor,
    vararg deco: TextDecoration = emptyArray()
) = Component.text().apply {
    for (i in content.indices)
        it.append(text(content[i], TextColor.lerp(i.toFloat() / content.length.toFloat(), c1, c2), *deco))
}.build()

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

operator fun Component.plus(other: String) = append(text(other))


fun rgb(r: Int, g: Int, b: Int) = TextColor.color(r, g, b)
fun rgb(color: Int) = TextColor.color(color)