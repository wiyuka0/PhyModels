package com.volatila.phymodels.physics.listener.collisionlistener

import com.jme3.bullet.collision.PhysicsCollisionObject
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.math.Vector3f
import org.bukkit.Bukkit
import java.util.UUID

class PlayerVelocity: CollisionListener {
    override fun onCollisionStarted(
        rigidBody1: PhysicsCollisionObject,
        rigidBody2: PhysicsCollisionObject
    ) {
    }

    override fun onCollisionProcessed(
        rigidBody1: PhysicsCollisionObject,
        rigidBody2: PhysicsCollisionObject,
        point1: Vector3f,
        point2: Vector3f
    ) {
        var otherRigidBody: PhysicsCollisionObject? = null
        var playerPoint: Vector3f? = null
        var otherPoint: Vector3f? = null
        val playerRigidBody =
            if((rigidBody1.userObject as String).startsWith("PlayerBox")) {
                otherRigidBody = rigidBody2
                playerPoint = point1
                otherPoint = point2
                rigidBody1
            } else if((rigidBody2.userObject as String).startsWith("PlayerBox")) {
                otherRigidBody = rigidBody1
                playerPoint = point2
                otherPoint = point1
                rigidBody2
            } else return
        val player = Bukkit.getPlayer(UUID.fromString((playerRigidBody.userObject as String).removePrefix("PlayerBox")))
        if(player == null) return

        if(otherRigidBody !is PhysicsRigidBody || otherRigidBody.mass == 0f) return

        val vel = otherRigidBody.getLinearVelocity(null)
        val relPoint = playerPoint.subtract(otherPoint)
        val force = relPoint.normalize().mult(-0.001f)

        val oldPlayerVel = player.velocity
        player.velocity = oldPlayerVel.apply {
            this.x += force.x
            this.y += force.y
            this.z += force.z
        }
//        player.isSprinting = false
    }

    override fun onCollisionEnded(
        rigidBody1: PhysicsCollisionObject,
        rigidBody2: PhysicsCollisionObject
    ) {
    }
}