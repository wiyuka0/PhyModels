package com.volatila.phymodels.bukkitlistener

import com.volatila.phymodels.physics.obj.toJmeVector3f
import com.volatila.phymodels.physics.obj.toVector3f
import com.volatila.phymodels.util.PhysicsUtil
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.block.BlockPistonEvent
import org.bukkit.event.block.BlockPlaceEvent

class BlockUpdateListener: Listener {
    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent){
        updateNearbyRigidBodies(event.block.location)
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent){
        updateNearbyRigidBodies(event.block.location)
    }

//    @EventHandler
//    fun onPistonActivate(event: BlockPistonEvent){
//        updateNearbyRigidBodies(event.block.location)
//    }
//    companion object {
//        @JvmStatic
//        fun getHandlerList() {
//
//        }
//    }
    
    fun updateNearbyRigidBodies(location: Location){
        for ((rigidBody, _) in PhysicsUtil.getNearbyRigidBodies(location.toVector3f().toJmeVector3f(), 2f)) rigidBody.activate(true)
    }
}