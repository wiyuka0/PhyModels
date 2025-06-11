package com.volatila.phymodels.physics.forceexpansion.forces

import com.jme3.bullet.objects.PhysicsRigidBody
import com.volatila.phymodels.physics.forceexpansion.ForceAppend
import com.volatila.phymodels.physics.obj.toJmeVector3f
import com.volatila.phymodels.physics.obj.toVecmath
import org.bukkit.Material

class ThrustEngine: ForceAppend {
    override fun applyForce(rigidBody: PhysicsRigidBody) {
        val modelEntity = com.volatila.phymodels.physics.objmanager.ObjectManager.livingModels.values.find { it.rigidBody == rigidBody } ?: return
        val engineType = Material.END_ROD
        for ((v3i, block) in modelEntity.displayBlocks) {
            if (block.entity.block.material  == engineType) {
                val thrustForce = 1f // 设定一个推力值
                val forceDirection = block.entity.location.direction // 推力方向
                // 向下旋转90°
                val forceVector = forceDirection.multiply(thrustForce)
                rigidBody.applyForce(forceVector.toVector3f().toVecmath().toJmeVector3f(), v3i.toVector3f().toJmeVector3f())
            }
        }
    }
}