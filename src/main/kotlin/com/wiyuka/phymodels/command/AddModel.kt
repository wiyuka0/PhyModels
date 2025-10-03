package com.wiyuka.phymodels.command

import com.wiyuka.phymodels.PhyModels
import com.wiyuka.phymodels.model.Model
import org.joml.Vector3i
import com.wiyuka.phymodels.model.ModelManager
import com.wiyuka.phymodels.util.toVector3f
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.io.File
import java.nio.file.Files
import kotlin.math.max
import kotlin.math.min

class AddModel(val plugin: Plugin): CommandExecutor, TabCompleter {
    override fun onCommand(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>
    ): Boolean {
        var player: Player? = null
        if(p0 is Player) player = p0
        if(player == null) {
            p0.sendMessage("This command is player only.")
            return true
        }
        if(!player.hasPermission("phymodels.model")) return true

        try {
            val loc1 = Location(player.world, p3[0].toDouble(), p3[1].toDouble(), p3[2].toDouble())
            val loc2 = Location(player.world, p3[3].toDouble(), p3[4].toDouble(), p3[5].toDouble())

            val center = Vector3i(player.location.x.toInt(), player.location.y.toInt(), player.location.z.toInt())

            val model = ModelManager.makeModel(loc1, loc2, center, p3[6].toFloat(), p3[7], modelWorldPosition = center.toVector3f()).first
            //save to File
            val jsonContent = PhyModels.jsonEncoder.toJson(model)
                .replace(" ", "__SPACE__")
                .replace(".", "__DOT__")
                .replace("+", "__ADD__")
                .replace("-", "__SUB__")
                .replace("(", "__LEFT__BRACES")
                .replace(")", "__RIGHT__BRACES")

            val fileName = "${model.name}.json"

            val file = File(Model.modelFolder(plugin), fileName)

            if(!file.exists()) file.createNewFile()
            Files.writeString(file.toPath(), jsonContent)

            player.sendMessage("Finish adding model ${model.name} to the file.")

        }catch(e: Exception){
            p0.sendMessage("Usage: /addmodel <x1> <y1> <z1> <x2> <y2> <z2> <mass> <name>")
        }
        return true
    }

    private fun Location.toVector3i(): Vector3i{
        return Vector3i(this.blockX, this.blockY, this.blockZ)
    }

    override fun onTabComplete(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>
    ): List<String?>? {
        val player = p0 as Player
        val location = player.location
        if(p3.isEmpty()) return listOf(location.x.toString())
        if(p3.size == 1) return listOf(location.z.toString())
        if(p3.size == 2) return listOf(location.z.toString())
        return listOf("0")
    }
}