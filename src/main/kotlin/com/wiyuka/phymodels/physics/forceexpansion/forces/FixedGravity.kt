package com.wiyuka.phymodels.physics.forceexpansion.forces

import com.jme3.bullet.objects.PhysicsRigidBody
import com.wiyuka.phymodels.physics.applyAcceleration
import com.wiyuka.phymodels.physics.forceexpansion.ForceAppend
import com.wiyuka.phymodels.physics.obj.ModelEntity
import com.wiyuka.phymodels.physics.obj.toJmeVector3f
import com.wiyuka.phymodels.physics.scheduler
import kotlin.math.abs

class FixedGravity: ForceAppend {

    override val excludeTagList: List<String>
        get() = listOf("limb")

    override fun initialize() {
    }
    override fun applyForce(model: ModelEntity) {
        val rigidBody = model.rigidBody

        model.absoluteMassCenter
        val rbVel = rigidBody.getLinearVelocity(null)
        val threshold = 0.001f
        if (
            abs(rbVel.x) >= threshold ||
            abs(rbVel.y) >= threshold ||
            abs(rbVel.z) >= threshold
        ) rigidBody.scheduler {
            it.applyAcceleration(
                com.jme3.math.Vector3f(0f, -1f, 0f),
                model.massCenterRel.toJmeVector3f()
            )
        }
    }

}