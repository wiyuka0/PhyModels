package com.wiyuka.phymodels.command

import com.wiyuka.phymodels.physics.liquid.LiquidManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class LiquidClear: CommandExecutor {
    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>): Boolean {
        LiquidManager.clear()
        return true
    }
}