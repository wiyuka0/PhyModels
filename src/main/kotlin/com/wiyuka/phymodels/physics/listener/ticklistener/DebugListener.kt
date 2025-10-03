package com.wiyuka.phymodels.physics.listener.ticklistener

import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.PhysicsTickListener
import com.jme3.bullet.collision.shapes.BoxCollisionShape
import com.jme3.math.Vector3f
import com.wiyuka.phymodels.PhyModels
import com.wiyuka.phymodels.debug.DebugUtil
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Color

class DebugListener : PhysicsTickListener{
    override fun prePhysicsTick(p0: PhysicsSpace?, p1: Float) {
        if(!PhyModels.debug) return

        val allRigidBody = p0?.rigidBodyList

        allRigidBody?.forEach {
            val selfPos = it.getPhysicsLocation(null)

            if(it.mass == 0f) return@forEach

            DebugUtil.drawParticle(Color.WHITE, Location(Bukkit.getWorlds()[0], selfPos.x.toDouble(), selfPos.y.toDouble(),
                selfPos.z.toDouble()
            ))

            if(it.collisionShape is BoxCollisionShape) {
                val halfExtents = (it.collisionShape as BoxCollisionShape).getHalfExtents(null)
                val localVertices = mutableListOf<Vector3f>()
                // 根据半长宽高的8种正负组合计算出8个顶点
                localVertices.add(Vector3f(-halfExtents.x, -halfExtents.y, -halfExtents.z))
                localVertices.add(Vector3f( halfExtents.x, -halfExtents.y, -halfExtents.z))
                localVertices.add(Vector3f( halfExtents.x,  halfExtents.y, -halfExtents.z))
                localVertices.add(Vector3f(-halfExtents.x,  halfExtents.y, -halfExtents.z))
                localVertices.add(Vector3f(-halfExtents.x, -halfExtents.y,  halfExtents.z))
                localVertices.add(Vector3f( halfExtents.x, -halfExtents.y,  halfExtents.z))
                localVertices.add(Vector3f( halfExtents.x,  halfExtents.y,  halfExtents.z))
                localVertices.add(Vector3f(-halfExtents.x,  halfExtents.y,  halfExtents.z))


                val rbRot = it.getPhysicsRotation(null)
                val rbLoc = it.getPhysicsLocation(null)

                val worldVertices = mutableListOf<Vector3f>()

                for (f in localVertices) {
                    val local = Vector3f(f)
                     

                }

            }
        }

    }

    override fun physicsTick(p0: PhysicsSpace?, p1: Float) {
    }
}