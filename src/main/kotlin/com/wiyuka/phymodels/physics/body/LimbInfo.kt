package com.wiyuka.phymodels.physics.body

import org.joml.Vector3f

class LimbInfo (
    val offset: Vector3f,
    val scale: Vector3f,
    val rotationAnchorPoint: Vector3f,
    val mass: Float = 1f
)