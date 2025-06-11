package com.volatila.phymodels.physics.materialattributes

import org.bukkit.Material

class MaterialMass {
    companion object{
        fun density(material: Material): Float{
            return when(material){
                Material.AIR -> 0f
                Material.STONE -> 1f
                Material.GRASS_BLOCK -> .8f
                Material.DIRT -> .8f
                Material.COBBLESTONE -> 1f
                Material.DIAMOND_BLOCK -> 1f
                Material.GOLD_BLOCK -> 8f
                Material.IRON_BLOCK -> 4f
                Material.BEDROCK -> 100f
                Material.SAND -> 0.5f
                Material.SLIME_BLOCK -> 0.4f
                Material.SNOW_BLOCK -> 0.1f
                Material.WATER -> 0.9f
                Material.LAVA -> 2f
                Material.ICE -> 0.8f


                else -> complexMass(material) // Default mass for other materials
            }
        }
        fun complexMass(material: Material): Float{
            if(material.name.contains("_LOG") || material.name.contains("_WOOD")){
                return 0.5f
            }
            if(material.name.contains("_PLANKS")){
                return 0.4f
            }
            if(material.name.contains("_LEAVES")){
                return 0.1f
            }
            if(material.name.contains("_WOOL")){
                return 0.2f
            }
            if(material.name.contains("_GLASS")){
                return 0.3f
            }
            return 1f
        }
    }
}