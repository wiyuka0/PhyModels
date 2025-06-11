package com.volatila.phymodels.command

import com.volatila.phymodels.PhysAPI.Companion.BodyType
import com.volatila.phymodels.model.Model
import com.volatila.phymodels.model.ModelManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class GenerateModel: CommandExecutor, TabCompleter {

    override fun onTabComplete(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>
    ): List<String?>? {
        val result = mutableListOf<String>()
        for (model in Model.staticModels) result.add(model.name)
        return result
    }

    override fun onCommand(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>
    ): Boolean {
        try {
            val modelName = p3[0]
            val player = p0 as Player

            if(!player.hasPermission("phymodels.model")) return true

            val modelInstance = Model.getModelByName(modelName)

            if (modelInstance == null) {
                player.sendMessage("The model is not exist.")
                return true
            }
            val location = player.location

            val scale = p3[1].toFloat()
            ModelManager.generateModel(modelInstance, location, scale, BodyType.PHYSICAL)
            player.sendMessage("Model ${modelInstance.name} generated.")
        }catch (e:Exception){
            p0.sendMessage("Usage: /generateModel <modelName> <scale>")
        }
        return true
    }
}