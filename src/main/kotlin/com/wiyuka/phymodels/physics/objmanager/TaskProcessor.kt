package com.wiyuka.phymodels.physics.objmanager

import com.jme3.bullet.objects.PhysicsRigidBody
import com.wiyuka.phymodels.physics.bulletextended.ControllablePhysicsSpace
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class TaskProcessor {
    companion object{
        enum class ExecutionType{
            DELETE,
            NEW
        }
        internal val requestList: ConcurrentLinkedQueue<Pair<Any, Pair<ControllablePhysicsSpace ,ExecutionType>>> = ConcurrentLinkedQueue()
        internal val tasks: ConcurrentLinkedQueue<Pair<PhysicsRigidBody, (body: PhysicsRigidBody) -> Unit>> = ConcurrentLinkedQueue()

        internal fun handleRequestInstant() {
            handleRequest()
        }

        internal fun handleRequest() {
            requestList.forEach {
                val (body, executeMeta) = it
                when(executeMeta.second){
                    ExecutionType.NEW -> executeMeta.first.add(body)
                    ExecutionType.DELETE -> executeMeta.first.remove(body)
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