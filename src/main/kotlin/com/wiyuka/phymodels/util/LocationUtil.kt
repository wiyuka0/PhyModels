package com.wiyuka.phymodels.util

//import org.joml.Vector3i
import com.jme3.math.Vector3f
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.joml.Vector3i
import kotlin.math.max
import kotlin.math.min

class LocationUtil {
    companion object{
        fun getRelBlockList(
            blockList: HashMap<Location, Block>,
            center: Vector3i
        ): MutableMap<Vector3i, BlockData> {
            val blockListVector3i = mutableMapOf<Vector3i, BlockData>()
            for ((loc, block) in blockList) {
                val material = block.type
                if (material.isAir) continue
                val vector3i = Vector3i(loc.x.toInt(), loc.y.toInt(), loc.z.toInt())
                vector3i.sub(center.x, center.y, center.z)
                blockListVector3i[vector3i] = block.blockData
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

fun Vector3i.toToken(): String {
    return this.x.toString() + ":" + this.y.toString() + ":" + this.z.toString()
}
fun Vector3i.toVector3f(): javax.vecmath.Vector3f {
    return javax.vecmath.Vector3f(this.x.toFloat(), this.y.toFloat(), this.z.toFloat())
}

fun fromTokenV3i(token: String): Vector3i {
    val tokenSplit = token.split(":")
    return Vector3i(tokenSplit[0].toInt(), tokenSplit[1].toInt(), tokenSplit[2].toInt())
}