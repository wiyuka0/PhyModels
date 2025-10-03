package com.wiyuka.phymodels.physics.materialattributes

import org.bukkit.Material

class MaterialFriction {
    companion object {
        const val DEFAULT_FRICTION = 0.5f
        fun friction(material: Material): Float {
            return when (material) {
                Material.GLASS -> 0.5f
                Material.ICE -> 0.3f
                Material.PACKED_ICE -> 0.08f
                Material.BLUE_ICE -> 0.01f
                Material.SLIME_BLOCK -> 3.2f
                Material.HONEY_BLOCK -> 4.8f
                Material.SAND -> 0.6f
                else -> DEFAULT_FRICTION // default friction value
            }
        }

    }
}