package com.wiyuka.phymodels.command

import com.wiyuka.phymodels.PhysAPI
import com.wiyuka.phymodels.model.Interpolation
import com.wiyuka.phymodels.model.Model
import com.wiyuka.phymodels.model.ModelManager
import com.wiyuka.phymodels.model.ModelScriptData
import com.wiyuka.phymodels.physics.obj.toLocation
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import javax.vecmath.Vector3f

class AddScript: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        try {
            if (sender !is Player) sender.sendMessage("Player has to be a BukkitPlayer!")
            val player = sender as Player
            if(!player.hasPermission("phymodels.model")) return true
            if (args.size < 2) {
                player.sendMessage("Usage: /addscript <model> <scale> <repeatable> <keyframe1>;<keyframe2>;[keyframe3];...")
                player.sendMessage("About the keyframe: \"{X,Y,Z,time}\" geometric center.")
                return false
            }
            val modelName = args[0]
            val keyframes = mutableMapOf<Vector3f, Int>()
            val keyframeList = mutableListOf<Vector3f>()

            var startTime = 0
            var endTime = 0
            var first = true

            val scale = args[1].toFloat()
            val repeatable = args[2].toBoolean()

            val keyframeSourceTextList = args[3].split(';')
            keyframeSourceTextList.forEach {
                val removeBraces = it.replace("{", "").replace("}", "")
                val numbers = removeBraces.split(',')
                val vector3f = Vector3f(numbers[0].toFloat(), numbers[1].toFloat(), numbers[2].toFloat())
                val time = numbers[3].toInt()

                if (first) {
                    startTime = time
                    first = false
                } else {
                    endTime = time
                }

                keyframes[vector3f] = time
                keyframeList.add(vector3f)
            }
            val durationTimeSecond = (endTime.toDouble() - startTime.toDouble()).toDouble()
            val durationTicks = durationTimeSecond * 20
            val interpolatedKeyframes = Interpolation.cubicSpline3d(keyframeList, durationTicks.toInt())

            val modelScriptData = ModelScriptData(
                startTime = -1,
                keyframes = interpolatedKeyframes,
                interpolationMode = Interpolation.CUBIC_SPLINE,
                repeatable = repeatable,
            )

            val model = Model.getModelByName(modelName)
            if (model == null) {
                player.sendMessage("The model is not exist.")
                return true
            }
            ModelManager.generateModel(
                model,
                keyframeList.first().toLocation(player.world),
                scale,
                modelType = PhysAPI.Companion.BodyType.SCRIPTED,
                modelScriptData = modelScriptData,
                worldName = player.world.name
            )

        }catch (e:Exception){
            sender.sendMessage("Usage: /addscript <model> <scale> <keyframe1>;<keyframe2>;[keyframe3];...")
            sender.sendMessage("About the keyframe: \"{X,Y,Z,time}\" geometric center.")
            sender.sendMessage("Example: /addscript test 1.5 {0,0,0,0};{1,1,1,10};{2,2,2,20}")
        }


        return true
    }
}