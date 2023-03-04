package dev.lexoland.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World

val module = SerializersModule {
    contextual(Location::class, LocationSerializer())
    contextual(Component::class, ComponentSerializer())
    contextual(World::class, WorldSerializer())
}
val jsonFormat = Json { serializersModule = module }

class LocationSerializer : KSerializer<Location> {
    override val descriptor = buildClassSerialDescriptor("Location") {
        element<String>("world")
        element<Double>("x")
        element<Double>("y")
        element<Double>("z")
        element<Float>("yaw")
        element<Float>("pitch")
    }

    override fun serialize(encoder: Encoder, value: Location) = encoder.encodeStructure(descriptor) {
        encodeStringElement(descriptor, 0, value.world.name)
        encodeDoubleElement(descriptor, 1, value.x)
        encodeDoubleElement(descriptor, 2, value.y)
        encodeDoubleElement(descriptor, 3, value.z)
        encodeFloatElement(descriptor, 4, value.yaw)
        encodeFloatElement(descriptor, 5, value.pitch)
    }

    override fun deserialize(decoder: Decoder): Location = decoder.decodeStructure(descriptor) {
        var world = "world"
        var x = 0.0
        var y = 0.0
        var z = 0.0
        var yaw = 0.0f
        var pitch = 0.0f
        while (true) {
            when (val i = decodeElementIndex(descriptor)) {
                0 -> world = decodeStringElement(descriptor, 0)
                1 -> x = decodeDoubleElement(descriptor, 1)
                2 -> y = decodeDoubleElement(descriptor, 2)
                3 -> z = decodeDoubleElement(descriptor, 3)
                4 -> yaw = decodeFloatElement(descriptor, 4)
                5 -> pitch = decodeFloatElement(descriptor, 5)
                CompositeDecoder.DECODE_DONE -> break
                else -> error("Unexpected index: $i")
            }
        }
        Location(Bukkit.getWorld(world), x, y, z, yaw, pitch)
    }
}

class ComponentSerializer : KSerializer<Component> {
    override val descriptor = PrimitiveSerialDescriptor("Component", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: Component
    ) = encoder.encodeString(GsonComponentSerializer.gson().serialize(value))

    override fun deserialize(
        decoder: Decoder
    ): Component = GsonComponentSerializer.gson().deserialize(decoder.decodeString())
}

class WorldSerializer : KSerializer<World> {
    override val descriptor = PrimitiveSerialDescriptor("World", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: World
    ) = encoder.encodeString(value.name)

    override fun deserialize(
        decoder: Decoder
    ): World = Bukkit.getWorld(decoder.decodeString())!!
}