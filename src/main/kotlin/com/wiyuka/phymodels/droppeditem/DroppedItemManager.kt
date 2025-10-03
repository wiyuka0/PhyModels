package com.wiyuka.phymodels.droppeditem

import com.jme3.bullet.collision.shapes.BoxCollisionShape
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.math.Vector3f
import com.wiyuka.phymodels.PhysicsTickManager.Companion.updateNearbyBlocks
import com.wiyuka.phymodels.model.Model
import com.wiyuka.phymodels.physics.materialattributes.getFriction
import com.wiyuka.phymodels.physics.materialattributes.getMass
import com.wiyuka.phymodels.physics.materialattributes.getRestitution
import com.wiyuka.phymodels.physics.obj.toJomlVector3f
import com.wiyuka.phymodels.physics.obj.toLocation
import com.wiyuka.phymodels.physics.obj.toVecmath
import com.wiyuka.phymodels.physics.obj.toVecmathQuat4f
import com.wiyuka.phymodels.physics.obj.toVecmathVector3f
import com.wiyuka.phymodels.physics.obj.toVector3f
import com.wiyuka.phymodels.physics.objmanager.ObjectManager
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3i
import java.util.concurrent.ConcurrentHashMap
import javax.vecmath.Matrix4f
import javax.vecmath.Quat4f
import kotlin.random.Random

class DroppedItemManager {
    companion object{
        const val ITEM_SCALE: Float = 1f/16f               // m
        const val ITEM_HALF_SIZE: Float = ITEM_SCALE / 2f
        const val BLOCK_HALF_SIZE: Float = ITEM_SCALE * 2f // m
//        private val GLOBAL_ITEM_SHAPE = CompoundCollisionShape().apply {
//            for(i in -8..7) for(j in -8..7) for(k in -8..7){
//                val offset = Vector3f(i.toFloat() * ITEM_SCALE, j.toFloat() * ITEM_SCALE, k.toFloat() * ITEM_SCALE)
//                val childShape = BoxCollisionShape(Vector3f(ITEM_HALF_SIZE, ITEM_HALF_SIZE, ITEM_HALF_SIZE))
//                this.addChildShape(childShape)
//            }
//        }
        val GLOBAL_ITEM_SHAPE  = BoxCollisionShape(Vector3f(1f/2f, 1/16f, 1f/2f))
        val GLOBAL_BLOCK_SHAPE = BoxCollisionShape(Vector3f(1f/4f, 1/4f, 1f/4f))

        private val livingDroppedItem = ConcurrentHashMap<Int, DroppedItem>()

        fun buildDroppedItemRigidBody(material: Material): PhysicsRigidBody{
            return PhysicsRigidBody(GLOBAL_ITEM_SHAPE).apply {
                this.restitution   = material.getRestitution()
                this.friction      = material.getFriction()
                this.mass          = material.getMass()

                this.userObject    = "DroppedItem-${material}"
            }
        }

        fun createDroppedItem(itemStack: ItemStack, location: Location): DroppedItem {
            val droppedItemRigidBody = buildDroppedItemRigidBody(itemStack.type)

            val droppedItemDisplay = location.world.spawnEntity(location, EntityType.ITEM_DISPLAY) as ItemDisplay
            droppedItemDisplay.teleportAsync(location)
            droppedItemDisplay.setItemStack(itemStack)

            val droppedItem = DroppedItem(itemStack, droppedItemRigidBody, droppedItemDisplay)
            ObjectManager.addObjectToPhysicsWorld(droppedItemRigidBody, location.world.name)
            livingDroppedItem[Random.nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE)] = droppedItem

            return droppedItem
        }

