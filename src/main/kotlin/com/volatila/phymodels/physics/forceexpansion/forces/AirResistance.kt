package com.volatila.phymodels.physics.forceexpansion.forces

import com.jme3.bullet.objects.PhysicsRigidBody
import com.volatila.phymodels.physics.forceexpansion.ForceAppend

class AirResistance: ForceAppend {
    override fun applyForce(rigidBody: PhysicsRigidBody) {
//        val rbVel = rigidBody.getLinearVelocity(null)
//        if (rbVel.length() < 0.01f) return // 如果速度很小，忽略空气阻力
//
//        if(rigidBody.collisionShape !is CompoundCollisionShape) return
//        val compoundShape = rigidBody.collisionShape as CompoundCollisionShape
//
//        val resistanceModel =
    }
}