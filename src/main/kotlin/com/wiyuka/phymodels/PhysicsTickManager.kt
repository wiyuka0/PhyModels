package com.wiyuka.phymodels


import com.jme3.bullet.collision.shapes.BoxCollisionShape
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.math.Transform
import com.wiyuka.phymodels.model.ModelManager.getBlockMatrixWithoutPassableBlock
import com.wiyuka.phymodels.physics.objmanager.BlockPhysicsUpdater
import com.wiyuka.phymodels.physics.materialattributes.MaterialMass
import com.wiyuka.phymodels.physics.obj.ModelEntity
import com.wiyuka.phymodels.physics.Physics
import com.wiyuka.phymodels.physics.obj.getGeometricCenter
import com.wiyuka.phymodels.physics.obj.relativeTo
import com.wiyuka.phymodels.physics.scheduler
import com.wiyuka.phymodels.physics.obj.toJmeVector3f
import com.wiyuka.phymodels.physics.obj.toLocation
import com.wiyuka.phymodels.physics.obj.toVecmathQuat4f
import com.wiyuka.phymodels.physics.obj.toVecmathVector3f
import com.wiyuka.phymodels.physics.obj.toVector3f
import com.wiyuka.phymodels.physics.objmanager.ObjectManager
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.joml.Vector3i
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.vecmath.Vector3f
import kotlin.math.abs

