package com.volatila.phymodels.physics.bulletextended.complex

import com.jme3.bullet.joints.SixDofJoint
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.math.Quaternion
import com.jme3.math.Vector3f
import com.volatila.phymodels.model.Model.Vector3i
import com.volatila.phymodels.physics.obj.toJmeVector3f
import com.volatila.phymodels.physics.objmanager.ObjectManager
import com.volatila.phymodels.physics.objmanager.TaskProcessor

open class CompoundRigidBody(
    val mainRigidBody: PhysicsRigidBody,
    private val subBodies: MutableMap<Vector3i, PhysicsRigidBody> = mutableMapOf(),
): PhysicsRigidBody(mainRigidBody.collisionShape) {
    // 复合刚体
    // 由多个刚体组成的刚体

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

        ObjectManager.addObjectToPhysicsWorld(joint)
        ObjectManager.addObjectToPhysicsWorld(body)

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