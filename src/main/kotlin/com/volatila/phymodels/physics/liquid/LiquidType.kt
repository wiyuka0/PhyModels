package com.volatila.phymodels.physics.liquid

import org.bukkit.Color

enum class LiquidType(val config: LiquidConfig) {
    LAVA(LiquidConfig(
        0.8f,
        0.5f,
        4f,
        Color.ORANGE,
    )),
    WATER(LiquidConfig(
        0.2f,
        0.15f,
        1f,
        Color.AQUA
    )),
    SLIME(LiquidConfig(
        0.9f,
        0.9f,
        1.4f,
        Color.GREEN
    ));
    companion object{
    }
}