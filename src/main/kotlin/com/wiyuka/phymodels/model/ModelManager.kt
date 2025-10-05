package com.wiyuka.phymodels.model

import com.jme3.bullet.collision.shapes.BoxCollisionShape
import com.jme3.bullet.collision.shapes.CollisionShape
import com.jme3.bullet.collision.shapes.CompoundCollisionShape
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.bullet.objects.PhysicsSoftBody
import com.jme3.bullet.objects.infos.SoftBodyConfig
import com.jme3.bullet.util.NativeSoftBodyUtil
import com.wiyuka.phymodels.PhyModels
import com.wiyuka.phymodels.PhysAPI.Companion.BodyType
import org.joml.Vector3i
import com.wiyuka.phymodels.physics.Physics
import com.wiyuka.phymodels.physics.Physics.Companion.physicsWorlds
import com.wiyuka.phymodels.physics.obj.BlockUnit
import com.wiyuka.phymodels.physics.materialattributes.MaterialMass
import com.wiyuka.phymodels.physics.materialattributes.getFriction
import com.wiyuka.phymodels.physics.obj.ModelEntity
import com.wiyuka.phymodels.physics.scheduler
import com.wiyuka.phymodels.physics.obj.toJmeVector3f
import com.wiyuka.phymodels.physics.obj.toVector3f
import com.wiyuka.phymodels.physics.objmanager.ObjectManager
import com.wiyuka.phymodels.util.LocationUtil
import com.wiyuka.phymodels.util.toToken
import com.wiyuka.phymodels.util.toVector3f
import com.wiyuka.phymodels.virtualworld.VirtualWorld
import org.bukkit.Bukkit
import org.bukkit.entity.BlockDisplay
import javax.vecmath.Vector3f
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.util.RayTraceResult
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import java.util.concurrent.CountDownLatch
import javax.vecmath.Quat4f
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

object ModelManager {

    fun generateModel(
        model: Model,
        location: Location,
        scale: Float,
        modelType: BodyType,
        worldName: String,
        specialTag: String = "",
        modelScriptData: ModelScriptData? = null
    ): ModelEntity {
        val modelDisplayBlocks: HashMap<Vector3i, BlockUnit> = HashMap()

        val modelSize = model.getSize()
        val virtualBaseLocation = VirtualWorld.allocate(model.getSize())

        val blockGrid = placeModelToWorld(model, virtualBaseLocation).mapKeys { it.key.toToken() }

        spawnModelDisplayBlocks(location, model, scale).map {
            val block = blockGrid[it.key.toToken()]
            modelDisplayBlocks.put(it.key, BlockUnit(it.value, block!!.location))
        }

        var modelEntity: ModelEntity? = null

        when (modelType) {
            BodyType.STATICAL -> {
                throw IllegalArgumentException("Body type shouldn't be statical")
            }

            BodyType.SCRIPTED -> {
                modelEntity = getScriptedModel(modelScriptData, model, location, modelDisplayBlocks, scale, worldName)
            }

            BodyType.PHYSICAL -> {
                modelEntity = generatePhysicalModel(model, location, modelDisplayBlocks, scale, worldName, specialTag)
            }
        }

        if (false) throw NullPointerException("Model entity shouldn't be null")


        return modelEntity
    }

    fun placeModelToWorld(model: Model, originLoc: Location): MutableMap<Vector3i, Block> {
        val result = mutableMapOf<Vector3i, Block>()
            for ((offset, blockData) in model.blocks.filter { it.value.material.isSolid }) {
                val offsetLoc = originLoc.clone().add(offset.x.toDouble(), offset.y.toDouble(), offset.z.toDouble())
                offsetLoc.block.type = blockData.material
                offsetLoc.block.blockData = blockData
                result[offset] = offsetLoc.block
            }
            for ((offset, blockData) in model.blocks.filter { !it.value.material.isSolid }) {
                val offsetLoc = originLoc.clone().add(offset.x.toDouble(), offset.y.toDouble(), offset.z.toDouble())
                offsetLoc.block.type = blockData.material
                offsetLoc.block.blockData = blockData
                result[offset] = offsetLoc.block
            }
        return result
    }

