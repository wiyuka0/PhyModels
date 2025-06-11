package com.volatila.phymodels.physics.listener.collisionlistener

import com.jme3.bullet.collision.PhysicsCollisionObject
import com.jme3.math.Vector3f
import com.volatila.phymodels.physics.bulletextended.ControllablePhysicsSpace

interface CollisionListener{
    fun onCollisionStarted(rigidBody1: PhysicsCollisionObject, rigidBody2: PhysicsCollisionObject)
    fun onCollisionProcessed(
        rigidBody1: PhysicsCollisionObject,
        rigidBody2: PhysicsCollisionObject,
        point1: Vector3f,
        point2: Vector3f
    )
    fun onCollisionEnded(rigidBody1: PhysicsCollisionObject, rigidBody2: PhysicsCollisionObject)

    companion object{
        fun initListeners(space: ControllablePhysicsSpace){
            space.registerCollisionListener(PlayerVelocity())
            space.registerCollisionListener(LiquidParticle())
        }
    }
}