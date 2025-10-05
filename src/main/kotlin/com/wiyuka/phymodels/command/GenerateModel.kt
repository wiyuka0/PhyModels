package com.wiyuka.phymodels.command

import com.wiyuka.phymodels.PhysAPI.Companion.BodyType
import com.wiyuka.phymodels.model.Model
import com.wiyuka.phymodels.model.ModelManager
import io.papermc.paper.command.CommandBlockHolder
import org.bukkit.Location
import org.bukkit.block.CommandBlock
import org.bukkit.command.BlockCommandSender
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
//            var location: Location? = null
            if(p0 is Player) {
                val player = p0 as Player
                if (!player.hasPermission("phymodels.model")) return true
            }
            val location = when(p0) {
                is Player -> {
                    p0.location
                }
                is BlockCommandSender -> {
                    p0.block.location
                }
                else -> {
                    null
                }
            }
            val modelInstance = Model.getModelByName(modelName)

            if(location == null) {
                p0.sendMessage("null")
                return true
            }
            if (modelInstance == null) {
                p0.sendMessage("The model is not exist.")
                return true
            }


            val scale = p3[1].toFloat()
            ModelManager.generateModel(modelInstance, location, scale, BodyType.PHYSICAL, location.world.name)
            p0.sendMessage("Model ${modelInstance.name} generated.")
        }catch (e:Exception){
            p0.sendMessage("Usage: /generateModel <modelName> <scale>")
        }
        return true
    }
}