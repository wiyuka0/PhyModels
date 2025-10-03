package com.wiyuka.phymodels.physics.bulletextended

import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.collision.ManifoldPoints
import com.jme3.bullet.collision.PersistentManifolds
import com.jme3.bullet.collision.PhysicsCollisionObject
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.math.Vector3f
import com.wiyuka.phymodels.PhysicsTickManager
import com.wiyuka.phymodels.physics.listener.collisionlistener.CollisionListener

class ControllablePhysicsSpace: PhysicsSpace(BroadphaseType.AXIS_SWEEP_3) {

    val rbHashMap: HashMap<Long, PhysicsCollisionObject> = hashMapOf() // nativeId -> PhysicsCollisionObject

    val collisionListeners = HashSet<CollisionListener>()

    fun registerCollisionListener(collisionListener: CollisionListener){
        collisionListeners.add(collisionListener)
    }

    override fun onContactStarted(manifoldId: Long) {
        super.onContactStarted(manifoldId)
//        PhysicsTickManager.execute {
//
//            val (rigidBody1, rigidBody2) = getPairRigidBody(manifoldId)
//
//            for (it in collisionListeners) if(rigidBody1 != null && rigidBody2 != null)
//                it.onCollisionStarted(rigidBody1, rigidBody2)
//        }
    }

    private fun getPairRigidBody(manifoldId: Long): Pair<PhysicsCollisionObject?, PhysicsCollisionObject?> {
        val rigidBodyId1 = PersistentManifolds.getBodyAId(manifoldId)
        val rigidBodyId2 = PersistentManifolds.getBodyBId(manifoldId)

        val rigidBody1 = this.rbHashMap.values.find { it.nativeId() == rigidBodyId1 }
        val rigidBody2 = this.rbHashMap.values.find { it.nativeId() == rigidBodyId2 }
        return Pair(rigidBody1, rigidBody2)
    }

    override fun add(`object`: Any?) {
        if(`object` is PhysicsRigidBody){
            val nativeId = `object`.nativeId()
            rbHashMap[nativeId] = `object`
        }
        super.add(`object`)
    }

    override fun onContactProcessed(pcoA: PhysicsCollisionObject?, pcoB: PhysicsCollisionObject?, pointId: Long) {
        super.onContactProcessed(pcoA, pcoB, pointId)
//
//        PhysicsTickManager.execute {
//
//            val point1 = Vector3f()
//            ManifoldPoints.getPositionWorldOnA(pointId, point1)
//            val point2 = Vector3f()
//            ManifoldPoints.getPositionWorldOnB(pointId, point2)
//            for (it in collisionListeners) if(pcoA != null && pcoB != null)
//                it.onCollisionProcessed(pcoA, pcoB, point1, point2)
//        }

    }
    override fun onContactEnded(manifoldId: Long) {
        super.onContactEnded(manifoldId)


    }

    override fun update(timeInterval: Float, maxSteps: Int) {
        assert(timeInterval > 0)
        assert(maxSteps > 0)

        val doEnded = false
        val doProcessed = false
        val doStarted = false

        this.update(timeInterval, maxSteps, doEnded, doProcessed, doStarted);
    }
}