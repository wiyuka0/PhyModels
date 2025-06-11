package com.volatila.phymodels.physics.liquid

import com.jme3.bullet.collision.shapes.SphereCollisionShape
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.math.Vector3f
import com.volatila.phymodels.PhysicsTickManager
import com.volatila.phymodels.PhysicsTickManager.Companion.updateNearbyBlocks
import com.volatila.phymodels.model.Model
import com.volatila.phymodels.physics.obj.toLocation
import com.volatila.phymodels.physics.obj.toVecmathVector3f
import com.volatila.phymodels.physics.objmanager.ObjectManager
import com.volatila.phymodels.physics.scheduler
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import java.util.concurrent.ConcurrentHashMap

class LiquidManager {
    companion object{
        val liquidRigidBody = ConcurrentHashMap<Int, Pair<LiquidType, PhysicsRigidBody>>()
        private fun generateLiquidSphere(location: Vector3f, liquidType: LiquidType, radius: Float = LiquidConfig.resolution){
            val shape = SphereCollisionShape(radius)
            val rigidBody = PhysicsRigidBody(shape)

            rigidBody.mass = liquidType.config.mass
            rigidBody.friction = liquidType.config.sticky
            rigidBody.linearDamping = liquidType.config.sticky
            rigidBody.angularDamping = liquidType.config.sticky

            rigidBody.restitution = 0.5f
            rigidBody.userObject = "LiquidSphere-${liquidType}"

            rigidBody.setPhysicsLocation(location)

            liquidRigidBody.put(kotlin.random.Random.nextInt(Int.MIN_VALUE, Int.MAX_VALUE), liquidType to rigidBody)
            ObjectManager.addObjectToPhysicsWorld(rigidBody)
        }
        fun createLiquid(location: Vector3f, liquidType: LiquidType, counts: Int){ for (i in 0 until counts) generateLiquidSphere(location, liquidType, 0.1f) }
        fun liquidTick(){
            for ((_, bodyInfo) in liquidRigidBody) {
                PhysicsTickManager.execute {
                    val body = bodyInfo.second
                    val type = bodyInfo.first
                    val surfaceTension = type.config.surfaceTension
                    val neighbors = getNeighborLiquidBody(body.getPhysicsLocation(null), 1.5f)
                    for ((_, neighbor) in neighbors) {
                        val neighborLoc = neighbor.getPhysicsLocation(null)
                        val force = neighborLoc.subtract(body.getPhysicsLocation(null)).mult(surfaceTension * -1f)
                        neighbor.scheduler { neighbor.applyCentralForce(force) }
                    }
                    val loc = body.getPhysicsLocation(null)

                    val color = type.config.color
                    val dustOptions = DustOptions(color, 1.0f) // 1.0F 为粒子大小
                    val world = Bukkit.getWorlds()[0]
                    val bukkitLoc = loc.toVecmathVector3f().toLocation(world)
                world.spawnParticle(Particle.DUST, bukkitLoc, 4, 0.1, 0.1, 0.1, dustOptions)
                    updateNearbyBlocks(bukkitLoc, Model.Vector3i(2, 2, 2))
                }
            }
        }
        /**
         * 优化思路：
         *   1. 将RigidBody的坐标作为地址放在一块连续的内存区域里，作为地址的格式是X(第3位数)Y(第3位数)Z(第3位数)X(第二位数)Y(第二位数)Z(第二位数)....Z(第一位数)，
         *   2. 调用NeighborLiquidBody时只查找该刚体转换成内存地址后附近的内存的刚体
         */
        fun getNeighborLiquidBody(location: Vector3f, threshold: Float): Map<Float, PhysicsRigidBody>{ //distance -> body
            val result = HashMap<Float, PhysicsRigidBody>()
            for ((_, bodyInfo) in liquidRigidBody) {
                val body = bodyInfo.second
                val otherBodyLoc = body.getPhysicsLocation(null)
                val distance = otherBodyLoc.distance(location)
                if(distance < threshold) result[distance] = body
            }
            return result
        }
    }
}