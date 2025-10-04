package com.wiyuka.phymodels.physics.obj

import org.bukkit.Location
import org.bukkit.entity.BlockDisplay

data class BlockUnit(
    val entity: BlockDisplay,
    val virtualWorldBlock: Location
)