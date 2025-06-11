package com.volatila.phymodels.util

import com.volatila.phymodels.model.Model.Vector3i
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import kotlin.math.max
import kotlin.math.min

class LocationUtil {
    companion object{
        fun getRelBlockList(
            blockList: HashMap<Location, Block>,
            center: Vector3i
        ): MutableMap<Vector3i, Material> {
            val blockListVector3i = mutableMapOf<Vector3i, Material>()
            for ((loc, block) in blockList) {
                val material = block.type
                if (material.isAir) continue
                val vector3i = Vector3i(loc.x.toInt(), loc.y.toInt(), loc.z.toInt())
                vector3i.sub(center.x, center.y, center.z)
                blockListVector3i[vector3i] = material
            }
            return blockListVector3i
        }

        fun getVector3iSize(v3i: Set<Vector3i>): Vector3i {
            var minX = Int.MAX_VALUE; var minY = Int.MAX_VALUE; var minZ = Int.MAX_VALUE
            var maxX = Int.MIN_VALUE; var maxY = Int.MIN_VALUE; var maxZ = Int.MIN_VALUE
            for (pos in v3i) {
                if (pos.x < minX) minX = pos.x; if (pos.y < minY) minY = pos.y; if (pos.z < minZ) minZ = pos.z
                if (pos.x > maxX) maxX = pos.x; if (pos.y > maxY) maxY = pos.y; if (pos.z > maxZ) maxZ = pos.z
            }
            val sizeX = maxX - minX + 1
            val sizeY = maxY - minY + 1
            val sizeZ = maxZ - minZ + 1
            return Vector3i(sizeX, sizeY, sizeZ)
        }

        fun getBlocksInArea(loc1: Location, loc2: Location): HashMap<Location, Block> {
            val xMin = min(loc1.blockX, loc2.blockX)
            val yMin = min(loc1.blockY, loc2.blockY)
            val zMin = min(loc1.blockZ, loc2.blockZ)

            val xMax = max(loc1.blockX, loc2.blockX)
            val yMax = max(loc1.blockY, loc2.blockY)
            val zMax = max(loc1.blockZ, loc2.blockZ)

            val blockList = hashMapOf<Location, Block>()

            for (i in xMin..xMax) for(j in yMin..yMax) for(k in zMin..zMax){
                val currLoc = Location(loc1.world, i.toDouble(), j.toDouble(), k.toDouble())
                blockList[currLoc] = currLoc.block
            }

            return blockList
        }
    }
}