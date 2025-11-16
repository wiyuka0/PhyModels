package com.wiyuka.phymodels.listener

import com.wiyuka.phymodels.PhysicsTickManager
import com.wiyuka.phymodels.PhysicsTickManager.Companion.updateNearbyBlocks
import com.wiyuka.phymodels.physics.cloth.ClothManager.xList
import com.wiyuka.phymodels.physics.cloth.ClothManager.yList
import com.wiyuka.phymodels.physics.cloth.ClothManager.zList
import com.wiyuka.phymodels.physics.obj.toJomlQuaternionf
import com.wiyuka.phymodels.physics.obj.toJomlVector3f
import com.wiyuka.phymodels.physics.obj.toVecmathQuat4f
import com.wiyuka.phymodels.physics.objmanager.ObjectManager
import com.wiyuka.phymodels.physics.objmanager.TaskProcessor
import com.wiyuka.phymodels.virtualworld.VirtualWorld
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.AnaloguePowerable
import org.bukkit.block.data.Openable
import org.bukkit.block.data.Powerable
import org.bukkit.block.data.type.Switch
import org.bukkit.entity.BlockDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInputEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.joml.Quaternionf
import org.joml.Vector3i
import java.util.Vector

class Listeners: Listener {
    @EventHandler
    fun onPlayerSneak(event: PlayerToggleSneakEvent) {
        val player = event.player
        if(player.isSneaking) {
            val targetEntity = player.getTargetEntity(4)
            if(targetEntity !is BlockDisplay) return
            val model = ObjectManager.livingModels.values.find { modelEntity ->
                modelEntity.displayBlocks.values.find { modelBlockDisplay ->
                    targetEntity.uniqueId == modelBlockDisplay.entity.uniqueId
                } != null
            }
            if(model == null) return

            model.fetchingPlayer = player
        }
    }
//
//    @EventHandler
//    fun onBlockBreak(event: BlockBreakEvent) {
//
//        val block = event.block
//        if(block.isPassable) return
//        PhysicsTickManager.Block(block)
////        Physics.chunkManager.removeBlockAsync(Vector3i(block.location.blockX, block.location.blockY, block.location.blockZ))
//    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        val player = event.player
        val playerPointer = "PlayerBox${player.uniqueId}"
        val playerRigidBody = PhysicsTickManager.playerBoxes[playerPointer]
        if(playerRigidBody == null) return

        ObjectManager.removeObjectFromPhysicsWorld(playerRigidBody, player.world.name)
    }

    val playerFlags = mutableMapOf<String, Boolean>()
    val PLAYER_INTERACT_DISTANCE_SQUARED = 36.0
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerInteract(event: PlayerDropItemEvent) {
//        if (!event.action.isRightClick) return
//        event.player.sendMessage("Interacted")

        val player = event.player
        val playerUniqueId = player.uniqueId.toString()
        if(playerFlags.computeIfAbsent(playerUniqueId.toString()) { true }) {
            playerFlags[playerUniqueId] = false
        }else {
            playerFlags[playerUniqueId] = true
            return
        }

        val playerLocation = event.player.eyeLocation
        val nearbyModels = ObjectManager.livingModels.filter {
            if(it.value.location.world.name == playerLocation.world.name)
                return@filter it.value.location.distanceSquared(playerLocation) <= PLAYER_INTERACT_DISTANCE_SQUARED
            return@filter false
        }
        if(nearbyModels.isEmpty()) return
        nearbyModels.forEach { uuid, model ->
                val modelCenter = model.rigidBody.getPhysicsLocation(null)
            val modelRotation = model.rigidBody.getPhysicsRotation(null).toJomlQuaternionf()
            val relativePoint = relativePosOnPoint(Location(VirtualWorld.virtualWorld, modelCenter.x.toDouble(), modelCenter.y.toDouble(), modelCenter.z.toDouble()).apply { direction = playerLocation.direction }, modelRotation, playerLocation)
            val mirrorStructureXList = mutableListOf<Float>()
            val mirrorStructureYList = mutableListOf<Float>()
            val mirrorStructureZList = mutableListOf<Float>()
            model.displayBlocks.values.forEach {
                mirrorStructureXList += it.virtualWorldBlock.x.toFloat()
                mirrorStructureYList += it.virtualWorldBlock.y.toFloat()
                mirrorStructureZList += it.virtualWorldBlock.z.toFloat()
            }

            val maxX = mirrorStructureXList.max()
            val maxY = mirrorStructureYList.max()
            val maxZ = mirrorStructureZList.max()
            val minX = mirrorStructureXList.min()
            val minY = mirrorStructureYList.min()
            val minZ = mirrorStructureZList.min()

            val centerX = ((maxX + minX) * 0.5f) + 0.5f
            val centerY = ((maxY + minY) * 0.5f) + 0.5f
            val centerZ = ((maxZ + minZ) * 0.5f) + 0.5f

            val structureCenterPos = Location(VirtualWorld.virtualWorld, centerX.toDouble(), centerY.toDouble(), centerZ.toDouble())
            val rayTraceStart = structureCenterPos.clone().add(relativePoint).subtract(0.5, 0.5, 0.5)
            val rayTraceDirection = relativePoint.direction

//            event.player.teleportAsync(rayTraceStart.apply { direction = rayTraceDirection })

            val result = VirtualWorld.virtualWorld.rayTraceBlocks(rayTraceStart, org.bukkit.util.Vector(rayTraceDirection.x, rayTraceDirection.y, rayTraceDirection.z), 10.0)
            if(result == null) return@forEach
            if(result.hitBlock == null) return@forEach

            result.hitBlock!!.interactBlock()
        }
    }

    fun Block.interactBlock() {
        val data = this.blockData

        when (data) {
            // 门、活板门、栅栏门（能开关的）
            is Openable -> {
                data.isOpen = !data.isOpen
                this.blockData = data
                this.state.update(true, true)
            }

            // 拉杆、按钮（Switch covers both）
            is Switch -> {
                data.isPowered = !data.isPowered
                this.blockData = data
                // 红石更新
                this.state.update(true, true)
            }

            // 普通 Powerable（某些红石方块）
            is Powerable -> {
                data.isPowered = !data.isPowered
                this.blockData = data
                this.state.update(true, true)
            }

            // 比较器、中继器这种有强度的
            is AnaloguePowerable -> {
                val next = (data.power + 1) % (data.maximumPower + 1)
                data.power = next
                this.blockData = data
            }

            else -> {

                when (this.type) {
                    Material.DISPENSER, Material.DROPPER -> {
                        val dispenser = this.state as org.bukkit.block.Dispenser
                        dispenser.dispense()
                    }

                    else -> {
                    }
                }
            }
        }
    }

    private fun relativePosOnPoint(originOnWorld: Location, rotation: Quaternionf, targetLocation: Location): Location {
        val world = originOnWorld.world
        val direction = originOnWorld.direction
        val invRot = Quaternionf(rotation).invert()

        val relativePoint = targetLocation.clone().apply{ setWorld(originOnWorld.world) }.subtract(originOnWorld).toJomlVector3f()
        invRot.transform(relativePoint)
        val rotatedDirection = invRot.transform(direction.toVector3f())
        return Location(
            world,
            relativePoint.x.toDouble() + 0.5,
            relativePoint.y.toDouble() + 0.5,
            relativePoint.z.toDouble() + 0.5
        ).apply {
            this.direction = (
                org.bukkit.util.Vector(rotatedDirection.x, rotatedDirection.y, rotatedDirection.z)
            )
        }
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        updateNearbyBlocks(player.location, Vector3i(2, 2, 2))
    }
}