    private fun generatePhysicalModel(
        model: Model,
        location: Location,
        modelDisplayBlocks: HashMap<Vector3i, BlockUnit>,
        scale: Float,
        worldName: String,
        specialTag: String
    ): ModelEntity {

        val modelEntity = ModelEntity(
            model,
            location,
            Quat4f(0f, 0f, 0f, 0f),
            modelDisplayBlocks,
            scale = scale,
            fetchingPlayer = null,
            modelType = BodyType.PHYSICAL,
            modelScriptData = null,
            massCenterRel = Vector3f(0f, 0f, 0f),
            rotatedMassCenter = Vector3f(0f, 0f, 0f),
            absoluteMassCenter = Vector3f(0f, 0f, 0f)
        )

        val modelInfo = createModelRigidBody(model, scale, location, modelDisplayBlocks)
        //                    updateNearbyBlocks(model.getGeometricCenter(location, scale), model.getSize())
        val massCenter = modelInfo.second
        val modelSize = model.getSize()
        if (modelSize.x % 2 == 0) massCenter.x += 0.5f
        if (modelSize.y % 2 == 0) massCenter.y += 0.5f
        if (modelSize.z % 2 == 0) massCenter.z += 0.5f
        modelEntity.massCenterRel = massCenter
        ObjectManager.bindRigidBody(modelEntity, modelInfo.first)
        modelEntity.rigidBody.restitution

        modelEntity.rigidBody.linearDamping = Physics.BASE_LINEAR_DAMPING
        modelEntity.rigidBody.angularDamping = Physics.BASE_LINEAR_DAMPING

        modelInfo.first.userObject = "$worldName-model-${modelEntity.uniqueId}-special:$specialTag"
        ObjectManager.addObjectToPhysicsWorld(modelInfo.first, worldName)
        ObjectManager.addModel(modelEntity, modelInfo.first)

        return modelEntity
    }

    private fun getScriptedModel(
        modelScriptData: ModelScriptData?,
        model: Model,
        location: Location,
        modelDisplayBlocks: HashMap<Vector3i, BlockUnit>,
        scale: Float,
        worldName: String
    ): ModelEntity {
        if (modelScriptData == null) throw IllegalArgumentException("ScriptData should not be null")
        val scriptData = modelScriptData
        val modelEntity = ModelEntity(
            model,
            location,
            Quat4f(0f, 0f, 0f, 0f),
            modelDisplayBlocks,
            scale = scale,
            fetchingPlayer = null,
            modelType = BodyType.SCRIPTED,
            modelScriptData = scriptData,
            massCenterRel = Vector3f(0f, 0f, 0f),
            rotatedMassCenter = Vector3f(0f, 0f, 0f),
            absoluteMassCenter = Vector3f(0f, 0f, 0f)
        )
        val modelRigidBody = createModelStaticRigidBody(model, scale, location)
        //                    PhyUtil.addRigidBody(modelRigidBody)
        modelRigidBody.linearDamping = Physics.BASE_LINEAR_DAMPING
        modelRigidBody.angularDamping = Physics.BASE_LINEAR_DAMPING
        modelRigidBody.userObject = "scripted-model-${modelEntity.uniqueId}"
        ObjectManager.addObjectToPhysicsWorld(modelRigidBody, worldName)
        ObjectManager.addModel(modelEntity, modelRigidBody)
        ObjectManager.bindRigidBody(modelEntity, modelRigidBody)

        return modelEntity
    }

    fun spawnModelDisplayBlocks(spawnOrigin: Location, model: Model, scale: Float): HashMap<Vector3i, BlockDisplay> {
        val result = hashMapOf<Vector3i, BlockDisplay>()
        for ((locRel, blockData) in model.blocks) {
            val spawnWorld = spawnOrigin.world
            val locAbs = Location(
                spawnWorld,
                spawnOrigin.x + locRel.x * scale,
                spawnOrigin.y + locRel.y * scale,
                spawnOrigin.z + locRel.z * scale
            )
            val displayBlockEntity = spawnWorld.spawnEntity(locAbs, EntityType.BLOCK_DISPLAY) as BlockDisplay

            displayBlockEntity.block = blockData
            displayBlockEntity.transformation = Transformation(
                org.joml.Vector3f(0f, 0f, 0f),
                AxisAngle4f(),
                org.joml.Vector3f(scale, scale, scale),
                AxisAngle4f()
            )
            result[locRel] = displayBlockEntity
        }
        return result
    }


