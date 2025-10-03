package com.wiyuka.phymodels.physics.forceexpansion

import com.jme3.bullet.objects.PhysicsRigidBody
import com.wiyuka.phymodels.PhysicsTickManager
import com.wiyuka.phymodels.physics.forceexpansion.forces.AirBuoyancy
import com.wiyuka.phymodels.physics.forceexpansion.forces.Buoyancy
import com.wiyuka.phymodels.physics.forceexpansion.forces.FixedGravity
import com.wiyuka.phymodels.physics.forceexpansion.forces.ThrustEngine
import com.wiyuka.phymodels.physics.obj.ModelEntity

class ForceAppendManager {
    companion object{
        private val livingForces = mutableListOf<ForceAppend>() // 计算所有每tick要额外添加的力 例如bullet没有自带的力比如浮力 空气阻力 模型上的摩擦力

        fun registerForceAppend(append: ForceAppend) {
            livingForces.add(append)
        }
        fun unregisterForceAppend(append: ForceAppend) {
            livingForces.remove(append)
        }

        fun applyForce(model: ModelEntity) {
            livingForces.forEach { force ->
                PhysicsTickManager.execute {
                    val modelTag = model.rigidBody.userObject as String
                    val specialTag = modelTag.substringAfterLast("-special:")
                    for (tag in force.excludeTagList)
                        if (tag in specialTag) return@execute
                    force.applyForce(model)
                }
            }
        }

        fun initAppendForces(){
            this.registerForceAppend(Buoyancy())
            this.registerForceAppend(AirBuoyancy())
            this.registerForceAppend(ThrustEngine())
            this.registerForceAppend(FixedGravity())
        }
    }
}