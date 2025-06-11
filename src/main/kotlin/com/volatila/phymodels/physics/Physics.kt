package com.volatila.phymodels.physics

import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.objects.PhysicsRigidBody
import com.volatila.phymodels.PhysicsTickManager
import com.volatila.phymodels.PhysicsTickManager.Companion.staticBlocks
import com.volatila.phymodels.droppeditem.DroppedItemManager
import com.volatila.phymodels.model.Model.Vector3i
import com.volatila.phymodels.physics.bulletextended.ControllablePhysicsSpace
import com.volatila.phymodels.physics.forceexpansion.ForceAppendManager
import com.volatila.phymodels.physics.liquid.LiquidManager
import com.volatila.phymodels.physics.listener.collisionlistener.CollisionListener
import com.volatila.phymodels.physics.obj.toJmeVector3f
import com.volatila.phymodels.physics.objmanager.BlockPhysicsUpdater
import com.volatila.phymodels.physics.objmanager.ObjectManager
import com.volatila.phymodels.physics.objmanager.TaskProcessor
import com.volatila.phymodels.physics.listener.ticklistener.DebugListener
import com.volatila.phymodels.physics.listener.ticklistener.DistanceOptimization
import com.volatila.phymodels.tools.Crawl
import org.bukkit.block.Block
import java.lang.reflect.Method
import javax.vecmath.Vector3f
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.math.abs

class Physics {
    companion object{
        const val GRAVITY = -9.81f // 重力加速度，单位 m/s²
        const val BASE_LINEAR_DAMPING = 0.3f // 基础线性阻尼
        const val BASE_ANGULAR_DAMPING = 0.4f // 基础角阻尼
        // uuid -> model
        var physicsWorld: ControllablePhysicsSpace = getWorld(Vector3f(0f, -9.81f, 0f))


        private fun getWorld(gravity: Vector3f): ControllablePhysicsSpace {
            val world = ControllablePhysicsSpace()
            world.setGravity(com.jme3.math.Vector3f(0f, GRAVITY, 0f))
            world.setMaxSubSteps(10)
            world.setMaxTimeStep(1f/20f)

            initTickListeners(world)

            ForceAppendManager.initAppendForces()
            CollisionListener.initListeners(world)

            return world
        }

        private fun initTickListeners(world: PhysicsSpace) {

            world.addTickListener(DistanceOptimization(48F, 10))
            world.addTickListener(Crawl.TickListener())
            world.addTickListener(DebugListener())
        }
        data class DragConfig(
            val speedMultiple: Float = 1.2f,
            val density: Int = 16,
            val waterDrag: Float = 0.1f,
            val airDrag: Float = 0.02f,
        )

        /*TODO:
                1. 把所有与Bullet交互的代码都放到这里 (已经完成)
                2. 实现流体阻力
                    - 阻力计算
                        1. 从模型几何中心出发，向速度方向延长(velocity*n + 中心点从速度方向出发到模型边界的距离)，另一个端点作为点A.
                        2. 以点A为中心向模型发射射线，
                            - 如果射线与模型相交，则取交点作为点B，在点B施加一个力，方向为射线的反射方向，大小为阻力系数 * 当前速度
                            - 如果或射线与模型不相交，则忽略这条射线
                        3. (如果性能不足) 计算所有射线所施加的力的合力，施加在模型中心点
                            - 代价是损失真实感
            */

        // 计算所有模型的阻力
        fun tick(tickRate: Float, maxSubStep: Int, accuracy: Float) {
            TaskProcessor.handleRequest(this.physicsWorld)
            if(physicsWorld.accuracy != accuracy) physicsWorld.accuracy = accuracy
            physicsWorld.update(tickRate, maxSubStep)
//            for ((_, body) in modelBodies) simulationThreadPool.submit { calcModelDrag(body) }
            BlockPhysicsUpdater.updateAllChunks()
            LiquidManager.liquidTick()
            DroppedItemManager.applyDroppedItemTick()

            applyFixedGravity()
            applyAppendForces()
            applyBodyShapeUpdate()

            checkBodyState()
        }

        private fun applyAppendForces() {
            for ((_, modelEntity) in ObjectManager.livingModels) {
                val rigidBody = modelEntity.rigidBody
                if(rigidBody.activationState == 1)
                    ForceAppendManager.applyForce(rigidBody)
            }
        }

        private fun checkBodyState(){
            val needRebuild = ObjectManager.livingModels.map { it.value.rigidBody }.filter { it.activationState == 5 } // 5 == DISABLE_SIMULATION，即物体无法正常模拟
            for (it in needRebuild) it.setActivationState(1) // 1 == ACTIVE_TAG

            val needFixGravity = ObjectManager.livingModels.map { it.value.rigidBody }.filter { it.getGravity(null).y != GRAVITY}
            for (it in needFixGravity) it.setGravity(com.jme3.math.Vector3f(0f, GRAVITY, 0f))
        }

        private fun applyBodyShapeUpdate(){
            val needToUpdate = ObjectManager.livingModels.filter { it.value.needCheckShape }

            needToUpdate.forEach {

            }
        }


        fun removeStaticBlock(block: Block) {
            val blockLoc = block.location
            val x = blockLoc.x.toInt()
            val y = blockLoc.y.toInt()
            val z = blockLoc.z.toInt()
            val vector3i = Vector3i(x, y, z)

            for ((index, rigidBody) in staticBlocks) {
                if(!(index.x == vector3i.x && index.y == vector3i.y && index.z == vector3i.z)) continue
//                Physics.removeObject(rigidBody)
//                staticBlocks.remove(index)
                BlockPhysicsUpdater.removeBlock(org.joml.Vector3i(vector3i.x, vector3i.y, vector3i.z))
            }

            //Active blocks
            ObjectManager.livingModels.values.forEach {
                val rigidBody = it.rigidBody
                val position = rigidBody.getPhysicsLocation(null)
                val distance = position.distance(vector3i.toVector3f().toJmeVector3f())
                if(distance <= 3){
                    rigidBody.activate()
                }
            }
        }

        private fun applyFixedGravity() {
            PhysicsTickManager.execute {
                for ((_, modelEntity) in ObjectManager.livingModels) {
                    modelEntity.absoluteMassCenter
                    val rigidBody = modelEntity.rigidBody
                    val rbVel = rigidBody.getLinearVelocity(null)
                    val threshold = 0.001f
                    if (
                        abs(rbVel.x) >= threshold ||
                        abs(rbVel.y) >= threshold ||
                        abs(rbVel.z) >= threshold
                    ) rigidBody.scheduler {
                        it.applyAcceleration(
                            com.jme3.math.Vector3f(0f, -1f, 0f),
                            modelEntity.massCenterRel.toJmeVector3f()
                        )
                    }
                }
            }
        }

        /**
         * 射线检测，返回第一个碰撞的刚体和碰撞点
         */
        fun raycastLocalHitPoint(source: com.jme3.math.Vector3f, direction: com.jme3.math.Vector3f): Pair<PhysicsRigidBody, com.jme3.math.Vector3f>? {
            val rayResult = physicsWorld.rayTest(source, direction)
            if (rayResult == null) return null
            val hitResult = rayResult.firstOrNull()
            if (hitResult == null) return null
            val hitBody = hitResult.collisionObject as? PhysicsRigidBody ?: return null
            val hitFraction = hitResult.hitFraction
            direction.normalize()
            val hitPoint = source.add(direction.mult(hitFraction))
            return Pair(hitBody, hitPoint)
        }
        fun raycastLocalHitPointIgnorePlayer(source: com.jme3.math.Vector3f, direction: com.jme3.math.Vector3f): Pair<PhysicsRigidBody, com.jme3.math.Vector3f>? {
            val rayResult = physicsWorld.rayTest(source, direction)
            if (rayResult == null) return null
            val hitResult = rayResult.find { !(it.collisionObject.userObject as String).startsWith("PlayerBox") }
            if (hitResult == null) return null
            val hitBody = hitResult.collisionObject as? PhysicsRigidBody ?: return null
            val hitFraction = hitResult.hitFraction
            direction.normalize()
            val hitPoint = source.add(direction.mult(hitFraction))
            return Pair(hitBody, hitPoint)
        }

    }
}

