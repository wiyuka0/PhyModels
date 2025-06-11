package com.volatila.phymodels.physics.listener.ticklistener

import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.PhysicsTickListener
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.math.Vector3f
import com.volatila.phymodels.physics.obj.toJmeVector3f
import com.volatila.phymodels.physics.obj.toLocation
import com.volatila.phymodels.physics.obj.toVecmathVector3f
import com.volatila.phymodels.physics.obj.toVector3f
import com.volatila.phymodels.physics.objmanager.ObjectManager
import org.bukkit.Bukkit

class DistanceOptimization internal constructor(private val thresholdDistance: Float, private val checkTickDelay: Int) :
    PhysicsTickListener {
    private var currentTick = 0
    private val sleepingBodies = mutableMapOf<PhysicsRigidBody, Float>() //rigidBody -> mass

    override fun prePhysicsTick(physicsSpace: PhysicsSpace?, v: Float) {
        if (currentTick >= checkTickDelay) {
            currentTick = 0
            val bodies = ObjectManager.livingModels.map { it.value.rigidBody }
            val playerList = Bukkit.getOnlinePlayers()
            bodies.forEach { body ->
                val nearbyPlayers = playerList.filter { player ->
                    val playerLocation = player.location.toVector3f().toJmeVector3f()
                    return@filter (playerLocation.distance(body.getPhysicsLocation(null)) <= thresholdDistance)
                }

                if (nearbyPlayers.isEmpty()) {
                    if (body.mass == 0f) return@forEach
                    val bodyMass = body.mass
                    body.mass = 0f
                    sleepingBodies[body] = bodyMass
                    return@forEach
                }
                if (sleepingBodies.containsKey(body)) {
                    body.mass = sleepingBodies[body]!!
                    body.activateBySmallRandomForce()
                }
                if (nearbyPlayers.minOf {
                        it.location.distance(
                            body.getPhysicsLocation(null).toVecmathVector3f().toLocation(it.location.world)
                        )
                    } < 3) {
                    body.activateBySmallRandomForce()
                    body.activate(true)
                }
            }
        }
        currentTick++
    }

    private fun PhysicsRigidBody.activateBySmallRandomForce() {
        this.applyCentralForce(Vector3f(0f, 0.001f, 0f))
    }

    override fun physicsTick(physicsSpace: PhysicsSpace?, v: Float) {
    }
}