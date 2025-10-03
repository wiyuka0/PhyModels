package com.wiyuka.phymodels.listener

import com.wiyuka.phymodels.PhysicsTickManager
import com.wiyuka.phymodels.PhysicsTickManager.Companion.updateNearbyBlocks
import com.wiyuka.phymodels.physics.cloth.ClothManager.xList
import com.wiyuka.phymodels.physics.cloth.ClothManager.yList
import com.wiyuka.phymodels.physics.cloth.ClothManager.zList
import com.wiyuka.phymodels.physics.objmanager.ObjectManager
import com.wiyuka.phymodels.physics.objmanager.TaskProcessor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.BlockDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.joml.Vector3i

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
    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        updateNearbyBlocks(player.location, Vector3i(2, 2, 2))
    }
}