        fun applyDroppedItemTick(){
//            for (item in livingDroppedItem) {
//                val itemRigidBody = item.rigidBody
//                val itemEntity = item.itemDisplay
//
//                val rbTransform = itemRigidBody.getTransform(null)
//                val rbTranslation = rbTransform.translation
//
//                val oldTransformation = itemEntity.transformation
//
//                val entityLoc = itemEntity.location.toVector3f().toJomlVector3f()
//                if(rbTranslation.toVecmathVector3f().toJomlVector3f().distance(entityLoc) >= 48){
//                    itemEntity.teleportAsync(rbTranslation.toVecmathVector3f().toLocation(world = Bukkit.getWorlds()[0]))
//                }
//                val offset = org.joml.Vector3f(rbTranslation.x - entityLoc.x, rbTranslation.y - entityLoc.y, rbTranslation.z - entityLoc.z)
//
//                val rbRotation = rbTransform.rotation
//                val rotationMatrix = Matrix4f().apply {
//                    val negativeRotation = Quat4f(-rbRotation.x, -rbRotation.y, -rbRotation.z, rbRotation.w)
//                    set(negativeRotation)
//                }
//                val offsetVecmath = offset.toVecmath()
//                rotationMatrix.transform(offsetVecmath)
//
//                val newTransformation = Transformation(
//                    offsetVecmath.toJomlVector3f(),
////                    org.joml.Vector3f(0f, 0f, 0f),
//                    Quaternionf(rbTransform.rotation.x, rbTransform.rotation.y, rbTransform.rotation.z, rbTransform.rotation.w),
//                    oldTransformation.scale,
//                    oldTransformation.leftRotation
//                )
//                itemEntity.interpolationDelay = 0
//                itemEntity.interpolationDuration = 2
//
//                itemEntity.transformation = newTransformation
//
////                itemEntity.teleportAsync(rbTranslation.toVecmathVector3f().toLocation(world = Bukkit.getWorlds()[0]))
//
//                updateNearbyBlocks(rbTranslation.toVecmathVector3f().toLocation(world = itemEntity.world), Vector3i(2, 2, 2))
//            }
            for (item in livingDroppedItem.values) {
                val itemRigidBody = item.rigidBody
                val itemEntity = item.itemDisplay

                val rbTransform = itemRigidBody.getTransform(null)
                val rbRotation = itemRigidBody.getPhysicsRotation(null)
                val rbTranslation = itemRigidBody.getPhysicsLocation(null)

                val entityLoc = itemEntity.location.toJomlVector3f()

                // 如果实体和物理位置距离超过48，就强制同步位置
                if (rbTranslation.toVecmathVector3f().toJomlVector3f().distance(entityLoc) >= 48) {
                    itemEntity.teleportAsync(rbTranslation.toVecmathVector3f().toLocation(world = Bukkit.getWorlds()[0]))
                }

                // 计算世界坐标下的偏移向量
                val offsetWorld = org.joml.Vector3f(
                    rbTranslation.x - entityLoc.x,
                    rbTranslation.y - entityLoc.y,
                    rbTranslation.z - entityLoc.z
                )

                val rbOffsetNoRotation = org.joml.Vector3f(
                    entityLoc.x - offsetWorld.x,
                )

                // 构造新的 Transformation
//                val rbRotation = Quaternionf(
//                    rbTransform.rotation.x,
//                    rbTransform.rotation.y,
//                    rbTransform.rotation.z,
//                    rbTransform.rotation.w
//                )

                (offsetWorld)
                val newTransformation = Transformation(
                    offsetWorld, //只能传offset
                    Quaternionf(rbRotation.x, rbRotation.y, rbRotation.z, rbRotation.w),
                    itemEntity.transformation.scale,
                    itemEntity.transformation.rightRotation,
                )

                // 设置插值参数
                itemEntity.interpolationDelay = 0
                itemEntity.interpolationDuration = 2
                itemEntity.transformation = newTransformation

                // 更新方块周围逻辑
                updateNearbyBlocks(rbTranslation.toVecmathVector3f().toLocation(world = itemEntity.world), Vector3i(2, 2, 2))
            }


        }


    }
}