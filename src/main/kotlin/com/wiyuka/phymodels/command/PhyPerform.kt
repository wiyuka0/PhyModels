package com.wiyuka.phymodels.command

import com.wiyuka.phymodels.PhysicsTickManager
import com.wiyuka.phymodels.physics.Physics
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
        p0.sendMessage("===============")
        Physics.costTimes.forEach { (name, time) ->
            p0.sendMessage("N: $name - $time ms")
        }
        return true
    }
}