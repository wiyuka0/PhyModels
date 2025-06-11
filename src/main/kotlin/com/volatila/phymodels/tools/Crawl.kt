package com.volatila.phymodels.tools

import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.PhysicsTickListener
import com.jme3.bullet.objects.PhysicsRigidBody
import com.volatila.phymodels.debug.DebugUtil.Companion.drawParticle
import com.volatila.phymodels.physics.Physics
import com.volatila.phymodels.physics.scheduler
import com.volatila.phymodels.physics.obj.toJmeVector3f
import com.volatila.phymodels.physics.obj.toLocation
import com.volatila.phymodels.physics.obj.toVecmath
import com.volatila.phymodels.physics.obj.toVecmathVector3f
import com.volatila.phymodels.physics.obj.toVector3f
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import java.util.concurrent.ConcurrentHashMap
import javax.vecmath.Matrix4f
import javax.vecmath.Quat4f
import javax.vecmath.Vector3f

class Crawl: Listener {
    companion object{
        private data class PlayerState(
            val isCrawling: Boolean,
            val crawlDirection: Int,
            val crawlRigidBody: PhysicsRigidBody,
            val pointRel: Vector3f
        )
        private val playerStates = ConcurrentHashMap<Player, PlayerState>()
    }
    @EventHandler
    fun onUse(e: PlayerInteractEvent){
        val player = e.player
        if(player.inventory.itemInMainHand.type != Material.SLIME_BALL) return
        val playerPosition = player.eyeLocation
        val playerDirection = playerPosition.direction
        val rayTraceResult =
            Physics.raycastLocalHitPointIgnorePlayer(playerPosition.toVector3f().toJmeVector3f(), playerDirection.toVector3f().toVecmath().toJmeVector3f())
        if(rayTraceResult == null) return
        e.player.sendMessage("Fetching crawl point...")
        val playerPointRigidBody = rayTraceResult.first
        val pointPosition = rayTraceResult.second
        val playerState = PlayerState(
            isCrawling = true,
            crawlDirection = 0,
            crawlRigidBody = playerPointRigidBody,
            pointRel = (pointPosition.subtract(playerPosition.toVector3f().toJmeVector3f())).toVecmathVector3f()
        )

        drawParticle(Color.PURPLE, pointPosition.toVecmathVector3f().toLocation(e.player.world))
        playerStates[player] = playerState
    }


    @EventHandler
    fun onSwitchItem(e: PlayerInventorySlotChangeEvent){
        val player = e.player
        if(playerStates.containsKey(player)){
            playerStates.remove(player)
            player.sendMessage("Crawl mode deactivated.")
            return
        }
    }

    class TickListener: PhysicsTickListener {
//        override fun prePhysicsTick(p0: PhysicsSpace?, p1: Float) {
//            playerStates.forEach { (player, playerState) ->
//                val rigidBody = playerState.crawlRigidBody
//                val worldPoint = playerState.pointRel
//
//                // 1. 玩家视线前方 2.5 格的目标点（世界坐标）
//                val target = player.eyeLocation.add(player.location.direction.clone().normalize().multiply(2.5)).toVector3f()
//
//                // 2. 力的方向 = 目标点 - 当前刚体抓取点
//                val forceDir = Vector3f(
//                    target.x - worldPoint.x,
//                    target.y - worldPoint.y,
//                    target.z - worldPoint.z
//                )
//
//                // 3. 设置一个力系数（例如：50.0f），防止太小或过强
//                forceDir.scale(5f) // 可以调节这个值控制“拖动强度”
//
//                // 4. 对刚体施加力，让其朝向目标点移动（作用点是抓取点）
//                rigidBody.applyForce(forceDir.toJmeVector3f(), worldPoint.toJmeVector3f())
//            }
//        }

        override fun prePhysicsTick(p0: PhysicsSpace?, p1: Float) {
            val k = 1000f // 弹性系数
            val d = 5f   // 阻尼系数

            playerStates.forEach { (player, playerState) ->
                val rigidBody = playerState.crawlRigidBody

                // 1. 玩家视线前 2.5 格的目标点
                val target = player.eyeLocation
                    .add(player.location.direction.clone().normalize().multiply(2.5))
                    .toVector3f().toJmeVector3f()

                // 2. 计算刚体抓取点在世界坐标中的位置
                val bodyPos = rigidBody.getPhysicsLocation(null)
                val bodyRot = rigidBody.getPhysicsRotation(null)
                val rotation = Quat4f(bodyRot.x, bodyRot.y, bodyRot.z, bodyRot.w)
                val relPoint = Vector3f(playerState.pointRel) // 本地点的拷贝，避免直接修改原始

                val rotationMatrix = Matrix4f()
                rotationMatrix.set(rotation)
                rotationMatrix.transform(relPoint) // 把相对点旋转成世界空间

                val worldGrabPoint = bodyPos.add(relPoint.toJmeVector3f())

                // 3. displacement = 目标点 - 当前点
                val displacement = target.subtract(worldGrabPoint)
                val springForce = com.jme3.math.Vector3f(displacement)
                springForce.mult(k)

                // 4. velocity at grab point = v + ω × r
                val linearVel = rigidBody.getLinearVelocity(null)
                val angularVel = rigidBody.getAngularVelocity(null)

                val relativeVel = com.jme3.math.Vector3f()
                val r = relPoint.toJmeVector3f() // 本地旋转后的向量 r
                val angularComponent = com.jme3.math.Vector3f()
                angularComponent.cross(angularVel, r) // ω × r
                relativeVel.add(linearVel)
                relativeVel.add(angularComponent)

                // 5. 阻尼力 = -d * velocity
                val dampingForce = com.jme3.math.Vector3f(relativeVel)
                dampingForce.mult(-d)

                // 6. 合力 = 弹力 + 阻尼力
                springForce.add(dampingForce)

                // 7. 应用合力
                rigidBody.scheduler {
                    rigidBody.applyForce(springForce, relPoint.toJmeVector3f())
//                rigidBody.applyCentralForce(springForce)
                }
            }

        }

        override fun physicsTick(p0: PhysicsSpace?, p1: Float) {
        }
    }


}