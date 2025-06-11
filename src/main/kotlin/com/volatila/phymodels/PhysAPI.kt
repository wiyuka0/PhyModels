package com.volatila.phymodels

import com.volatila.phymodels.model.Model
import com.volatila.phymodels.physics.obj.ModelEntity
import com.volatila.phymodels.model.ModelManager
import com.volatila.phymodels.physics.Physics
import com.volatila.phymodels.physics.obj.toJmeVector3f
import com.volatila.phymodels.physics.objmanager.ObjectManager
import org.bukkit.Location
import javax.vecmath.Vector3f

class PhysAPI {
    companion object{
        enum class BodyType{
            STATICAL,
            PHYSICAL,
            SCRIPTED
        }
        fun generateModel(model: Model, scale: Float, position: Location, bodyType: BodyType): ModelEntity {
            return ModelManager.generateModel(model, position, scale, bodyType)
        }
        fun removeModel(modelEntity: ModelEntity): Unit {
            ModelManager.removeModel(modelEntity)
        }
        /**
         * @return UniqueId -> ModelEntity
         */
        fun allLiveModels(): Map<String, ModelEntity> {
            return ObjectManager.livingModels
        }
        /**
         * @param modelEntity 要施加力的模型
         * @param position 力在世界坐标中的位置
         * @param force 力向量
         */
        fun applyForce(modelEntity: ModelEntity, position: Vector3f, force: Vector3f){
            val modelRigidBody = modelEntity.rigidBody
            modelRigidBody.applyForce(position.toJmeVector3f(), force.toJmeVector3f())
        }
    }
}