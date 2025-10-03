package com.wiyuka.phymodels.tools

import com.wiyuka.phymodels.physics.liquid.LiquidManager
import com.wiyuka.phymodels.physics.liquid.LiquidType
import com.wiyuka.phymodels.physics.obj.toJmeVector3f
import com.wiyuka.phymodels.physics.obj.toVector3f
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import net.kyori.adventure.util.ARGBLike
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
        val blockLoc = block.location.toJmeVector3f()

//        Title.title(
//            Component.text("Liquid Active", NamedTextColor.YELLOW).shadowColor(TextColor.color(0, 255, 255)),
//            Component.text("Liquid Active", NamedTextColor.YELLOW)
//        )

        val type =
            when(block.type) {
                Material.LIGHT_BLUE_WOOL -> LiquidType.WATER
                Material.ORANGE_WOOL -> LiquidType.LAVA
                Material.GREEN_WOOL -> LiquidType.SLIME
                Material.STONE -> LiquidType.SMOKE
                else -> return
            }

        LiquidManager.createLiquid(blockLoc, type, 5, player.world.name)
    }

}