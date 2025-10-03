package com.wiyuka.phymodels.bukkitlistener

import com.wiyuka.phymodels.physics.obj.toJmeVector3f
import com.wiyuka.phymodels.physics.obj.toVector3f
import com.wiyuka.phymodels.util.PhysicsUtil
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.ExplosionPrimeEvent

class ExplosionListener: Listener {

    @EventHandler
    fun onExplosionHappen(event: ExplosionPrimeEvent){
        val centerLocation = event.entity.location.toJmeVector3f()

        val nearbyRigidBodies = PhysicsUtil.getNearbyRigidBodies(centerLocation, event.radius, event.entity.world.name)

        nearbyRigidBodies.forEach { (rigidBody, distance) ->
            val impulseDirection = rigidBody.getPhysicsLocation(null).subtract(centerLocation).normalize()
            val impulseLength = 1f / distance * 10 * (event.radius / 2f)

            val impulseVector = impulseDirection.mult(impulseLength)

            rigidBody.applyCentralImpulse(impulseVector)
        }
    }
}