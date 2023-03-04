package dev.lexoland.utils

import org.bukkit.Location
import org.bukkit.util.Vector

operator fun Vector.plus(other: Vector) = Vector(x + other.x, y + other.y, z + other.z)
operator fun Vector.minus(other: Vector) = Vector(x - other.x, y - other.y, z - other.z)
operator fun Vector.times(other: Vector) = Vector(x * other.x, y * other.y, z * other.z)
operator fun Vector.div(other: Vector) = Vector(x / other.x, y / other.y, z / other.z)
operator fun Vector.unaryMinus() = Vector(-x, -y, -z)
operator fun Vector.times(scalar: Double) = Vector(x * scalar, y * scalar, z * scalar)
operator fun Vector.div(scalar: Double) = Vector(x / scalar, y / scalar, z / scalar)

infix fun Vector.blockPosEqual(other: Vector) = blockX == other.blockX && blockY == other.blockY && blockZ == other.blockZ
infix fun Vector.blockPosEqual(other: Location) = blockX == other.blockX && blockY == other.blockY && blockZ == other.blockZ