package com.volatila.phymodels.tools

import com.volatila.phymodels.PhysicsTickManager
import com.volatila.phymodels.PhysAPI.Companion.BodyType
import com.volatila.phymodels.model.Model
import com.volatila.phymodels.model.Model.Vector3i
import com.volatila.phymodels.model.ModelManager
import com.volatila.phymodels.physics.Physics
import com.volatila.phymodels.physics.obj.toLocation
import com.volatila.phymodels.util.LocationUtil
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import java.util.UUID

class Active: Listener {

    @EventHandler
    fun onBlockBreak(e: BlockBreakEvent) {
        if(e.player.inventory.itemInMainHand.type != Material.FLINT) return
        val player = e.player
        if(!player.hasPermission("phymodels.model")) return
        e.isCancelled = true
        val block = e.block
        val blockLoc = block.location
        val blockList = bfs(blockLoc)

        val xMax = blockList.maxOf { it.x }
        val xMin = blockList.minOf { it.x }
        val yMax = blockList.maxOf { it.y }
        val yMin = blockList.minOf { it.y }
        val zMax = blockList.maxOf { it.z }
        val zMin = blockList.minOf { it.z }

        val centerX = ((xMax + xMin) / 2) + 1.0
        val centerY = ((yMax + yMin) / 2) + 1.0
        val centerZ = ((zMax + zMin) / 2) + 0.0

        val centerLoc = Location(blockLoc.world, centerX, centerY, centerZ)
        val minLoc = Location(blockLoc.world, xMin.toDouble(), yMin.toDouble(), zMin.toDouble())
        val maxLoc = Location(blockLoc.world, xMax.toDouble(), yMax.toDouble(), zMax.toDouble())

        val modelName = UUID.randomUUID().toString()
        val modelCenter = Vector3i(e.player.x.toInt(), e.player.y.toInt(), e.player.z.toInt())
        val modelInfo = ModelManager.makeModel(minLoc, maxLoc, modelCenter, 1f,
            modelName, modelCenter.toVector3f()
        )
        val model = modelInfo.first
        val fixedCenter = modelInfo.second

        LocationUtil.getBlocksInArea(minLoc, maxLoc).map {
            it.value.type = Material.AIR
            Physics.removeStaticBlock(block)
        }

//        val modelGeometricCenter = model.getGeometricCenter(centerLoc, 1f)

        ModelManager.generateModel(
            Model.getModelByName(modelName)!!,
            fixedCenter.toLocation(e.player.world),
            1f,
            BodyType.PHYSICAL
        )

        e.player.sendMessage("Model ${model.name} generated.")

    }
    fun bfs(startLocation: Location): Set<Block>{
        val hashSet = hashSetOf<Block>()
        findNeighbor(startLocation, hashSet)
        return hashSet
    }

    fun findNeighbor(blockLoc: Location, totalSet: HashSet<Block>, currentDeepLayer: Int = 0): Set<Block>{
        val block = blockLoc.block
        if(block.type == Material.AIR) return totalSet
        if(totalSet.contains(block)) return totalSet
        if(currentDeepLayer > 20) return totalSet
        totalSet.add(block)
        val x = blockLoc.x
        val y = blockLoc.y
        val z = blockLoc.z
        findNeighbor(Location(blockLoc.world, x + 1, y, z), totalSet, currentDeepLayer + 1)
        findNeighbor(Location(blockLoc.world, x - 1, y, z), totalSet, currentDeepLayer + 1)
        findNeighbor(Location(blockLoc.world, x, y + 1, z), totalSet, currentDeepLayer + 1)
        findNeighbor(Location(blockLoc.world, x, y - 1, z), totalSet, currentDeepLayer + 1)
        findNeighbor(Location(blockLoc.world, x, y, z + 1), totalSet, currentDeepLayer + 1)
        findNeighbor(Location(blockLoc.world, x, y, z - 1), totalSet, currentDeepLayer + 1)

        return totalSet
    }
}