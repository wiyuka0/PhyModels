package com.wiyuka.phymodels.tools

import com.wiyuka.phymodels.PhysicsTickManager
import com.wiyuka.phymodels.PhysAPI.Companion.BodyType
import com.wiyuka.phymodels.model.Model
import org.joml.Vector3i
import com.wiyuka.phymodels.model.ModelManager
import com.wiyuka.phymodels.physics.Physics
import com.wiyuka.phymodels.physics.obj.toLocation
import com.wiyuka.phymodels.util.LocationUtil
import com.wiyuka.phymodels.util.toVector3f
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import java.util.UUID

class AreaSelect: Listener {
    companion object{
        private data class PlayerState(
            val point1: Vector3i? = null,
            val point2: Vector3i? = null,
        )

        private val playerStates = mutableMapOf<Player, PlayerState>()
    }
    @EventHandler
    fun onPlayerBreakBlock(e: BlockBreakEvent) {
        if(e.player.inventory.itemInMainHand.type != Material.STICK) return
        if(!e.player.hasPermission("phymodels.model")) return
        e.isCancelled = true
        val playerCurrentState = playerStates.computeIfAbsent(e.player) { PlayerState() }
        val point = e.block.location
        val newState = if (playerCurrentState.point1 == null) {
            e.player.sendMessage("Point 1 set to ${point.blockX}, ${point.blockY}, ${point.blockZ}")
            playerCurrentState.copy(point1 = Vector3i(point.blockX, point.blockY, point.blockZ))
        } else if (playerCurrentState.point2 == null) {
            e.player.sendMessage("Point 2 set to ${point.blockX}, ${point.blockY}, ${point.blockZ}")
            val modelName = UUID.randomUUID().toString()
            val modelCenter = Vector3i(e.player.x.toInt(), e.player.y.toInt(), e.player.z.toInt())
            val modelInfo = ModelManager.makeModel(
                playerCurrentState.point1.toLocation(e.block.world),
                point,
                modelCenter,
                1f,
                modelName,
                modelCenter.toVector3f()
            )
            val model = modelInfo.first

            LocationUtil.getBlocksInArea(playerCurrentState.point1.toLocation(e.block.world), point).map {
                it.value.type = Material.AIR
                Physics.removeStaticBlock(block = it.value)
            }
//            val fixedCenter: Location = Location(e.player.world,
//                (playerCurrentState.point1.x + point.x) / 2 + 0.5,
//                (playerCurrentState.point1.y + point.y) / 2 + 0.5,
//                (playerCurrentState.point1.z + point.z) / 2 + 0.5
//            )
            val fixedCenter = modelInfo.second.toLocation(e.player.world)

            val modelGeometricCenter = model.getGeometricCenter(fixedCenter, 1f)

            ModelManager.generateModel(
                Model.getModelByName(modelName)!!,
                modelGeometricCenter,
                1f,
                BodyType.PHYSICAL,
                e.player.world.name
            )

            e.player.sendMessage("Model ${model.name} generated.")

            playerCurrentState.copy(point2 = Vector3i(point.blockX, point.blockY, point.blockZ))
        } else {
            playerStates.remove(e.player)
            return
        }
        playerStates[e.player] = newState
    }
}

fun Vector3i.toLocation(world: World): Location {
    return Location(world, x.toDouble(), y.toDouble(), z.toDouble())
}
