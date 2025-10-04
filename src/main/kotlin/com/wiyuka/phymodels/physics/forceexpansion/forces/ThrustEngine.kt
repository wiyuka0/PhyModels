package com.wiyuka.phymodels.physics.forceexpansion.forces

import com.jme3.bullet.objects.PhysicsRigidBody
import com.wiyuka.phymodels.physics.forceexpansion.ForceAppend
import com.wiyuka.phymodels.physics.obj.ModelEntity
import com.wiyuka.phymodels.physics.obj.toJmeVector3f
import com.wiyuka.phymodels.physics.obj.toVecmath
import com.wiyuka.phymodels.util.toVector3f
import org.bukkit.Material

class ThrustEngine: ForceAppend {
    override val excludeTagList: List<String>
        get() = listOf()

    override fun initialize() {
    }

    override fun applyForce(model: ModelEntity) {
        val rigidBody = model.rigidBody
        val modelEntity = com.wiyuka.phymodels.physics.objmanager.ObjectManager.livingModels.values.find { it.rigidBody == rigidBody } ?: return
        val engineType = Material.END_ROD
        for ((v3i, block) in modelEntity.displayBlocks) {
            if (block.entity.block.material  == engineType) {
                val thrustForce = 10000f // 设定一个推力值
                val forceDirection = block.entity.location.direction // 推力方向
                // 向下旋转90°
                val forceVector = forceDirection.multiply(thrustForce)
                rigidBody.applyForce(forceVector.toVector3f().toVecmath().toJmeVector3f(), v3i.toVector3f().toJmeVector3f())
            }
        }
    }
}