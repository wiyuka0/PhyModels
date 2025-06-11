package com.volatila.phymodels.physics.objmanager

import com.jme3.bullet.collision.shapes.BoxCollisionShape
import com.jme3.bullet.collision.shapes.CompoundCollisionShape
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.math.Transform
import com.jme3.math.Vector3f
import com.volatila.phymodels.physics.objmanager.BlockPhysicsUtil.BOX_SHAPE
import com.volatila.phymodels.physics.materialattributes.getFriction
import com.volatila.phymodels.physics.materialattributes.getRestitution
import com.volatila.phymodels.physics.scheduler
import org.bukkit.Material
import org.joml.Vector3i
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.collections.iterator

// ---- Core Block Physics Updater ----

object BlockPhysicsUpdater {
    const val CHUNK_SIZE = 8
    private val chunkBodies = ConcurrentHashMap<BlockChunkCoord, ChunkRigidBody>()

    fun addBlock(pos: Vector3i, material: Material) {
        val chunkCoord = getChunkCoord(pos)
        val chunk = chunkBodies.getOrPut(chunkCoord) { ChunkRigidBody(chunkCoord) }
        chunk.addBlock(pos, material)
    }

    fun removeBlock(pos: Vector3i) {
        val chunkCoord = getChunkCoord(pos)
        chunkBodies[chunkCoord]?.removeBlock(pos)
    }

    fun updateAllChunks() {
        for (chunk in chunkBodies.values.toList().filter { it.needUpdate() }) chunk.commitChanges()
    }

    private fun getChunkCoord(pos: Vector3i): BlockChunkCoord {
        return BlockChunkCoord(pos.x shr 3, pos.y shr 3, pos.z shr 3)
    }
}

// ---- ChunkRigidBody: Represents a group of blocks ----

class ChunkRigidBody(private val coord: BlockChunkCoord) {
    private val subRegions = ConcurrentHashMap<Material, PhysicsRigidBody>()

    //    private val blocks = hashMapOf<Vector3i, Material>()
    private val pendingAdd = ConcurrentHashMap<Vector3i, Material>()
    private val pendingRemove = ConcurrentLinkedQueue<Vector3i>()

    private var dirty = true


    fun needUpdate(): Boolean{
        return dirty
    }

    /*
    TODO 为每个存在的Material分配一个CompoundShape，其形状是当前Chunk里所有material为it的方块的组合
     */
    fun addBlock(pos: Vector3i, material: Material) {
//        if (blocks.contains(pos)) return
//        dirty = true
//        pendingAdd.add(pos)
//        pendingRemove.remove(pos)
        pendingAdd[pos] = material
        pendingRemove.remove(pos)

        dirty = true
    }

    fun removeBlock(pos: Vector3i) {
        dirty = true
//        if (!blocks.contains(pos)) return
//        dirty = true
//        pendingRemove.add(pos)
//        pendingAdd.remove(pos)
        pendingRemove.add(pos)
        pendingAdd.remove(pos)
    }

    fun commitChanges() {
        this.dirty = false
        if (pendingAdd.isEmpty() && pendingRemove.isEmpty()) return
        val pendingAddSnapshot = pendingAdd// for avoiding concurrent modification
        val pendingRemoveSnapshot = pendingRemove.toSet()// for avoiding concurrent modification

        for ((v3i, material) in pendingAddSnapshot) {
            val subRegion = subRegions[material]
            if (subRegion == null) {
                subRegions[material] =
                    PhysicsRigidBody(CompoundCollisionShape(), 0f).apply { // set rigidbody attributes
                        this.restitution = material.getRestitution()
                        this.friction = material.getFriction()
                    }
                subRegions[material]?.userObject = "chunk-${coord.x},${coord.y},${coord.z}-$material"
                ObjectManager.addObjectToPhysicsWorld(subRegions[material]!!)
            }
            val oldCompoundShape = subRegions[material]?.collisionShape as CompoundCollisionShape
            val localTransform = v3i.toChunkLocalTransform(coord)
            if (oldCompoundShape.listChildren().find { it.copyTransform(null) == localTransform } != null) continue
            oldCompoundShape.addChildShape(BOX_SHAPE, localTransform)
            subRegions[material]?.collisionShape = oldCompoundShape
        }
        pendingAdd.clear()

        if (!pendingRemove.isEmpty()) for ((material, rigidBody) in subRegions) {
            val removeList = pendingRemoveSnapshot.filter {
                for (shape in (rigidBody.collisionShape as CompoundCollisionShape).listChildren()) {
                    val shapeOffset = shape.copyOffset(null)
                    val localTransform = it.toChunkLocalTransform(coord)
                    if (shape.shape == BOX_SHAPE && Vector3i(
                            shapeOffset.x.toInt(),
                            shapeOffset.y.toInt(),
                            shapeOffset.z.toInt()
                        ) == Vector3i(
                            localTransform.translation.x.toInt(),
                            localTransform.translation.y.toInt(),
                            localTransform.translation.z.toInt()
                        )
                    ) {
                        return@filter true
                    }
                }
                return@filter false
            }

            if (removeList.isEmpty()) continue

            val updatedShapes =
                (rigidBody.collisionShape as CompoundCollisionShape).listChildren().toMutableList() // 重新构建形状

            for (pos in removeList) {
                val localTransform = pos.toChunkLocalTransform(coord)
                updatedShapes.removeIf { it.shape == BOX_SHAPE && it.copyTransform(null) == localTransform }
            }

            val newCompoundShape = CompoundCollisionShape()
            for (child in updatedShapes) newCompoundShape.addChildShape(child.shape, child.copyOffset(null))
            rigidBody.collisionShape = newCompoundShape
        }
        pendingRemove.clear()

        for (material in subRegions.keys) {
            val rigidBody = subRegions[material]
            if (rigidBody != null) {
                val worldOffset = Vector3f(
                    coord.x * BlockPhysicsUpdater.CHUNK_SIZE.toFloat(),
                    coord.y * BlockPhysicsUpdater.CHUNK_SIZE.toFloat(),
                    coord.z * BlockPhysicsUpdater.CHUNK_SIZE.toFloat()
                )
                rigidBody.scheduler {
                    rigidBody.setPhysicsLocation(worldOffset)
                }
            }
        }
    }

}

// ---- Utility & Shape ----

object BlockPhysicsUtil {
    val BOX_SHAPE = BoxCollisionShape(Vector3f(0.5f, 0.5f, 0.5f))
}

fun Vector3i.toChunkLocalTransform(chunkCoord: BlockChunkCoord): Transform {
    val localX = this.x - (chunkCoord.x shl 3)
    val localY = this.y - (chunkCoord.y shl 3)
    val localZ = this.z - (chunkCoord.z shl 3)
    val transform = Transform()
    transform.translation = Vector3f(localX + 0.5f, localY + 0.5f, localZ + 0.5f)
    return transform
}

// ---- Block Chunk Coord ----

data class BlockChunkCoord(val x: Int, val y: Int, val z: Int){
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (other !is Model.Vector3i) return false
//        return x == other.x && y == other.y && z == other.z
//    }
//
//    override fun hashCode(): Int {
//        var result = x
//        result = 31 * result + y
//        result = 31 * result + z
//        return result
//    }
}

// ---- Physics Access ----
