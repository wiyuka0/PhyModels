package com.wiyuka.phymodels.debug

import com.wiyuka.phymodels.PhyModels
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions

class DebugUtil {
    companion object{
        fun drawParticle(color: Color, location: Location) {
            if(!PhyModels.debug) return
            val dustOptions = DustOptions(color, 1.0f) // 1.0F 为粒子大小
            location.world.spawnParticle(Particle.DUST, location, 2, 0.1, 0.1, 0.1, dustOptions)
        }
    }
}