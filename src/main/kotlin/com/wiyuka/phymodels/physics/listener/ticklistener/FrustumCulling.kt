package com.wiyuka.phymodels.physics.listener.ticklistener

import com.destroystokyo.paper.ClientOption
import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.PhysicsTickListener
import com.wiyuka.phymodels.physics.objmanager.ObjectManager
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.cos


/* FrustumCulling by pama1234 2025/10/4 */

/**
 * 每玩家视锥剔除：隐藏/显示 BlockDisplay/ItemDisplay 包（仍在服务端物理世界中模拟）。
 * - 使用 Player#hideEntity / showEntity 过滤发包
 * - 每 PERIOD_TICKS 跑一次
 * - 通过更宽 FOV 与内外阈值（IN/OUT）做滞后，降低边界闪烁；第三人称相机未知，因此放宽角度
 * - Folia 安全：优先全局 Region Scheduler，失败则退回常规 Scheduler
 */
class FrustumCulling (val plugin: Plugin): PhysicsTickListener{

    // 进入/退出半 FOV（度）；IN 严一点，OUT 宽一点，避免抖动
    private val FOV_HALF_DEG_IN = 110.0
    private val FOV_HALF_DEG_OUT = 120.0
    private val cosHalfFovIn = cos(Math.toRadians(FOV_HALF_DEG_IN))
    private val cosHalfFovOut = cos(Math.toRadians(FOV_HALF_DEG_OUT))

    // 视距（方块）。OUT 比 IN 稍大，用于滞后
    private fun viewDistanceBlocks(): Double {
        val server = Bukkit.getServer()
        val vd = try {
            server.viewDistance
        } catch (_: Throwable) {
            10
        }
        return (vd * 16 + 32).toDouble()
    }

    private fun viewDistanceBlocksOut(): Double = viewDistanceBlocks() + 24.0

    // 记录：每个玩家当前被隐藏的 modelId 集合
    private val hiddenByPlayer: MutableMap<UUID, MutableSet<String>> = ConcurrentHashMap()
    override fun prePhysicsTick(p0: PhysicsSpace?, p1: Float) {}

    override fun physicsTick(p0: PhysicsSpace?, p1: Float) {
        val models = ObjectManager.livingModels.values.toList()
        if (models.isEmpty()) return

        for (player in Bukkit.getOnlinePlayers()) {
            val hidden = hiddenByPlayer.computeIfAbsent(player.uniqueId) { ConcurrentHashMap.newKeySet() }
            val eye = player.eyeLocation
            val dir = eye.direction.normalize()

            val inDist = viewDistanceBlocks()
            val outDist = viewDistanceBlocksOut()

            for (model in models) {
                val world = model.location.world ?: continue
                if (world != player.world) {
                    if (hidden.remove(model.uniqueId)) showAll(player, model.displayBlocks.values.map { it.entity })
                    continue
                }

                // 使用模型质量中心作为近似包围球中心
                val center = Location(
                    world,
                    model.absoluteMassCenter.x.toDouble(),
                    model.absoluteMassCenter.y.toDouble(),
                    model.absoluteMassCenter.z.toDouble()
                )

                val delta = center.toVector().subtract(eye.toVector())
                val distance = delta.length()

                val wasHidden = hidden.contains(model.uniqueId)
                val distanceOutside = if (wasHidden) distance > outDist else distance > inDist
                var shouldHide = distanceOutside

                if (!shouldHide) {
                    // 角度用 cos 比较：cos 大表示更“正对”
                    val cosAngle = dir.dot(delta.clone().normalize())
                    shouldHide = if (wasHidden) {
                        // 仍隐藏，直到明显进入更窄的视锥
                        cosAngle < cosHalfFovIn
                    } else {
                        // 仅当明显离开更宽的视锥才隐藏
                        cosAngle < cosHalfFovOut
                    }
                }

                val entities: List<Entity> = model.displayBlocks.values.map { it.entity }

                if (shouldHide) {
                    if (!wasHidden) {
                        hidden.add(model.uniqueId)
                        hideAll(player, entities)
                    }
                } else {
                    if (wasHidden) {
                        hidden.remove(model.uniqueId)
                        showAll(player, entities)
                    }
                }
            }
        }
    }

    private fun hideAll(player: Player, entities: List<Entity>) {
        entities.forEach { e ->
            try {
                player.hideEntity(plugin, e)
            } catch (_: Throwable) {
            }
        }
    }

    private fun showAll(player: Player, entities: List<Entity>) {
        entities.forEach { e ->
            try {
                player.showEntity(plugin, e)
            } catch (_: Throwable) {
            }
        }
    }
}