package com.wiyuka.phymodels.physics.`interface`

import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.collision.PhysicsCollisionEvent
import com.jme3.bullet.collision.PhysicsCollisionListener
import com.jme3.bullet.collision.PhysicsCollisionObject

//class CollisionEventManager(
//    private val physicsSpace: PhysicsSpace
//) : PhysicsCollisionListener {
//
//    private val listeners = mutableListOf<ExtendedCollisionListener>()
//
//    // 存储当前接触中的物体对
//    private val currentContacts = mutableSetOf<Pair<PhysicsCollisionObject, PhysicsCollisionObject>>()
//    private val frameContacts = mutableSetOf<Pair<PhysicsCollisionObject, PhysicsCollisionObject>>()
//
//    fun addListener(listener: ExtendedCollisionListener) {
//        listeners += listener
//    }
//
//    fun start() {
//        physicsSpace.callback
//    }
//
//    override fun collision(event: PhysicsCollisionEvent) {
//
//        physicsSpace.onContactEnded()
//
//        val objA = event.objectA
//        val objB = event.objectB
//        val pair = orderedPair(objA, objB)
//
//        frameContacts += pair
//
//        if (pair !in currentContacts) {
//            // 新接触，onEnter
//            listeners.forEach { it.onEnter(event) }
//        } else {
//            // 持续接触，onStay
//            listeners.forEach { it.onStay(event) }
//        }
//    }
//
//    // 每帧 tick 后调用，刷新状态（例如在 SimpleApplication.simpleUpdate 中调用）
//    fun update() {
//        val exitedPairs = currentContacts - frameContacts
//        for (pair in exitedPairs) {
//            val (a, b) = pair
//            listeners.forEach { it.onExit(a, b) }
//        }
//
//        currentContacts.clear()
//        currentContacts.addAll(frameContacts)
//        frameContacts.clear()
//    }
//
//    private fun orderedPair(a: PhysicsCollisionObject, b: PhysicsCollisionObject): Pair<PhysicsCollisionObject, PhysicsCollisionObject> {
//        return if (a.hashCode() <= b.hashCode()) a to b else b to a
//    }
//}