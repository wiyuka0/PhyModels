package com.volatila.phymodels.physics.listener.ticklistener

import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.PhysicsTickListener
import com.volatila.phymodels.PhyModels
import com.volatila.phymodels.debug.DebugUtil
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
        }

    }

    override fun physicsTick(p0: PhysicsSpace?, p1: Float) {
    }
}