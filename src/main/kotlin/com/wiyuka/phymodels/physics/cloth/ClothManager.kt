package com.wiyuka.phymodels.physics.cloth

import com.jme3.bullet.collision.shapes.BoxCollisionShape
import com.jme3.bullet.joints.Point2PointJoint
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.math.Vector3f
import com.wiyuka.phymodels.PhyModels
import com.wiyuka.phymodels.PhysicsTickManager
import com.wiyuka.phymodels.PhysicsTickManager.Companion.updateNearbyBlocks
import com.wiyuka.phymodels.physics.Physics
import com.wiyuka.phymodels.physics.obj.toJmeVector3f
import com.wiyuka.phymodels.physics.obj.toVecmath
import com.wiyuka.phymodels.physics.obj.toVector3f
import com.wiyuka.phymodels.physics.objmanager.ObjectManager
import com.wiyuka.phymodels.physics.objmanager.TaskProcessor
import com.wiyuka.phymodels.util.fromTokenV3i
import com.wiyuka.phymodels.util.toToken
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.EntityType
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3i
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch

object ClothManager {
    val clothes: ConcurrentLinkedQueue<Pair<String, Cloth>> = ConcurrentLinkedQueue()

    fun clear() {
        for ((worldName, cloth) in clothes) {
            cloth.rigidBodies.map { ObjectManager.removeObjectFromPhysicsWorld(it.value, worldName) }
            cloth.displayBlocks.map { it.value.remove() }
        }
    }
    enum class JointMode {
        NO_JOINT,
        FOUR_CORNERS,
        TWO_CORNERS,
    }
    fun addCloth(baseLocation: Location, sideLength: Float, width: Int, length: Int, height: Float, totalMass: Float, jointMode: JointMode = JointMode.FOUR_CORNERS){
        PhysicsTickManager.execute {
            val points = mutableMapOf<String, Vector3f>()
            val baseVector = baseLocation.toJmeVector3f()

            val blockShape = BoxCollisionShape(sideLength * 0.5f, height * 0.5f, sideLength * 0.5f)

            points.put(Vector3i(0, 0, 0).toToken(), baseVector)

            for (x in 1..width) {
                for (z in 1..length) {
                    val node = Vector3f(baseVector).add(x * sideLength, 0f, z * sideLength)
                    val gridLoc = Vector3i(x, z, 0).toToken()
                    points.put(gridLoc, node)
                }
            }

            val singleBoxMass = totalMass / (width * length).toFloat()

            val cloth = Cloth(
                width,
                length,
                height,
                totalMass,
                rigidBodies = mutableMapOf(),
                displayBlocks = mutableMapOf()
            )
            val world = baseLocation.world

            val blockDisplays = mutableMapOf<String, BlockDisplay>()

            val countDownLatch = CountDownLatch(points.size)
            for ((gridLoc, blockCenterPos) in points) {
                Bukkit.getGlobalRegionScheduler().execute(PhyModels.plugin) {
                    val blockDisplay = world.spawnEntity(
                        Location(
                            world,
                            blockCenterPos.x.toDouble(),
                            blockCenterPos.y.toDouble(),
                            blockCenterPos.z.toDouble()
                        ), EntityType.BLOCK_DISPLAY
                    ) as BlockDisplay
                    blockDisplays[gridLoc] = blockDisplay
                    countDownLatch.countDown()
                }
            }
            countDownLatch.await()

            for ((gridLoc, blockCenterPos) in points) {
                val rigidBody = PhysicsRigidBody(blockShape, singleBoxMass)
//
//                if(jointMode == JointMode.FOUR_CORNERS) {
//                    val gridLocVec = fromTokenV3i(gridLoc)
//                    if(gridLocVec.x == length && gridLocVec.z == length) rigidBody.mass = 0fq
//                    if(gridLocVec.x == 0 && gridLocVec.z == length) rigidBody.mass = 0f
//                    if(gridLocVec.x == length && gridLocVec.z == 0) rigidBody.mass = 0f
//                    if(gridLocVec.x == 0 && gridLocVec.z == 0) rigidBody.mass = 0f
//                }

                rigidBody.setPhysicsLocation(blockCenterPos)

                var blockDisplay = blockDisplays[gridLoc]!!

                blockDisplay.teleportDuration = 2
                blockDisplay.block = Bukkit.createBlockData(Material.WHITE_CONCRETE)

                cloth.rigidBodies[gridLoc] = rigidBody
//            Physics.physicsWorlds[world.name]!!.first.add(rigidBody)
                cloth.displayBlocks[gridLoc] = blockDisplay
                ObjectManager.addObjectToPhysicsWorld(rigidBody, world.name)
                val oldTransformation = blockDisplay.transformation
                val newTransformation = Transformation(
                    oldTransformation.translation,
                    oldTransformation.leftRotation,
                    org.joml.Vector3f(sideLength, height, sideLength).mul(1.1f),
                    oldTransformation.rightRotation
                )
                blockDisplay.transformation = newTransformation
            }

//        TaskProcessor.handleRequestInstant()

            for (x in 0..width) {
                for (z in 0..length) {
                    val currentKey = Vector3i(x, z, 0).toToken()
                    val currentBody = cloth.rigidBodies[currentKey] ?: continue

                    // 与右边相邻的点约束
                    if (x < width) {
                        val rightKey = Vector3i(x + 1, z, 0).toToken()
                        val rightBody = cloth.rigidBodies[rightKey]
                        if (rightBody != null) {
//                        if(!(currentBody.isInWorld)) Physics.physicsWorlds[world.name]!!.first.add(currentBody)
//                        if(!(rightBody.isInWorld)) Physics.physicsWorlds[world.name]!!.first.add(rightBody)
                            val joint = Point2PointJoint(
                                currentBody, rightBody,
                                Vector3f(sideLength * 0.5f, 0f, 0f),
                                Vector3f(-sideLength * 0.5f, 0f, 0f)
                            )
                        ObjectManager.addObjectToPhysicsWorld(joint, world.name)
                        }
                    }
                    // 与下方相邻的点约束
                    if (z < length) {
                        val downKey = Vector3i(x, z + 1, 0).toToken()
                        val downBody = cloth.rigidBodies[downKey]
                        if (downBody != null) {
//                        if(!(currentBody.isInWorld)) Physics.physicsWorlds[world.name]!!.first.add(currentBody)
//                        if(!(downBody.isInWorld)) Physics.physicsWorlds[world.name]!!.first.add(downBody)
                            val joint = Point2PointJoint(
                                currentBody, downBody,
                                Vector3f(0f, 0f, sideLength * 0.5f),
                                Vector3f(0f, 0f, -sideLength * 0.5f)
                            )
                            ObjectManager.addObjectToPhysicsWorld(joint, world.name)
                        }
                    }
                }
            }
//            TaskProcessor.handleRequestInstant()

            clothes.add(world.name to cloth)
        }
    }
    val xList = mutableListOf<Float>()
    val yList = mutableListOf<Float>()
    val zList = mutableListOf<Float>()
    fun clothTick(worldName: String) {
        for ((world, cloth) in clothes) {
            if(world != worldName) continue
            val rigidBodies = cloth.rigidBodies
            val displays = cloth.displayBlocks
            for ((token, rigidBody) in rigidBodies) {
                val displayBlockEntity = displays[token] ?: continue

                val rigidBodyPos = rigidBody.getPhysicsLocation(null)
                val rigidBodyRot = rigidBody.getPhysicsRotation(null)

                val oldTransformation = displayBlockEntity.transformation
                val rotationQuat = Quaternionf(rigidBodyRot.x, rigidBodyRot.y, rigidBodyRot.z, rigidBodyRot.w)
                val offset = org.joml.Vector3f(oldTransformation.scale.x / 2.0f, oldTransformation.scale.y / 2.0f, oldTransformation.scale.z / 2.0f)
                val rotatedOffset = rotationQuat.transform(offset)
//                val blockCenter = rigidBodyPos.subtract(rotatedOffset.toVecmath().toJmeVector3f())
                val newTransformation = Transformation(
                    oldTransformation.translation,
                    rotationQuat,
                    oldTransformation.scale,
                    oldTransformation.rightRotation
                )
                displayBlockEntity.teleportDuration = 2
                displayBlockEntity.interpolationDuration = 2
                displayBlockEntity.interpolationDelay = 0
                displayBlockEntity.teleportAsync(Location(Bukkit.getWorld(worldName), rigidBodyPos.x.toDouble(), rigidBodyPos.y.toDouble(), rigidBodyPos.z.toDouble()).clone().subtract(rotatedOffset.x.toDouble(), rotatedOffset.y.toDouble(), rotatedOffset.z.toDouble()))
                displayBlockEntity.transformation = newTransformation
                xList += displayBlockEntity.location.x.toFloat()
                yList += displayBlockEntity.location.y.toFloat()
                zList += displayBlockEntity.location.z.toFloat()
            }
            updateNearbyBlocks(Location(Bukkit.getWorld(world), xList.average(), yList.average(), zList.average()), Vector3i(2, 2, 2))
            xList.clear()
            yList.clear()
            zList.clear()
        }
    }
}