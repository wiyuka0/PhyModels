package com.wiyuka.phymodels.tools

import com.jme3.bullet.joints.Point2PointJoint
import com.jme3.bullet.objects.PhysicsRigidBody
import javax.vecmath.Vector3f
import com.wiyuka.phymodels.physics.Physics
import com.wiyuka.phymodels.physics.obj.toJmeVector3f
import com.wiyuka.phymodels.physics.obj.toVecmath
import com.wiyuka.phymodels.physics.obj.toVector3f
import com.wiyuka.phymodels.physics.objmanager.TaskProcessor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class P2PCreator: Listener {

    companion object{
        private data class PlayerState(
            var point1: Vector3f?,
            var point2: Vector3f?,

            var rigidBody1: PhysicsRigidBody?,
            var rigidBody2: PhysicsRigidBody?
        )
        private val playerStateMap = HashMap<Player, PlayerState>()
    }
    @EventHandler
    fun onUse(e: PlayerInteractEvent){
//        if(e.player.inventory.itemInMainHand.type != Material.STRING) return
//        val player = e.player
//
//        val playerPosition = player.location
//        val playerDirection = playerPosition.direction
//
//        val rayTraceResultRel = Physics.LocationUtil.getBlocksInAreaPoint(playerPosition.toVector3f(), playerDirection.toVector3f().toVecmath())
//        if(rayTraceResultRel == null) return
//        val playerCurrentState = P2PCreator.playerStateMap.computeIfAbsent(e.player) { PlayerState(null, null, null, null) }
//
//        if(playerCurrentState.point1 == null) {
//            playerCurrentState.point1 = rayTraceResultRel.second
//            playerCurrentState.rigidBody1 = rayTraceResultRel.first
//
//            playerStateMap[player] = playerCurrentState
//
//            player.sendMessage("Set point1: ${playerCurrentState.point1}")
//        }else if(playerCurrentState.point2 == null){
//            playerCurrentState.point2 = rayTraceResultRel.second
//            playerCurrentState.rigidBody2 = rayTraceResultRel.first
//
//            playerStateMap[player] = playerCurrentState
//            if(playerCurrentState.rigidBody1 == playerCurrentState.rigidBody2) {
//                player.sendMessage("The two points are on the same object, please select again")
//                playerCurrentState.point1 = null
//                playerCurrentState.point2 = null
//                return
//            }
//            if(playerCurrentState.rigidBody1?.mass == 0f || playerCurrentState.rigidBody2?.mass == 0f) {
//                player.sendMessage("There is at least one point on a static object, please select again")
//                playerCurrentState.point1 = null
//                playerCurrentState.point2 = null
//                return
//            }
//
//            val joint = Point2PointJoint(playerCurrentState.rigidBody1, playerCurrentState.rigidBody2, playerCurrentState.point1?.toJmeVector3f(), playerCurrentState.point2?.toJmeVector3f())
//
//            player.sendMessage("Finish add joint")
//            TaskProcessor.addObject(joint)
//        }else {
//            playerStateMap.remove(player)
//        }
    }
}