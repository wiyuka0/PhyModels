package com.volatila.phymodels.command

import com.volatila.phymodels.model.ModelManager
import com.volatila.phymodels.physics.obj.ModelEntity
import com.volatila.phymodels.physics.objmanager.ObjectManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class RemoveModel: CommandExecutor, TabCompleter{
    override fun onCommand(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>
    ): Boolean {

        if(!p0.hasPermission("phymodels.model")) return true
        val modelEntity = ObjectManager.livingModels.values.filter { it.model.name == p3[0] }[0]
        ModelManager.removeModel(modelEntity)

        return true
    }

    override fun onTabComplete(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>
    ): List<String?>? {
        return ObjectManager.livingModels.values.map { it.model.name }
    }


}