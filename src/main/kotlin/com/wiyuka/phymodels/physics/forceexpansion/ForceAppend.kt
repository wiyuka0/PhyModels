package com.wiyuka.phymodels.physics.forceexpansion

import com.jme3.bullet.objects.PhysicsRigidBody
import com.wiyuka.phymodels.physics.Physics
import com.wiyuka.phymodels.physics.obj.ModelEntity

interface ForceAppend {
    val excludeTagList: List<String>
    fun initialize()
    fun applyForce(model: ModelEntity)
}