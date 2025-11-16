package com.wiyuka.phymodels.command

import com.wiyuka.phymodels.PhysicsTickManager
import com.wiyuka.phymodels.command.Perm.hasPerm
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ScriptStart: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
//        PhyUtil.scriptStarted = true
        if(!hasPerm(sender)) return true

        PhysicsTickManager.startScriptTick()
        return true
    }
}