fun PhysicsSpace.scheduler(func: () -> Unit){

}

fun PhysicsRigidBody.applyAcceleration(acceleration: com.jme3.math.Vector3f, offset: com.jme3.math.Vector3f){
    this.applyForce(acceleration.mult(this.mass), offset)
}

fun PhysicsRigidBody.applyCentralAcceleration(acceleration: com.jme3.math.Vector3f){
    this.applyCentralForce(acceleration.mult(this.mass))
}

/* Async Modification Fix */

fun PhysicsRigidBody.scheduler(func: (body: PhysicsRigidBody) -> Unit){
    TaskProcessor.tasks.add(this to func)
//    func.invoke(this)
}

/**
 * 设置刚体的激活状态
 * @param activationState 激活状态
 * 1 -> ACTIVE_TAG
 * 2 -> ISLAND_SLEEPING
 * 3 -> WANTS_DEACTIVATION
 * 4 -> DISABLE_DEACTIVATION
 * 5 -> DISABLE_SIMULATION
 */
fun PhysicsRigidBody.setActivationState(activationState: Int) {
    try {
        val clazz = Class.forName("com.jme3.bullet.collision.PhysicsCollisionObject")
        val method: Method = clazz.getDeclaredMethod("setActivationState", Long::class.javaPrimitiveType, Int::class.javaPrimitiveType)
        method.isAccessible = true

        // 调用静态方法（nativeId 是本体）
        method.invoke(null, this.nativeId(), activationState)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}