package com.wiyuka.phymodels.physics.forceexpansion.forces

import com.jme3.bullet.objects.PhysicsRigidBody
import com.wiyuka.phymodels.physics.forceexpansion.ForceAppend
import com.wiyuka.phymodels.physics.obj.ModelEntity

class AirResistance: ForceAppend {

    override val excludeTagList: List<String>
        get() = listOf()

    override fun initialize() {
    }
    override fun applyForce(model: ModelEntity) {
        val rigidBody = model.rigidBody//        val rbVel = rigidBody.getLinearVelocity(null)
//        if (rbVel.length() < 0.01f) return // 如果速度很小，忽略空气阻力
//
//        if(rigidBody.collisionShape !is CompoundCollisionShape) return
//        val compoundShape = rigidBody.collisionShape as CompoundCollisionShape
//
//        val resistanceModel =
    }
}