class PhysicsTickManager {
    companion object {

        var scriptStarted = false

        val zeroV3f = Vector3f(.0f, .0f, .0f)

        val staticBlocks = ConcurrentHashMap<Vector3i, PhysicsRigidBody>() //地面方块 -> 对应的物理世界内的刚体
        val playerBoxes = ConcurrentHashMap<String, PhysicsRigidBody>() //PlayerPointer -> 对应的物理世界内的刚体

        val tickrate = 20 // 20 ticks per second

        fun setup(plugin: Plugin){


            plugin.logger.info { "Initializing PhyWorld..." }
            plugin.logger.info { "Update thread starting..." }

            val tickTimer = Timer("Physics Tick Timer")

            var tickTimeRate = 1f / tickrate
            var elapsedTime = 0L

            var firstStart = true

            tickTimer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    if (firstStart) {
                        Physics.initializeWorlds()
                        firstStart = false
                    }
                    elapsedTime = tick(tickTimeRate)
                }
            }, 1, (tickTimeRate * 1000).toLong())
            plugin.logger.info { "Update thread started with ${tickTimeRate}s/tick" }
        }

        val simulationThreadPool = Executors.newWorkStealingPool()

        fun submitAsync(func: () -> Unit): Future<*> {
            return simulationThreadPool.submit {
                func()
            }

        }
        fun execute(func: () -> Unit) {
            simulationThreadPool.execute {
                func()
            }
        }


        val PLAYER_BOX_SHAPE = BoxCollisionShape(Vector3f(.3f, .9f, .3f).toJmeVector3f())
        val lastTickPos = mutableMapOf<String, Transform>()
        val lastElapsedTime = 0L
        fun tick(tickTimeRate: Float): Long{
            try {
                for (modelUniqueId in ObjectManager.livingModels.keys) {
                    simulationThreadPool.submit {
                        processSingleModel(modelUniqueId, tickTimeRate + 1f)
                    }
                }
            }catch (e: Exception){
                return lastElapsedTime
            }

            //add player model
            Bukkit.getOnlinePlayers().forEach { player ->
                updatePlayerHitBox(player, 1f/10f)
            }

//            processRigidBodyRequest()

            scriptTick() // 动画Tick
            return phyTick(tickTimeRate)
        }

        private fun processSingleModel(modelUniqueId: String, tickTimeRate: Float) {
            val modelEntity = ObjectManager.livingModels[modelUniqueId] ?: return
            val modelRigidBody = modelEntity.rigidBody

            if(modelRigidBody.activationState != 1) return

            val modelWorldTransform = modelRigidBody.getTransform(null)

            val modelRotation = modelWorldTransform.rotation.toVecmathQuat4f()
            val modelPosition = modelRigidBody.getGeometricCenter(modelRotation)

            val modelLastTransform = lastTickPos[modelEntity.uniqueId]
            val motionThreshold = 0.0001f
            if (modelLastTransform != null) {
                val lastModelPosition = modelLastTransform.translation.toVecmathVector3f()
                val lastModelRotation = modelLastTransform.rotation.toVecmathQuat4f()
                if (
                    abs(modelPosition.x - lastModelPosition.x) < motionThreshold &&
                    abs(modelPosition.y - lastModelPosition.y) < motionThreshold &&
                    abs(modelPosition.z - lastModelPosition.z) < motionThreshold &&
                    abs(modelRotation.x - lastModelRotation.x) < motionThreshold &&
                    abs(modelRotation.y - lastModelRotation.y) < motionThreshold &&
                    abs(modelRotation.z - lastModelRotation.z) < motionThreshold &&
                    abs(modelRotation.w - lastModelRotation.w) < motionThreshold
                ) {
                    return
                }
            }

            lastTickPos[modelEntity.uniqueId] = modelWorldTransform

            modelEntity.updateBlockDisplayLocation(
                modelPosition.toLocation(world = modelEntity.location.world),
                modelRotation,
                tickTimeRate
            )
            modelEntity.calcTranslation(tickTimeRate)

            val extendedSize = 2 //block
            val modelSize = modelEntity.model.getSize()
            modelSize.x += extendedSize
            modelSize.y += extendedSize
            modelSize.z += extendedSize

            var blockMatrixCenter = modelEntity.location
            // Location Prediction
            blockMatrixCenter = offsetBySpeed(modelRigidBody, blockMatrixCenter)
            updateNearbyBlocks(blockMatrixCenter, modelSize)
            updateModelState(modelEntity)
        }


        private fun updateModelState(modelEntity: ModelEntity) {
            for ((gridLoc, blockUnit) in modelEntity.displayBlocks) {
                blockUnit.entity.block = blockUnit.virtualWorldBlock.block.blockData
            }
        }

        private fun offsetBySpeed(
            modelRigidBody: PhysicsRigidBody,
            blockMatrixCenter: Location
        ): Location {
            val modelVel = modelRigidBody.getLinearVelocity(null)
            val predictionThreshold = 5f
            if (
                abs(modelVel.x) > predictionThreshold ||
                abs(modelVel.y) > predictionThreshold ||
                abs(modelVel.z) > predictionThreshold
            ) {
                val predictionTime = 1.0
                blockMatrixCenter.add(
                    predictionTime * modelVel.x,
                    predictionTime * modelVel.y,
                    predictionTime * modelVel.z
                )
            }
            return blockMatrixCenter
        }

        fun updateNearbyBlocks(
            modelEntityCenter: Location,
            modelSize: Vector3i
        ) {
            val blockMatrixSnapshot = getBlockMatrixWithoutPassableBlock(
                modelEntityCenter,
                modelSize
            )
            for ((loc, block) in blockMatrixSnapshot) {
                val vec3i = Vector3i(loc.blockX, loc.blockY, loc.blockZ)
                val vec3iJoml = org.joml.Vector3i(vec3i.x, vec3i.y, vec3i.z)
                val worldName = modelEntityCenter.world.name
                val blockUpdater = Physics.physicsWorlds[worldName]!!.second
                if (block.isPassable) {
                    blockUpdater.removeBlock(vec3iJoml)
                } else {
                    blockUpdater.addBlock(vec3iJoml, block.type)
                }
            }
        }

        private fun scriptTick() {
            if (!scriptStarted) return
            val scriptModels = getAllScriptModels()

            scriptModels.forEach { modelEntity ->
                val scriptData = modelEntity.modelScriptData
                if (scriptData == null) return@forEach

                val currentAnimationIndex = scriptData.currentAnimationIndex
                val currentModelRigidBody = modelEntity.rigidBody
                val currentKeyframe = scriptData.keyframes[currentAnimationIndex]

//                currentModelRigidBody.getWorldTransform(globalWorldTransform)
//                globalWorldTransform.origin.set(currentKeyframe)
//                currentModelRigidBody.setWorldTransform(globalWorldTransform)
//                val currentModelTransform = currentModelRigidBody.getTransform(null)
//                currentModelTransform.translation = currentKeyframe.toJmeVector3f()
//                currentModelRigidBody.setPhysicsTransform(currentModelTransform)
                currentModelRigidBody.scheduler { it.setPhysicsLocation(currentKeyframe.toJmeVector3f()) }

                if (!modelEntity.modelScriptData.backward) {
                    modelEntity.modelScriptData.currentAnimationIndex++
                } else modelEntity.modelScriptData.currentAnimationIndex--
                if (modelEntity.modelScriptData.currentAnimationIndex >= scriptData.keyframes.size) {
                    if (scriptData.repeatable) {
                        scriptData.backward = true
                        modelEntity.modelScriptData.currentAnimationIndex = scriptData.keyframes.size - 1
                    } else {
                        modelEntity.modelScriptData.currentAnimationIndex = 0
                    }
                }
                if (modelEntity.modelScriptData.currentAnimationIndex <= 0) {
                    modelEntity.modelScriptData.backward = false
                    modelEntity.modelScriptData.currentAnimationIndex = 0
                }
            }
        }

        private const val DEFAULT_FIXED_ACCURACY = 1f/60f // 默认60Hz的物理更新频率
        private const val CHANGE_COOLDOWN_TICKS = 10
        private var ticksSinceLastChange = 0

        private val OPTIMIZATION_THRESHOLD = linkedMapOf(
            140L to 1f/10f, // >140ms
            120L to 1f/20f,  // >120ms
            100L to 1f/30f,
            70L  to 1f/50f,
            0L   to 1f/60f  // 默认
        )
        private var dynamicAccuracy = DEFAULT_FIXED_ACCURACY
        private var lastSubStep = DEFAULT_FIXED_ACCURACY
        private val nearly10TimesTake = LinkedList<Long>()

        fun getAvgTimeTake(): Triple<Float, Float, Float> {
            val near10 = nearly10TimesTake.average().toFloat()
            val near5 = nearly10TimesTake.subList(5, 10).average().toFloat()
            val near1 = nearly10TimesTake.last().toFloat()

            return Triple(near10, near5, near1)
        }

        private fun phyTick(tickTimeRate: Float): Long{
            val startTime = System.currentTimeMillis()
            Physics.tick(tickTimeRate, 10, dynamicAccuracy)
            val elapsedTime = System.currentTimeMillis() - startTime
            nearly10TimesTake.add(elapsedTime)
            if (nearly10TimesTake.size > 20) nearly10TimesTake.removeFirst()
            ticksSinceLastChange++
            if (ticksSinceLastChange < CHANGE_COOLDOWN_TICKS) return elapsedTime
            ticksSinceLastChange = 0
            val avgTickTime = nearly10TimesTake.average()
            // 根据耗时动态调整 subStep
            val reduction = OPTIMIZATION_THRESHOLD.entries.firstOrNull { avgTickTime > it.key }?.value ?: dynamicAccuracy
            dynamicAccuracy = reduction
            if (dynamicAccuracy != lastSubStep) {
                PhyModels.logger.info("Dynamic accuracy changed: $lastSubStep -> $dynamicAccuracy")
                lastSubStep = dynamicAccuracy
            }
            return elapsedTime
        }
        fun getAllScriptModels(): List<ModelEntity> {
            return ObjectManager.livingModels.filter { it.value.modelType == PhysAPI.Companion.BodyType.SCRIPTED }.values.toList()
        }

        fun startScriptTick(){
            scriptStarted = true
            getAllScriptModels().forEach { model -> model.modelScriptData?.let { it.startTime = System.currentTimeMillis().toInt() } }
        }


        private val lastPlayerPositions = mutableMapOf<UUID, Vector3f>()

        fun getPlayerBoxKey(player: Player): String = "PlayerBox${player.uniqueId}"
        private fun updatePlayerHitBox(player: Player, tpf: Float) {
            val playerKey = getPlayerBoxKey(player)
            val targetPos = player.getGeometricCenter().toVector3f()
            val rigidBody = playerBoxes[playerKey]

            if (rigidBody == null) {
                addPlayerRigidBody(targetPos.toLocation(world = player.world), playerKey)
                lastPlayerPositions[player.uniqueId] = targetPos
                return
            }

            // 获取上一次位置
            val lastPos = lastPlayerPositions.getOrPut(player.uniqueId) {
                rigidBody.getPhysicsLocation(null).toVecmathVector3f()
            }

            // 插值计算（线性插值）
            val interpolatedPos = Vector3f(
                lerp(lastPos.x, targetPos.x, tpf * 10f),
                lerp(lastPos.y, targetPos.y, tpf * 10f),
                lerp(lastPos.z, targetPos.z, tpf * 10f)
            )

            // 设置物理位置（插值后）
            rigidBody.scheduler {
                rigidBody.setPhysicsLocation(interpolatedPos.toJmeVector3f())
            }
            // 更新缓存
            lastPlayerPositions[player.uniqueId] = interpolatedPos
        }

        private fun lerp(a: Float, b: Float, t: Float): Float {
            return a + (b - a) * t.coerceIn(0f, 1f)
        }

        private fun addPlayerRigidBody(
            playerGeometricCenter: Location,
            playerPointer: String
        ) {
            val position = playerGeometricCenter.toVector3f()


            val body = PhysicsRigidBody(PLAYER_BOX_SHAPE, 0f).apply {
                this.scheduler {
                    this.setPhysicsLocation(position.toJmeVector3f())
                    this.userObject = playerPointer
                    setDamping(Physics.BASE_LINEAR_DAMPING, Physics.BASE_ANGULAR_DAMPING)
                }
            }

            ObjectManager.addObjectToPhysicsWorld(body, playerGeometricCenter.world.name)
            playerBoxes[playerPointer] = body
        }




        private fun buoyancy(modelEntity: ModelEntity, rigidBody: PhysicsRigidBody){
//            val location = modelEntity.location
//            val block = location.block
//            // 检测水方块（包括所有类型的水）
//            if (block.type != Material.WATER) {
//                rigidBody.setDamping(0.1f, 0.2f)
//                return Vector3f()
//            }
//            // 计算浮力（假设质量可用，1.3倍重力形成上浮趋势）
//            val buoyancyForce = modelEntity.model.mass * 9.8f * 1.3f
//            // 应用浮力并增加流体阻力
//            rigidBody.setDamping(0.7f, 0.5f)
//            return Vector3f(0f, buoyancyForce, 0f)

            val totalBlocks = modelEntity.displayBlocks.size
            var inWaterCount = 0
            for ((_, blockUnit) in modelEntity.displayBlocks) {
                val blockEntity = blockUnit.entity
                val blockEntityIsInWater = blockEntity.location.block.type == Material.WATER
                if (!blockEntityIsInWater) continue
                inWaterCount++
                val blockPos = blockEntity.location
                val blockType = blockEntity.block.material
                val density = MaterialMass.density(blockType)
                val densityDiff = MaterialMass.density(Material.WATER) - density
                if (densityDiff <= 0) continue
                val buoyancyForce = densityDiff * 1f
                val force = Vector3f(0f, buoyancyForce, 0f)
                rigidBody.scheduler {
                    rigidBody.applyForce(force.toJmeVector3f(),
                        blockPos.toVector3f().relativeTo(rigidBody.getPhysicsLocation(null).toVecmathVector3f())
                            .toJmeVector3f()
                    )
                }
            }
            val waterRatio = inWaterCount.toFloat() / totalBlocks.toFloat()

            val offsetLinearDamping = 0.7f - 0.1f
            val offsetAngularDamping = 0.5f - 0.2f
            val linearDamping = offsetLinearDamping * waterRatio
            val angularDamping = offsetAngularDamping * waterRatio
            val finalLinear = 0.1f + linearDamping
            val finalAngular = 0.2f + angularDamping
            rigidBody.setDamping(finalLinear, finalAngular)

        }

        fun getAvgTime(): Float {
            return nearly10TimesTake.average().toFloat()
        }
    }
}

private fun Player.getGeometricCenter(): Location {
    return this.location.clone().add(.0, .9, .0)
}

private fun Vector3f.distance(f: Vector3f): Float {
    val x1 = this.x
    val y1 = this.y
    val z1 = this.z
    val x2 = f.x
    val y2 = f.y
    val z2 = f.z

    val dx = x2 - x1
    val dy = y2 - y1
    val dz = z2 - z1
    return kotlin.math.sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()
}