    /** @return 传入的model在物理世界中的刚体
     *
     */
    /**
     * @return model 顾名思义
     * @return vector3f 新的中心点
     */
    fun makeModel(
        loc1: Location,
        loc2: Location,
        center: Vector3i,
        mass: Float,
        name: String,
        modelWorldPosition: Vector3f
    ): Pair<Model, Vector3f> {
        val blockList = LocationUtil.getBlocksInArea(loc1, loc2)

        val model = makeModelByBlockMatrix(
            blockList,
            center = center,
            mass = mass,
            name = name,
            modelWorldPosition
        )
        return model.first to model.second
    }

    private fun makeModelByBlockMatrix(
        blockList: HashMap<Location, Block>,
        center: Vector3i,
        mass: Float,
        name: String,
        modelWorldPosition: Vector3f
    ): Pair<Model, Vector3f> {
        val blockListVector3i = LocationUtil.getRelBlockList(blockList, center)

        //recalc model center
        val newBlockListVector3i = fixCenter(blockListVector3i, modelWorldPosition)
        val model = Model(newBlockListVector3i.first, mass, name)
        //save model
        Model.staticModels.add(model)


        return model to newBlockListVector3i.second

    }

    /**
     * 重新计算中心点至最接近几何中心的整数点
     */
    private fun fixCenter(
        blockListVector3i: MutableMap<Vector3i, BlockData>,
        modelWorldPosition: Vector3f
    ): Pair<MutableMap<Vector3i, BlockData>, Vector3f> {
        val newBlockListVector3i = mutableMapOf<Vector3i, BlockData>()
        val modelSize = Vector3i(
            blockListVector3i.keys.maxOf { it.x } - blockListVector3i.keys.minOf { it.x } + 1,
            blockListVector3i.keys.maxOf { it.y } - blockListVector3i.keys.minOf { it.y } + 1,
            blockListVector3i.keys.maxOf { it.z } - blockListVector3i.keys.minOf { it.z } + 1
        )
        val modelCenter = Vector3i(
            blockListVector3i.keys.minOf { it.x } + modelSize.x / 2,
            blockListVector3i.keys.minOf { it.y } + modelSize.y / 2,
            blockListVector3i.keys.minOf { it.z } + modelSize.z / 2
        )
        for ((loc, block) in blockListVector3i) {
            loc.sub(modelCenter.x, modelCenter.y, modelCenter.z)
            newBlockListVector3i[loc] = block
        }
        return newBlockListVector3i to Vector3f(
            modelCenter.x + modelWorldPosition.x,
            modelCenter.y + modelWorldPosition.y,
            modelCenter.z + modelWorldPosition.z
        )
    }

    private fun createModelStaticRigidBody(model: Model, scale: Float, spawnLoc: Location): PhysicsRigidBody {
        val modelSize = model.getSize()
        val modelShape = getStaticModelShape(modelSize, scale)  // 返回的是 CollisionShape，例如 BoxCollisionShape

        val physicsLocation = spawnLoc.toVector3f()

        return PhysicsRigidBody(modelShape, 0f).apply {
            this.scheduler {
                setPhysicsLocation(physicsLocation.toJmeVector3f())
            }
        }
    }


    /*
        TODO:
          1. 创建一个最大外接立方体
          2. 评估缩小每个轴所减少的非法位置作为这个轴的价值(v), 缩小后所损失的合法位置数量为w
          3. 寻找+X +Y +Z -X -Y -Z中价值最高的轴，如果价值相等则寻找w最小的轴
          4. 缩小价值最高
            - 如果这个轴已经为1（无法再缩小）缩小价值第二的轴（或继续往下走）
         */
    data class AABB(
        val min: Vector3i,
        val max: Vector3i,
    )
//        private fun findMergedShapes(input: List<Vector3i>): List<AABB>{
//
//        }
//        private fun countIllegalLocations(shape: List<Vector3i>, aabb: AABB): Boolean {
//            for (i in aabb.min.x until aabb.max.x)
//                for (j in aabb.min.y until aabb.max.y) for (k in aabb.min.z until aabb.max.z) {
//                    val vector3i = Vector3i(i, j, k)
//                }
//        }

