package com.wiyuka.phymodels.physics.liquid

import org.bukkit.Color

enum class LiquidType(val config: LiquidConfig) {
    LAVA(LiquidConfig(
        sticky = 0.8f,
        surfaceTension = 0.5f,
        mass = 4f,
        color = Color.ORANGE,
    )),
    WATER(LiquidConfig(
        sticky = 0.2f,
        surfaceTension = 0.15f,
        mass = 1f,
        color = Color.AQUA
    )),
    SLIME(LiquidConfig(
        sticky = 0.9f,
        surfaceTension = 0.9f,
        mass = 1.4f,
        color = Color.GREEN
    )),
    SMOKE(LiquidConfig(
        sticky = 0.3f,
        surfaceTension = 0.15f,
        mass = 0.25f,
        color = Color.GRAY,
    ));
    companion object{
    }
}