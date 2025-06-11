package com.volatila.phymodels.tools

import com.volatila.phymodels.physics.liquid.LiquidManager
import com.volatila.phymodels.physics.liquid.LiquidType
import com.volatila.phymodels.physics.obj.toJmeVector3f
import com.volatila.phymodels.physics.obj.toVector3f
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class LiquidActive: Listener{

    @EventHandler
    fun liquidActive(e: BlockBreakEvent) {
        if(e.player.inventory.itemInMainHand.type != Material.SUGAR_CANE) return
        val player = e.player
        if(!player.hasPermission("phymodels.model")) return
        e.isCancelled = true
        val block = e.block
        val blockLoc = block.location.toVector3f().toJmeVector3f()

        val type =
            when(block.type) {
                Material.LIGHT_BLUE_WOOL -> LiquidType.WATER
                Material.ORANGE_WOOL -> LiquidType.LAVA
                Material.GREEN_WOOL -> LiquidType.SLIME
                else -> return
            }

        LiquidManager.createLiquid(blockLoc, type, 5)
    }

}