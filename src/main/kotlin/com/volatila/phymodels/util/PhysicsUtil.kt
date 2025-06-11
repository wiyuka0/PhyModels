package com.volatila.phymodels.util

import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.math.Vector3f
import com.volatila.phymodels.physics.Physics

class PhysicsUtil {
    companion object {
        fun getNearbyRigidBodies(center: Vector3f, radius: Float): Map<PhysicsRigidBody, Float> {
            val result = hashMapOf<PhysicsRigidBody, Float>() //rb -> distance
            Physics.physicsWorld.rigidBodyList.forEach {
                val distance = it.getPhysicsLocation(null).distance(center)
                if (distance <= radius) result[it] = distance
            }
            return result
        }
    }
}