package com.wiyuka.phymodels.physics.listener.ticklistener

import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.PhysicsTickListener
import com.wiyuka.phymodels.PhyModels
import com.wiyuka.phymodels.PhysicsTickManager.Companion.updateNearbyBlocks
import com.wiyuka.phymodels.model.Model
import com.wiyuka.phymodels.physics.Physics
import com.wiyuka.phymodels.physics.body.BodyManager
import io.papermc.paper.command.brigadier.argument.ArgumentTypes.world
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import org.joml.Vector3i

class BodySync: PhysicsTickListener {
    val worldTemp = mutableMapOf<Long, String>()
    override fun prePhysicsTick(p0: PhysicsSpace?, p1: Float) {
        if(p0 == null) return
        for ((id, body) in BodyManager.bodies) {
            for ((name, rb) in body.rigidBody) {
                val targetItemDisplay = body.itemDisplay[name]!!
                val rbRotation = rb.getPhysicsRotation(null)
                val rbLocation = rb.getPhysicsLocation(null)

//                targetItemDisplay.teleportDuration = 1

                var worldName = "null"
            //teleport会报错
                if(!worldTemp.containsKey(p0.nativeId())) {
                    val world = Physics.physicsWorlds.filterValues { it.first.nativeId() == p0.nativeId() }
                    worldName = world.keys.first()
                    worldTemp[p0.nativeId()] = worldName
                } else worldName = worldTemp[p0.nativeId()]!!

                targetItemDisplay.scheduler.run(PhyModels.plugin, { task ->
                    targetItemDisplay.teleport(Location(Bukkit.getWorld(worldName), rbLocation.x.toDouble(), rbLocation.y.toDouble(), rbLocation.z.toDouble()))
                }, null)
//                val oldTransformation = targetItemDisplay.transformation
                val newTransformation = Transformation(
                    Vector3f(0f, 0f, 0f),
                    Quaternionf(rbRotation.x, rbRotation.y, rbRotation.z, rbRotation.x),
                    Vector3f(1f, 1f, 1f),
                    Quaternionf()
                )
//                targetItemDisplay.interpolationDelay = 0
//                targetItemDisplay.interpolationDuration = 1
//                targetItemDisplay.transformation = newTransformation
                updateNearbyBlocks(Location(Bukkit.getWorld(worldName), rbLocation.x.toDouble(), rbLocation.y.toDouble(), rbLocation.z.toDouble()), Vector3i(2, 2, 2))
            }

        }
    }

    override fun physicsTick(p0: PhysicsSpace?, p1: Float) {
//        TODO("Not yet implemented")
    }
}