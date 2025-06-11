package com.volatila.phymodels.physics.forceexpansion

import com.jme3.bullet.objects.PhysicsRigidBody
import com.volatila.phymodels.PhysicsTickManager
import com.volatila.phymodels.physics.forceexpansion.forces.Buoyancy
import com.volatila.phymodels.physics.forceexpansion.forces.ThrustEngine

class ForceAppendManager {
    companion object{
        private val livingForces = mutableListOf<ForceAppend>() // 计算所有每tick要额外添加的力 例如bullet没有自带的力比如浮力 空气阻力 模型上的摩擦力

        fun registerForceAppend(append: ForceAppend) {
            livingForces.add(append)
        }
        fun unregisterForceAppend(append: ForceAppend) {
            livingForces.remove(append)
        }

        fun applyForce(rigidBody: PhysicsRigidBody) {
            for (force in livingForces) PhysicsTickManager.execute {
                force.applyForce(rigidBody)
            }
        }

        fun initAppendForces(){
            this.registerForceAppend(Buoyancy())
            this.registerForceAppend(ThrustEngine())
        }
    }
}