    private fun createModelRigidBody(
        model: Model,
        scale: Float,
        spawnLoc: Location,
        modelDisplayBlocks: HashMap<Vector3i, BlockUnit>
    ): Pair<PhysicsRigidBody, Vector3f> { // 刚体 -> 质量中心

        //build model shape
        val compoundShape = buildModelShape(scale, modelDisplayBlocks.keys.toSet())

        val fixedSpawnLoc = spawnLoc.clone()//.apply {
//            val modelSize = model.getSize()
//            if (modelSize.x % 2 != 0) this.x += 0.5f
//            if (modelSize.y % 2 != 0) this.y += 0.5f
//            if (modelSize.z % 2 != 0) this.z += 0.5f
//        }

        //re-calc mass center
        val offsetFromOrigin = Vector3f()
        val totalMassCenter = Vector3f()
        var totalMass = 0f

        val frictions = arrayListOf<Float>()

        for ((pos, block) in modelDisplayBlocks) {
            val material = block.entity.block.material
            val density = MaterialMass.density(material)
            val friction = material.getFriction()
            frictions += friction
            val mass = density * scale * scale * scale

            // 假设 pos 是 Vector3f 类型，表示方块相对于原点的位置
            val weightedPos = Vector3f(pos.toVector3f())
            weightedPos.scale(mass)

            totalMassCenter.add(weightedPos)
            totalMass += mass
        }
// 最终计算偏移量
        if (totalMass > 0f) {
            offsetFromOrigin.set(totalMassCenter)
            offsetFromOrigin.scale(1f / totalMass)
        } else offsetFromOrigin.set(0f, 0f, 0f) // 没有质量，默认原点
        val mass = model.mass * totalMass
        val modelRigidBody = PhysicsRigidBody(compoundShape, mass).apply {
//            this.scheduler {
                setPhysicsLocation(fixedSpawnLoc.toJmeVector3f().apply {
//                    if(this.x < 0.0) this.x -= 1f
//                    if(this.y < 0.0) this.y -= 1f
//                    if(this.z < 0.0) this.z -= 1f
                })
                this.friction = frictions.average().toFloat()
                this.restitution = 0.8f
//            }
        }

        val oldLocation = modelRigidBody.getPhysicsLocation(null)
        val fixedLocation = oldLocation.clone().add(-1f, -1f, -1f)
//        modelRigidBody.setPhysicsLocation(fixedLocation)

        return modelRigidBody to offsetFromOrigin
    }

    private fun buildModelShape(
        modelScale: Float,
        modelShapeBlock: Set<Vector3i>
    ): CollisionShape {

        val rectangleSize = getRectangleSize(modelShapeBlock)

        if (rectangleSize != null) {
            val boxCollisionShape = BoxCollisionShape(
                com.jme3.math.Vector3f(
                    rectangleSize.x * modelScale / 2f,
                    rectangleSize.y * modelScale / 2f,
                    rectangleSize.z * modelScale / 2f,
                )
            )
            return boxCollisionShape
        } else {
            val compoundShape = CompoundCollisionShape()
            val halfExtents =
                Vector3f(modelScale / 2f, modelScale / 2f, modelScale / 2f).toJmeVector3f() // 每个小方块的大小
            for (pos in modelShapeBlock) {
                val boxShape = BoxCollisionShape(halfExtents)
                val localTranslation = Vector3f(
                    pos.x * modelScale + halfExtents.x,
                    pos.y * modelScale + halfExtents.y,
                    pos.z * modelScale + halfExtents.z
                ).toJmeVector3f()
                compoundShape.addChildShape(boxShape, localTranslation)
            }
            return compoundShape
        }
    }

    /**
     * 返回矩形locations的大小，如果locations不是一个矩形则返回null
     */
    private fun getRectangleSize(locations: Set<Vector3i>): Vector3i? {
        if (!isRectangle(locations)) return null
        return LocationUtil.getVector3iSize(locations)
    }

