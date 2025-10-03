package com.wiyuka.phymodels.physics.materialattributes

import org.bukkit.Material

fun Material.getFriction(): Float {
    return MaterialFriction.friction(this)
}
fun Material.getRestitution(): Float {
    return MaterialRestitution.restitution(this)
}
fun Material.getMass(): Float {
    return MaterialMass.density(this)
}