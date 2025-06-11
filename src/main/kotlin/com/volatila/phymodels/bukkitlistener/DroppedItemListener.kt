package com.volatila.phymodels.bukkitlistener

import com.volatila.phymodels.droppeditem.DroppedItemManager
import com.volatila.phymodels.physics.obj.toJmeVector3f
import com.volatila.phymodels.physics.obj.toVecmath
import com.volatila.phymodels.physics.obj.toVector3f
import com.volatila.phymodels.physics.scheduler
import io.papermc.paper.command.brigadier.argument.ArgumentTypes.player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent

class DroppedItemListener: Listener {
    @EventHandler
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        val itemStack = event.itemDrop.itemStack
        val playerWhoDrop = event.player
        val playerEyeLocation = playerWhoDrop.eyeLocation
        val playerDirection = playerWhoDrop.location.direction

        event.isCancelled = true
        
        val playerHandItem = playerWhoDrop.inventory.itemInMainHand.amount--

        val droppedItem = DroppedItemManager.createDroppedItem(itemStack, playerWhoDrop.location)
        droppedItem.rigidBody.scheduler{
            it.applyCentralForce(playerDirection.normalize().multiply(1.5f).toVector3f().toVecmath().toJmeVector3f())
            it.setPhysicsLocation(playerEyeLocation.toVector3f().toJmeVector3f())
        }
    }
}