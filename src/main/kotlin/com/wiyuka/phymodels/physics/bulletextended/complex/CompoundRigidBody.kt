package com.wiyuka.phymodels.physics.bulletextended.complex

import com.jme3.bullet.joints.SixDofJoint
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.math.Quaternion
import com.jme3.math.Vector3f
import org.joml.Vector3i
import com.wiyuka.phymodels.physics.obj.toJmeVector3f
import com.wiyuka.phymodels.util.toVector3f

open class CompoundRigidBody(
    val mainRigidBody: PhysicsRigidBody,
    private val subBodies: MutableMap<Vector3i, PhysicsRigidBody> = mutableMapOf(),
): PhysicsRigidBody(mainRigidBody.collisionShape) {
    // 复合刚体
    // 由多个刚体组成的刚体

    class Solution {
        fun minOperations(word1: String, word2: String): Int {
            //split

            val w1t = word1.toCharArray()
            val w2t = word2.toCharArray()

            val wordLen = word1.length - 1
            val exchangeHashMap = hashMapOf<Pair<Char, Char>, Int>()
            for (i in 0 until wordLen) {
                var operationCount = 0
                val w1c = w1t[i]
                val w2c = w2t[i]
                if (exchangeHashMap[w2c to w1c] == 1) {
                    exchangeHashMap[w2c to w1c] == 0
                    operationCount++
                } else{
                    exchangeHashMap[w1c to w2c] = 1
                }


            }
            return 0
        }
    }

    open fun addSubBody(offset: Vector3i, body: PhysicsRigidBody) {
        subBodies[offset] = body
        val bodyOffsetPos = mainRigidBody.getPhysicsLocation(null).add(offset.toVector3f().toJmeVector3f())
        body.setPhysicsLocation(bodyOffsetPos)

        // add joint
        val joint = SixDofJoint(
            mainRigidBody,
            body,
            Vector3f.ZERO,
            Vector3f.ZERO,
            true
        )
        joint.setLinearLowerLimit(Vector3f.ZERO)
        joint.setLinearUpperLimit(Vector3f.ZERO)
        joint.setAngularLowerLimit(Vector3f.ZERO)
        joint.setAngularUpperLimit(Vector3f.ZERO)

//        ObjectManager.addObjectToPhysicsWorld(joint)
//        ObjectManager.addObjectToPhysicsWorld(body)

        for ((_, body) in subBodies) {
            body.setIgnoreList(subBodies.map { it.value }.filter { it.nativeId() != body.nativeId() }.toTypedArray())
        }

    }
    open fun removeSubBody(offset: Vector3i) {
        subBodies.remove(offset)

    }

    override fun getPhysicsLocation(storeResult: Vector3f): Vector3f {
        return mainRigidBody.getPhysicsLocation(storeResult)
    }
    override fun getPhysicsRotation(storeResult: Quaternion): Quaternion {
        return mainRigidBody.getPhysicsRotation(storeResult)
    }

    override fun setPhysicsLocation(location: Vector3f) {
        mainRigidBody.setPhysicsLocation(location)
        subBodies.forEach { (offset, body) ->
            body.setPhysicsLocation(location.add(offset.toVector3f().toJmeVector3f()))
        }
    }
    fun getGeometricCenter(){

    }

    override fun getMass(): Float {
        var totalMass = 0f
        subBodies.forEach { (_, body) ->
            totalMass += body.mass
        }
        return totalMass
    }

    override fun getFriction(): Float {
        var totalFriction = 0f
        subBodies.forEach { (_, body) ->
            totalFriction += body.friction
        }
        return totalFriction / subBodies.size
    }

    override fun getRestitution(): Float {
        var totalRestitution = 0f
        subBodies.forEach { (_, body) ->
            totalRestitution += body.restitution
        }
        return totalRestitution / subBodies.size
    }

    override fun activate() {
        this.mainRigidBody.activate(true)
        subBodies.forEach { (_, body) ->
            body.activate(true)
        }
    }

}