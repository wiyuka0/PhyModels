package com.wiyuka.phymodels.physics.bulletextended.complex

import com.jme3.bullet.objects.PhysicsRigidBody
import com.wiyuka.phymodels.model.Model
import org.bukkit.Material
import org.joml.Vector3i

class MaterializedComplexRigidBody(val mainBody: PhysicsRigidBody): CompoundRigidBody(mainBody) {

    fun addSubBody(offset: Vector3i, body: PhysicsRigidBody, material: Material) {

    }

    override fun removeSubBody(offset: Vector3i) {

    }


}