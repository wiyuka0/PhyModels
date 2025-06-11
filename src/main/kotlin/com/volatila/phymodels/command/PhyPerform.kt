package com.volatila.phymodels.command

import com.volatila.phymodels.PhysicsTickManager
import com.volatila.phymodels.physics.Physics
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class PhyPerform: CommandExecutor {

    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>): Boolean {
        val tpsData = PhysicsTickManager.getAvgTimeTake()
        p0.sendMessage("""
            MSPT from last 10(T) 5(T) 1(T):
            - ${tpsData.first},
            - ${tpsData.second},
            - ${tpsData.third},
        """.trimIndent())
        return true
    }
}