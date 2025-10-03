package com.wiyuka.phymodels.physics.liquid

import org.bukkit.Color

data class LiquidConfig(
    val sticky: Float,// -> friction and linear damping/angular damping
    val surfaceTension: Float, // -> 互相吸引力
    val mass: Float,
    val color: Color
){
    companion object{
        // Global params
        val resolution: Float = 10.4f // radius to liquid sphere pixel
    }
}