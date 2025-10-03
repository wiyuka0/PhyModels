package com.wiyuka.phymodels.physics.listener.collisionlistener

import com.jme3.bullet.collision.PhysicsCollisionObject
import com.jme3.math.Vector3f
import com.wiyuka.phymodels.PhysicsTickManager.Companion.updateNearbyBlocks
import com.wiyuka.phymodels.model.Model
import com.wiyuka.phymodels.physics.liquid.LiquidType
import com.wiyuka.phymodels.physics.obj.toLocation
import com.wiyuka.phymodels.physics.obj.toVecmathVector3f
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.joml.Vector3i

class LiquidParticle: CollisionListener {
    override fun onCollisionStarted(
        rigidBody1: PhysicsCollisionObject,
        rigidBody2: PhysicsCollisionObject
    ) {
    }

    override fun onCollisionProcessed(
        rigidBody1: PhysicsCollisionObject,
        rigidBody2: PhysicsCollisionObject,
        point1: Vector3f,
        point2: Vector3f
    ) {
//        if(!(rigidBody1.userObject is String && rigidBody2.userObject is String)) return
//        val rb1Info = rigidBody1.userObject as String
//        val rb2Info = rigidBody2.userObject as String
//        if(rb1Info.startsWith("LiquidSphere-") &&
//           rb2Info.startsWith("LiquidSphere-")) {
//            val type1 = LiquidType.valueOf(rb1Info.removePrefix("LiquidSphere-"))
//            val type2 = LiquidType.valueOf(rb2Info.removePrefix("LiquidSphere-"))
//
//            val defaultWorld = Bukkit.getWorlds()[0]
//            val loc1 = point1.toVecmathVector3f().toLocation(defaultWorld)
//            val loc2 = point2.toVecmathVector3f().toLocation(defaultWorld)
//
//            drawLiquidParticle(type1, loc1)
//            drawLiquidParticle(type2, loc2)
//
//            //TODO: 更新流体粒子上次碰撞的发生时间，移除长时间没有发生过碰撞的粒子（蒸发）
//        }
    }

    private fun drawLiquidParticle(type: LiquidType, bukkitLoc: Location) {
        val color = type.config.color
        val dustOptions = DustOptions(color, 1.0f) // 1.0F 为粒子大小
        val world = Bukkit.getWorlds()[0]
        world.spawnParticle(Particle.DUST, bukkitLoc, 2, 0.02, 0.02, 0.02, dustOptions)
        updateNearbyBlocks(bukkitLoc, Vector3i(2, 2, 2))
    }

    override fun onCollisionEnded(
        rigidBody1: PhysicsCollisionObject,
        rigidBody2: PhysicsCollisionObject
    ) {
        TODO("Not yet implemented")
    }
}