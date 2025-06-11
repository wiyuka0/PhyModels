package com.volatila.phymodels.physics.objmanager

import com.jme3.bullet.objects.PhysicsRigidBody
import com.volatila.phymodels.physics.obj.ModelEntity
import com.volatila.phymodels.physics.objmanager.TaskProcessor.Companion.ExecutionType
import com.volatila.phymodels.physics.objmanager.TaskProcessor.Companion.requestList
import java.util.concurrent.ConcurrentHashMap

class ObjectManager {
    companion object{
        var livingModels: ConcurrentHashMap<String, ModelEntity> = ConcurrentHashMap()
        private var modelBodies: HashMap<String, PhysicsRigidBody> = hashMapOf() // uuid -> rigidbody

        fun getModelBody(modelEntity: ModelEntity): PhysicsRigidBody {
            return modelBodies[modelEntity.uniqueId] ?: throw NullPointerException("Model body not found")
        }

        fun addModel(modelEntity: ModelEntity, modelRigidBody: PhysicsRigidBody) {
            modelEntity.rigidBody = modelRigidBody
            livingModels[modelEntity.uniqueId] = modelEntity
        }

        fun bindRigidBody(modelEntity: ModelEntity, modelRigidBody: PhysicsRigidBody) {
            modelBodies[modelEntity.uniqueId] = modelRigidBody
        }

        fun addObjectToPhysicsWorld(any: Any) {
            requestList.put(any, ExecutionType.NEW)
        }
        fun removeObjectFromPhysicsWorld(any: Any) {
            requestList.put(any, ExecutionType.DELETE)
        }
    }
}