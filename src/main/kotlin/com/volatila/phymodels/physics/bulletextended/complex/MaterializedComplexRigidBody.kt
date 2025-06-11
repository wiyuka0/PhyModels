package com.volatila.phymodels.physics.bulletextended.complex

import com.jme3.bullet.objects.PhysicsRigidBody
import com.volatila.phymodels.model.Model
import org.bukkit.Material

class MaterializedComplexRigidBody(val mainBody: PhysicsRigidBody): CompoundRigidBody(mainBody) {

    fun addSubBody(offset: Model.Vector3i, body: PhysicsRigidBody, material: Material) {

    }

    override fun removeSubBody(offset: Model.Vector3i) {

    }


}