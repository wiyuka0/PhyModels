package com.wiyuka.phymodels.util

import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.math.Vector3f
import com.wiyuka.phymodels.physics.Physics

class PhysicsUtil {
    companion object {
        fun getNearbyRigidBodies(center: Vector3f, radius: Float, worldName: String): Map<PhysicsRigidBody, Float> {
            val result = hashMapOf<PhysicsRigidBody, Float>() //rb -> distance
            Physics.physicsWorlds[worldName]!!.first.rigidBodyList.forEach {
                val distance = it.getPhysicsLocation(null).distance(center)
                if (distance <= radius) result[it] = distance
            }
            return result
        }
    }
}