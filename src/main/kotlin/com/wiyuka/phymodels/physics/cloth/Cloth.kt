package com.wiyuka.phymodels.physics.cloth

import com.jme3.bullet.objects.PhysicsRigidBody
import org.bukkit.entity.BlockDisplay

data class Cloth(
    val width: Int,
    val length: Int,
    val height: Float,
    val mass: Float,
    val rigidBodies: MutableMap<String, PhysicsRigidBody>,
    val displayBlocks: MutableMap<String, BlockDisplay>
)