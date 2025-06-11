package com.volatila.phymodels.physics.objmanager

import com.jme3.bullet.objects.PhysicsRigidBody
import com.volatila.phymodels.physics.bulletextended.ControllablePhysicsSpace
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class TaskProcessor {
    companion object{
        enum class ExecutionType{
            DELETE,
            NEW
        }
        internal val requestList: ConcurrentHashMap<Any, ExecutionType> = ConcurrentHashMap()
        internal val tasks: ConcurrentLinkedQueue<Pair<PhysicsRigidBody, (body: PhysicsRigidBody) -> Unit>> = ConcurrentLinkedQueue()

        internal fun handleRequest(physicsWorld1: ControllablePhysicsSpace) {
            requestList.forEach {
                val (body, type) = it
                when(type){
                    ExecutionType.NEW -> physicsWorld1.add(body)
                    ExecutionType.DELETE -> physicsWorld1.remove(body)
                }
            }
            requestList.clear()
            val taskSnapshot = tasks.toList()
            for (runnable in taskSnapshot){
                runnable.second(runnable.first)
            }
            tasks.map{it.first}.filter {
                it in taskSnapshot.map { it.first }
            }.apply {
                tasks.removeAll(taskSnapshot)
            }

        }

    }
}