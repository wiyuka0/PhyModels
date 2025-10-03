package com.wiyuka.phymodels.physics.body

import com.jme3.bullet.collision.shapes.BoxCollisionShape
import com.jme3.bullet.joints.Point2PointJoint
import com.jme3.bullet.objects.PhysicsRigidBody
import com.wiyuka.phymodels.PhysAPI
import com.wiyuka.phymodels.model.Model
import com.wiyuka.phymodels.model.ModelManager
import com.wiyuka.phymodels.physics.Physics
import com.wiyuka.phymodels.physics.obj.ModelEntity
import com.wiyuka.phymodels.physics.obj.toJmeVector3f
import com.wiyuka.phymodels.physics.obj.toVecmath
import com.wiyuka.phymodels.physics.obj.toVector3f
import com.wiyuka.phymodels.physics.objmanager.ObjectManager
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.joml.Vector3f
import org.joml.Vector3i
import java.awt.image.BufferedImage
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class BodyManager {
    companion object {
        val bodies = ConcurrentHashMap<String, Body>()

        fun createBody(baseLocation: Location): Body {
            // 1. 为新身体生成一个唯一ID
            val bodyUniqueId = UUID.randomUUID().toString()

            // 2. 创建一个Map，用于存储每个肢体名称与其对应的物理刚体
            val rigidBodyMap = mutableMapOf<String, PhysicsRigidBody>()
            val itemDisplayMap = mutableMapOf<String, ItemDisplay>()
            val limbData = Body.limbData

            // 3. 遍历并创建所有肢体的物理实体
            limbData.forEach { (limbName, limbInfo) ->
//                val limbModel = buildLimbModel(limbInfo)
//                val modelEntity = createLimbDisplay(limbInfo, limbModel, baseLocation)
                val rigidBody = buildRigidBody(limbInfo, baseLocation)
                val itemDisplay = buildItemDisplay(ItemStack(Material.DIAMOND), baseLocation.clone().add(limbInfo.offset.x.toDouble(), limbInfo.offset.y.toDouble(), limbInfo.offset.z.toDouble()))
                rigidBody.restitution = 0.1f
                rigidBodyMap[limbName] = rigidBody
                itemDisplayMap[limbName] = itemDisplay
//                ObjectManager.addObjectToPhysicsWorld(modelEntity.rigidBody, baseLocation.world.name)
            }

            // =================================================================
            // 4. 为肢体之间创建并添加物理约束 (Joints)
            // =================================================================
            println("\n--- Creating Body Constraints ---")
            val centralBodyName = "Torso" // 定义一个中心身体部件
            val centralBody = rigidBodyMap[centralBodyName]
            val centralBodyInfo = limbData[centralBodyName]

            if (centralBody != null && centralBodyInfo != null) {
                // 定义连接关系：Key是要连接的肢体，Value是它所连接到的父肢体 (这里都是Torso)
                val jointMap = mapOf(
                    "Head" to centralBodyName,
                    "LeftHand" to centralBodyName,
                    "RightHand" to centralBodyName,
                    "LeftLeg" to centralBodyName,
                    "RightLeg" to centralBodyName
                )

                jointMap.forEach { (childLimbName, parentLimbName) ->
                    val childBody = rigidBodyMap[childLimbName]
                    val childLimbInfo = limbData[childLimbName]

                    if (childBody != null && childLimbInfo != null) {
                        // 4.1. 计算约束的锚点 (Pivot points)
                        // 锚点是相对于每个刚体自身中心的局部坐标

                        // 子肢体的锚点：直接使用其LimbInfo中定义的旋转锚点
                        val pivotInChild = childLimbInfo.rotationAnchorPoint

                        // 父肢体（躯干）的锚点：需要计算。
                        // 它的位置应该和子肢体锚点在世界空间中的位置重合。
                        // 计算方法：(子肢体世界偏移 + 子肢体锚点) - 父肢体世界偏移
                        val pivotInParent = (Vector3f(childLimbInfo.offset).add(childLimbInfo.rotationAnchorPoint)).sub(centralBodyInfo.offset)

                        println("Creating joint for $childLimbName -> $parentLimbName")
                        println(" > pivotInChild: $pivotInChild")
                        println(" > pivotInParent: $pivotInParent")
                        // 4.2. 创建约束实例
                        // 4.2.1 检查实例是否在物理世界中
                        val physicsSpace = Physics.physicsWorlds[baseLocation.world.name]!!.first
                        if (!physicsSpace.contains(childBody)) {
                            ObjectManager.addObjectToPhysicsWorld(childBody, worldName = baseLocation.world.name)
                        }
                        if(!physicsSpace.contains(centralBody)) {
                            ObjectManager.addObjectToPhysicsWorld(centralBody, worldName = baseLocation.world.name)
                        }
                        val constraint = Point2PointJoint(childBody, centralBody, pivotInChild.toVecmath().toJmeVector3f(), pivotInParent.toVecmath().toJmeVector3f())
                        println("Creating constraint between $childLimbName and $parentLimbName...")
                        // 4.3. 将约束添加到物理世界
                        ObjectManager.addObjectToPhysicsWorld(constraint, baseLocation.world.name)
                    }
                }
            }

            // 5. 使用包含所有肢体刚体的Map和唯一ID，创建一个新的Body实例
            val newBody = Body(rigidBodyMap, itemDisplayMap, bodyUniqueId)

            // 6. 将新创建的Body实例存入管理器中
            bodies[bodyUniqueId] = newBody

            // 7. 返回完整创建的Body实例
            println("--- Body creation complete for ID: $bodyUniqueId ---\n")
            return newBody
        }


        fun buildLimbModel(limbInfo: LimbInfo, skinImage: BufferedImage? = null): Model{
            // TODO: Dynamic skin model
            val limbScale = limbInfo.scale
            val blocks = mutableMapOf<Vector3i, Material>()

            for (x in 0..(limbScale.x * 16).toInt()) for (y in 0..(limbScale.y * 16).toInt()) for (z in 0..(limbScale.z * 16).toInt())
                blocks[Vector3i(x,y,z)] = Material.WHITE_CONCRETE

            return Model(blocks, limbInfo.mass, "${UUID.randomUUID()}-bodylimb-${limbInfo.scale}")
        }


        fun buildRigidBody(limbInfo: LimbInfo, baseLocation: Location): PhysicsRigidBody{
            val boxShape = BoxCollisionShape(limbInfo.scale.toVecmath().toJmeVector3f())
            val physicsRigidBody = PhysicsRigidBody(boxShape)
            physicsRigidBody.mass = limbInfo.mass
            physicsRigidBody.restitution = 0.2f
            physicsRigidBody.setPhysicsLocation(baseLocation.toJmeVector3f())
            return physicsRigidBody
        }
        fun buildItemDisplay(itemStack: ItemStack, location: Location, skinImage: BufferedImage? = null): ItemDisplay{
            val itemDisplay = (location.world.spawnEntity(location, EntityType.ITEM_DISPLAY)) as ItemDisplay
            itemDisplay.setItemStack(itemStack)
            itemDisplay.teleportDuration = 1
            return itemDisplay
        }

        fun createLimbDisplay(limbInfo: LimbInfo, limbModel: Model, baseLocation: Location): ModelEntity {
            val modelEntity = ModelManager.generateModel(
                model = limbModel,
                location = baseLocation.clone().add(limbInfo.offset.x.toDouble(), limbInfo.offset.y.toDouble(), limbInfo.offset.z.toDouble()),
                scale = 1f/16f,
                modelType = PhysAPI.Companion.BodyType.PHYSICAL,
                worldName = baseLocation.world.name,
                specialTag = "bodylimb"
            )
            return modelEntity
        }
    }
}