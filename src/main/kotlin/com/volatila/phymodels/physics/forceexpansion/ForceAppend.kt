package com.volatila.phymodels.physics.forceexpansion

import com.jme3.bullet.objects.PhysicsRigidBody
import com.volatila.phymodels.physics.Physics

interface ForceAppend {
    fun applyForce(rigidBody: PhysicsRigidBody)
}