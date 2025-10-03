package com.wiyuka.phymodels.physics.liquid

import com.jme3.bullet.PhysicsTickListener
import com.jme3.bullet.collision.shapes.SphereCollisionShape
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.math.Vector3f
import com.wiyuka.phymodels.PhysicsTickManager
import com.wiyuka.phymodels.PhysicsTickManager.Companion.updateNearbyBlocks
import com.wiyuka.phymodels.model.Model
import com.wiyuka.phymodels.physics.obj.toLocation
import com.wiyuka.phymodels.physics.obj.toVecmathVector3f
import com.wiyuka.phymodels.physics.objmanager.ObjectManager
import com.wiyuka.phymodels.physics.scheduler
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.joml.Vector3i
import java.util.concurrent.ConcurrentHashMap

class LiquidManager {
    companion object{
        val liquidRigidBody = ConcurrentHashMap<Int, Pair<LiquidType, PhysicsRigidBody>>()
        fun clear() {
            for ((id, rb) in liquidRigidBody) {
                PhysicsTickManager.execute {
//                    rb.second.
                }
            }
        }
        private fun generateLiquidSphere(location: Vector3f, liquidType: LiquidType, radius: Float = LiquidConfig.resolution, worldName: String){
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
            ObjectManager.addObjectToPhysicsWorld(rigidBody, worldName)
        }
        fun createLiquid(location: Vector3f, liquidType: LiquidType, counts: Int, worldName: String){ for (i in 0 until counts) generateLiquidSphere(location, liquidType, 0.1f, worldName) }
        fun liquidTick(worldName: String){
            for ((_, bodyInfo) in liquidRigidBody) {
                PhysicsTickManager.execute {
                    val body = bodyInfo.second
                    val type = bodyInfo.first
                    val surfaceTension = type.config.surfaceTension
                    val neighbors = getNeighborLiquidBody(body.getPhysicsLocation(null), 1.5f)
//                    for ((_, neighbor) in neighbors) {
//                        val neighborLoc = neighbor.getPhysicsLocation(null)
//                        val force = neighborLoc.subtract(body.getPhysicsLocation(null)).mult(surfaceTension * -1f)
//                        neighbor.scheduler { neighbor.applyCentralForce(force) }
//                    }

                    val xList = mutableListOf<Float>()
                    val yList = mutableListOf<Float>()
                    val zList = mutableListOf<Float>()
                    neighbors.forEach { neighbor ->
                        xList += neighbor.value.getPhysicsLocation(null).x
                        yList += neighbor.value.getPhysicsLocation(null).y
                        zList += neighbor.value.getPhysicsLocation(null).z
                    }
                    val centerPos = Vector3f(xList.average().toFloat(), yList.average().toFloat(), zList.average().toFloat())
                    val loc = body.getPhysicsLocation(null)

                    val force = Vector3f(centerPos).subtract(loc).mult(surfaceTension)

                    body.scheduler {
                        it.applyCentralForce(force)
                    }

                    val color = type.config.color
                    val dustOptions = DustOptions(color, 1.0f) // 1.0F 为粒子大小
                    val world = Bukkit.getWorld(worldName)!!
                    val bukkitLoc = loc.toVecmathVector3f().toLocation(world)
                world.spawnParticle(Particle.DUST, bukkitLoc, 4, 0.1, 0.1, 0.1, dustOptions)
                    updateNearbyBlocks(bukkitLoc, Vector3i(2, 2, 2))
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