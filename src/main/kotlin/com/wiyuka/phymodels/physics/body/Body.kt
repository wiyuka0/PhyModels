package com.wiyuka.phymodels.physics.body

import com.jme3.bullet.objects.PhysicsRigidBody
import org.bukkit.entity.ItemDisplay
import org.joml.Vector3f

data class Body(
    val rigidBody: MutableMap<String, PhysicsRigidBody>,
    val itemDisplay: MutableMap<String, ItemDisplay>,
    val bodyUniqueId: String
) {

    companion object{
        private final val BODY_SCALE = 0.0625f
        val limbData = mutableMapOf<String, LimbInfo>().apply {
            // 尺寸单位: 方块 (blocks)
            // 1. 躯干 (Torso) - 作为身体的中心和参照物
            put("Torso", LimbInfo(
                // 躯干位于双腿之上，中心点在(0, y, 0)
                offset = Vector3f(0f, 1.125f, 0f),
                // 尺寸: 8x12x4 像素 -> 0.5 x 0.75 x 0.25 方块
                scale = Vector3f(0.5f, 0.75f, 0.25f),
                // 躯干是根节点，其自身的旋转锚点为中心点
                rotationAnchorPoint = Vector3f(0f, 0f, 0f),
                // 质量应相对较大
                mass = 8f
            ))

            // 2. 头部 (Head)
            put("Head", LimbInfo(
                // 头部位于躯干之上
                offset = Vector3f(0f, 1.75f, 0f),
                // 尺寸: 8x8x8 像素 -> 0.5 x 0.5 x 0.5 方块
                scale = Vector3f(0.5f, 0.5f, 0.5f),
                // 旋转锚点在头的底部中心（脖子位置）
                rotationAnchorPoint = Vector3f(0f, -0.25f, 0f),
                mass = 4f
            ))

            // 3. 左腿 (LeftLeg)
            put("LeftLeg", LimbInfo(
                // 位于原点左侧
                offset = Vector3f(0.125f, 0.375f, 0f),
                // 尺寸: 4x12x4 像素 -> 0.25 x 0.75 x 0.25 方块
                scale = Vector3f(0.25f, 0.75f, 0.25f),
                // 旋转锚点在腿的顶部中心（髋关节）
                rotationAnchorPoint = Vector3f(0f, 0.375f, 0f),
                mass = 5f
            ))

            // 4. 右腿 (RightLeg)
            put("RightLeg", LimbInfo(
                // 位于原点右侧
                offset = Vector3f(-0.125f, 0.375f, 0f),
                // 尺寸: 4x12x4 像素 -> 0.25 x 0.75 x 0.25 方块
                scale = Vector3f(0.25f, 0.75f, 0.25f),
                // 旋转锚点在腿的顶部中心（髋关节）
                rotationAnchorPoint = Vector3f(0f, 0.375f, 0f),
                mass = 5f
            ))

            // 5. 左臂 (LeftHand)
            put("LeftHand", LimbInfo(
                // 位于躯干左侧
                offset = Vector3f(0.375f, 1.125f, 0f),
                // 尺寸: 4x12x4 像素 -> 0.25 x 0.75 x 0.25 方块
                scale = Vector3f(0.25f, 0.75f, 0.25f),
                // 旋转锚点在手臂顶部中心（肩关节）
                rotationAnchorPoint = Vector3f(0f, 0.375f, 0f),
                mass = 4f
            ))

            // 6. 右臂 (RightHand)
            put("RightHand", LimbInfo(
                // 位于躯干右侧
                offset = Vector3f(-0.375f, 1.125f, 0f),
                // 尺寸: 4x12x4 像素 -> 0.25 x 0.75 x 0.25 方块
                scale = Vector3f(0.25f, 0.75f, 0.25f),
                // 旋转锚点在手臂顶部中心（肩关节）
                rotationAnchorPoint = Vector3f(0f, 0.375f, 0f),
                mass = 4f
            ))
        }
    }
}