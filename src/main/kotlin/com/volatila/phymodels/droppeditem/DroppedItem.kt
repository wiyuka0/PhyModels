package com.volatila.phymodels.droppeditem

import com.jme3.bullet.objects.PhysicsRigidBody
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack

data class DroppedItem (
    val itemStack: ItemStack,
    val rigidBody: PhysicsRigidBody,
    val itemDisplay: ItemDisplay
)