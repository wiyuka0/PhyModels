package com.volatila.phymodels.bukkitlistener

import com.volatila.phymodels.physics.obj.toJmeVector3f
import com.volatila.phymodels.physics.obj.toVector3f
import com.volatila.phymodels.util.PhysicsUtil
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.ExplosionPrimeEvent

class ExplosionListener: Listener {

    @EventHandler
    fun onExplosionHappen(event: ExplosionPrimeEvent){
        val centerLocation = event.entity.location.toVector3f().toJmeVector3f()

        val nearbyRigidBodies = PhysicsUtil.getNearbyRigidBodies(centerLocation, event.radius)

        nearbyRigidBodies.forEach { (rigidBody, distance) ->
            val impulseDirection = rigidBody.getPhysicsLocation(null).subtract(centerLocation).normalize()
            val impulseLength = 1f / distance * 10 * (event.radius / 2f)

            val impulseVector = impulseDirection.mult(impulseLength)

            rigidBody.applyCentralImpulse(impulseVector)
        }
    }
}