package com.wiyuka.phymodels

import com.wiyuka.phymodels.model.Model
import com.wiyuka.phymodels.physics.obj.ModelEntity
import com.wiyuka.phymodels.model.ModelManager
import com.wiyuka.phymodels.physics.Physics
import com.wiyuka.phymodels.physics.obj.toJmeVector3f
import com.wiyuka.phymodels.physics.objmanager.ObjectManager
import org.bukkit.Location
import javax.vecmath.Vector3f

class PhysAPI {
    companion object{
        enum class BodyType{
            STATICAL,
            PHYSICAL,
            SCRIPTED
        }
        fun generateModel(model: Model, scale: Float, position: Location, bodyType: BodyType, worldName: String): ModelEntity {
            return ModelManager.generateModel(model, position, scale, bodyType, worldName)
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