    private fun isRectangle(locations: Set<Vector3i>): Boolean {
        val firstLayerMinX = locations.minOf { it.x }
        val firstLayerMinY = locations.minOf { it.y }
        val firstLayerMaxX = locations.maxOf { it.x }
        val firstLayerMaxY = locations.maxOf { it.y }

        val minZ = locations.minOf { it.z }
        val maxZ = locations.maxOf { it.z }

        for (z in maxZ downTo minZ) for (x in firstLayerMaxX downTo firstLayerMinX) for (y in firstLayerMaxY downTo firstLayerMinY) {
            val currentV3i = Vector3i(x, y, z)
            if (currentV3i !in locations) return false
        }
        return true
    }


    private fun getStaticModelShape(
        modelSize: Vector3i,
        scale: Float
    ): BoxCollisionShape {
        val halfExtents = Vector3f(
            modelSize.x * 0.5f * scale,
            modelSize.y * 0.5f * scale,
            modelSize.z * 0.5f * scale
        )
        return BoxCollisionShape(halfExtents.toJmeVector3f())
    }

    fun getBlockMatrix(geometricCenter: Location): MutableMap<Location, Block> {
        val blockMatrix = mutableMapOf<Location, Block>()
        for (i in -5..5) {
            for (j in -5..5) {
                for (k in -5..5) {
                    val currLoc = geometricCenter.clone().add(i.toDouble(), j.toDouble(), k.toDouble())
                    val currBlock = currLoc.block
                    if (
                        !currLoc.clone().add(1.0, 0.0, 0.0).block.isPassable &&
                        !currLoc.clone().add(-1.0, 0.0, 0.0).block.isPassable &&
                        !currLoc.clone().add(0.0, 1.0, 0.0).block.isPassable &&
                        !currLoc.clone().add(0.0, -1.0, 0.0).block.isPassable &&
                        !currLoc.clone().add(0.0, 0.0, 1.0).block.isPassable &&
                        !currLoc.clone().add(0.0, 0.0, -1.0).block.isPassable
                    ) {
                        continue
                    }

                    if (currBlock.isPassable) continue
                    val blockLoc = currBlock.location
                    blockMatrix.put(blockLoc, currBlock)
                }
            }
        }
        return blockMatrix
    }

    fun getBlockMatrixWithoutPassableBlock(geometricCenter: Location, halfSize: Vector3i): MutableMap<Location, Block> {
        val blockMatrix = mutableMapOf<Location, Block>()
        val x = halfSize.x
        val y = halfSize.y
        val z = halfSize.z
        for (i in -x..x) {
            for (j in -y..y) {
                for (k in -z..z) {
                    val currLoc = geometricCenter.clone().add(i.toDouble(), j.toDouble(), k.toDouble())
                    val currBlock = currLoc.block

                    if (
                        !currLoc.clone().add(1.0, 0.0, 0.0).block.isPassable &&
                        !currLoc.clone().add(-1.0, 0.0, 0.0).block.isPassable &&
                        !currLoc.clone().add(0.0, 1.0, 0.0).block.isPassable &&
                        !currLoc.clone().add(0.0, -1.0, 0.0).block.isPassable &&
                        !currLoc.clone().add(0.0, 0.0, 1.0).block.isPassable &&
                        !currLoc.clone().add(0.0, 0.0, -1.0).block.isPassable
                    ) {
                        continue
                    }
                    val blockLoc = currBlock.location
                    blockMatrix.put(blockLoc, currBlock)
                }
            }
        }
        return blockMatrix
    }

    fun removeModel(modelEntity: ModelEntity) {

        val rigidBody = modelEntity.rigidBody
//            PhyUtil.removeRigidBody(rigidBody)
        ObjectManager.removeObjectFromPhysicsWorld(rigidBody, modelEntity.location.world.name)
        ObjectManager.livingModels.remove(modelEntity.uniqueId)
        for ((_, entity) in modelEntity.displayBlocks) entity.entity.remove()
    }
}

private fun Vector3f.scale(f: Float) {
    this.x *= f
    this.y *= f
    this.z *= f
}

fun Player.getTargetEntity(
    maxDistance: Double = 100.0,
    raySize: Double = 0.1,
): Entity? {
    val eyeLocation = eyeLocation
    val direction = eyeLocation.direction

    val result: RayTraceResult? = world.rayTraceBlocks(eyeLocation, direction, raySize)

    return result?.hitEntity
}

fun Player.calculateTargetLocation(distance: Double): Location {
    return eyeLocation.clone().apply {
        val direction = direction.multiply(distance)
        add(direction)
    }
}
