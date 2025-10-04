package com.wiyuka.phymodels.command

import com.wiyuka.phymodels.physics.cloth.ClothManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ClothCommand: CommandExecutor {
    override fun onCommand(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>
    ): Boolean {
        //     fun addCloth(baseLocation: Location, sideLength: Float, width: Int, length: Int, height: Float, totalMass: Float, world: World): Cloth{

        if(p3[0] == "clear"){
            ClothManager.clear()
            return true
        }

        val player = p0 as Player



        player.inventory.addItem()

        val baseLocation = player.location
        val sideLength = p3[0].toFloat()
        val width = p3[1].toInt()
        val length = p3[2].toInt()
        val height = p3[3].toFloat()
        val totalMass = p3[4].toFloat()

        ClothManager.addCloth(
            baseLocation,
            sideLength,
            width,
            length,
            height,
            totalMass
        )
        player.sendMessage("Finished add cloth.")

        return true
    }
}