package com.wiyuka.phymodels.physics.forceexpansion.forces

import com.jme3.bullet.objects.PhysicsRigidBody
import com.wiyuka.phymodels.physics.applyCentralAcceleration
import com.wiyuka.phymodels.physics.forceexpansion.ForceAppend
import com.jme3.math.Vector3f
import com.wiyuka.phymodels.physics.obj.ModelEntity

class AirBuoyancy: ForceAppend {

    override val excludeTagList: List<String>
        get() = listOf()

    override fun initialize() {
    }
    override fun applyForce(model: ModelEntity) {
        val rigidBody = model.rigidBody
        rigidBody.applyCentralForce(Vector3f(0f, 0.3f, 0f))
    }
}