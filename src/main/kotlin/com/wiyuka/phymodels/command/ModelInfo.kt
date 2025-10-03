package com.wiyuka.phymodels.command

import com.wiyuka.phymodels.physics.objmanager.ObjectManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class ModelInfo: CommandExecutor, TabCompleter{
    override fun onCommand(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>
    ): Boolean {
        val modelName = p3[0]
        val modelEntity = ObjectManager.livingModels[modelName]
        if(modelEntity == null) {
            p0.sendMessage("Target model is not exists")
            return true
        }
        p0.sendMessage("==== Model $modelName ====")
        p0.sendMessage("Mass: ${modelEntity.rigidBody.mass}")
        p0.sendMessage("Friction: ${modelEntity.rigidBody.friction}")
        p0.sendMessage("Restitution: ${modelEntity.rigidBody.restitution}")
        p0.sendMessage("ActivationState: ${modelEntity.rigidBody.activationState}")
        p0.sendMessage("Gravity: ${modelEntity.rigidBody.getGravity(null)}")
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