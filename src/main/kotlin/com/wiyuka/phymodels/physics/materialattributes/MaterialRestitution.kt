package com.wiyuka.phymodels.physics.materialattributes

import org.bukkit.Material

class MaterialRestitution {
    companion object {
        const val DEFAULT = 0.1f

        fun restitution(material: Material): Float {
            return when (material) {
                Material.STONE -> 0.01f
                Material.COBBLESTONE -> 0.01f
                Material.DIRT -> 0.02f
                Material.GRASS_BLOCK -> 0.02f
                Material.SLIME_BLOCK -> 0.9f
                Material.HONEY_BLOCK -> 0.01f
                else -> DEFAULT
            }
        }

    }


}