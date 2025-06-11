package com.volatila.phymodels.model

import javax.vecmath.Vector3f

data class ModelScriptData(
    var startTime: Int = -1,
    val keyframes: List<Vector3f> = listOf(), // keyframe -> 毫秒相对于开始时间的偏移量
    val interpolationMode: Interpolation,
    var currentAnimationIndex: Int = 0,
    val repeatable: Boolean = false,
    var backward: Boolean = false,
){
}
