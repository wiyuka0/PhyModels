package com.wiyuka.phymodels.physics.obj

import com.jme3.bullet.collision.shapes.CollisionShape
import com.jme3.bullet.collision.shapes.CompoundCollisionShape
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.math.Quaternion
import com.wiyuka.phymodels.PhysAPI
import com.wiyuka.phymodels.PhysicsTickManager
import com.wiyuka.phymodels.debug.DebugUtil
import com.wiyuka.phymodels.model.Model
import com.wiyuka.phymodels.model.ModelScriptData
import com.wiyuka.phymodels.physics.Physics
import com.wiyuka.phymodels.physics.objmanager.ObjectManager
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Player
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3i
import java.util.UUID
import javax.vecmath.Matrix4f
import javax.vecmath.Quat4f
import javax.vecmath.Vector3f
import kotlin.math.ceil

data class ModelEntity(
    var model: Model,
    var location: Location,
    var rotation: Quat4f,
    val displayBlocks: HashMap<Vector3i, BlockUnit>,
    val modelType: PhysAPI.Companion.BodyType,
    val scale: Float,
    var fetchingPlayer: Player?,
    val uniqueId: String = UUID.randomUUID().toString(),
    val modelScriptData: ModelScriptData?,
    var massCenterRel: Vector3f, // un-rotation
    var rotatedMassCenter: Vector3f, // rotated
    var absoluteMassCenter: Vector3f, // absolute

    var needCheckShape: Boolean = false
) {

    val rotationMatrix = Matrix4f()
    var rigidBody: PhysicsRigidBody
        get() = ObjectManager.getModelBody(this)
        set(value) {
            ObjectManager.bindRigidBody(this, value)}

    fun addChildShape(childShape: CollisionShape, offset: Vector3f) {
        (this.rigidBody.collisionShape as CompoundCollisionShape).addChildShape(childShape, offset.toJmeVector3f())
    }

    fun removeChildShape(childShapeOffset: Vector3f) {
        val childShapes = (this.rigidBody.collisionShape as CompoundCollisionShape).listChildren().toMutableList()
        childShapes.removeIf { it.copyOffset(null) == childShapeOffset.toJmeVector3f() }
        var newShape = rigidBody.collisionShape as CompoundCollisionShape
        newShape = CompoundCollisionShape().apply {
            for (shape in childShapes) addChildShape(
                shape.shape,
                shape.copyOffset(null)
            )
        }
        this.rigidBody.collisionShape = newShape

        //TODO 检查移除后的整个刚体是否已经被分成两个或多个部分，并逐一向世界添加被分离的部分作为一个新的PHYSICAL model
        // 或者上面说的内容交给物理模拟线程去执行，即Physics.tick()方法内部，此时只需要将 needCheckShape 设置为true，在tick()里面遍历每个需要检查的模型,
        // 如果需要分离就执行分离的步骤，不需要就跳过，然后把 needCheckShape 设置为false
        // 然后将分离的模型添加到Physics中即可

        needCheckShape = true
    }

    fun updateBlockDisplayLocation(currentLocation: Location, currentRotation: Quat4f, tickTimeRate: Float){
        this.location = currentLocation
        this.rotation = currentRotation

        val rotatedMassCenter = massCenterRel.clone() as Vector3f
            Matrix4f().apply { set(rotation) }.transform(rotatedMassCenter)

        this.rotatedMassCenter = rotatedMassCenter
        this.absoluteMassCenter = Vector3f(
            (currentLocation.x + rotatedMassCenter.x).toFloat(),
            (currentLocation.y + rotatedMassCenter.y).toFloat(),
            (currentLocation.z + rotatedMassCenter.z).toFloat()
        )


        DebugUtil.drawParticle(Color.AQUA, this.rotatedMassCenter.toLocation(world = currentLocation.world).clone().add(currentLocation.x, currentLocation.y, currentLocation.z))

        calcTranslation(tickTimeRate)
    }

    fun calcTranslation(tickTimeRate: Float) {
        updateRotationMatrix()
        val currentModelOrigin = model.getOriginByGeometricCenterWithRotation(location, rotation, scale)
        DebugUtil.drawParticle(Color.RED, currentModelOrigin)
        DebugUtil.drawParticle(Color.BLUE, location)
        displayBlocks.forEach { (sourceLoc, blockDisplay) ->
            handleModelBlockMoving(sourceLoc, currentModelOrigin, blockDisplay.entity, tickTimeRate)
        }
    }

    private fun updateRotationMatrix(){
        rotationMatrix.set(rotation)
    }

    private var keepLocation = true

    private fun handleModelBlockMoving(
        sourceLoc: Vector3i,
        currentModelOrigin: Location,
        blockDisplay: BlockDisplay,
        tickTimeRate: Float
    ) {
        val scaledSourceLoc = Vector3f(
            sourceLoc.x.toFloat() * scale,
            sourceLoc.y.toFloat() * scale,
            sourceLoc.z.toFloat() * scale
        )

        rotationMatrix.transform(scaledSourceLoc)

        val newLocation = buildNewLocation(currentModelOrigin, scaledSourceLoc)
            ?: return

        val distanceToNewLocation = blockDisplay.location.distance(newLocation)
        if (distanceToNewLocation > 48) keepLocation = false

        val newTransformation = buildNewTransformation(newLocation, blockDisplay, keepLocation)
        // 应用变换逻辑
        applyTransformation(keepLocation, blockDisplay, newTransformation, newLocation, ceil((PhysicsTickManager.getAvgTime()) / 50.0).toInt())
        keepLocation = true
    }

    private fun buildNewLocation(
        currentModelOrigin: Location,
        scaledSourceLoc: Vector3f
    ): Location? {
        val newLocation = Location(
            location.world ?: return null,
            currentModelOrigin.x + scaledSourceLoc.x,// - appendOffset.x,
            currentModelOrigin.y + scaledSourceLoc.y,// - appendOffset.y,
            currentModelOrigin.z + scaledSourceLoc.z// - appendOffset.z
        )
        return newLocation
    }


    val tempVector = org.joml.Vector3f()
    private fun buildNewTransformation(
        newLocation: Location,
        blockDisplay: BlockDisplay,
        keepLocation: Boolean
    ): Transformation = Transformation(
        if(keepLocation) {
            tempVector.set(
                (newLocation.x - blockDisplay.location.x).toFloat(),
                (newLocation.y - blockDisplay.location.y).toFloat(),
                (newLocation.z - blockDisplay.location.z).toFloat()
            )
            tempVector
        } else tempVector.apply { set(0f, 0f, 0f) },
        Quaternionf(
            rotation.x.toFloat(),
            rotation.y.toFloat(),
            rotation.z.toFloat(),
            rotation.w.toFloat()
        ),
        blockDisplay.transformation.scale,
        blockDisplay.transformation.rightRotation
    )

    private fun applyTransformation(
        positiveOffset: Boolean,
        blockDisplay: BlockDisplay,
        newTransformation: Transformation,
        newLocation: Location,
        interpolationDuration: Int
    ) {
        if (positiveOffset) {
            blockDisplay.interpolationDuration = interpolationDuration
            blockDisplay.interpolationDelay = 0
            blockDisplay.transformation = newTransformation
        } else {
            blockDisplay.interpolationDuration = 0
            blockDisplay.teleportAsync(newLocation)
            blockDisplay.transformation = newTransformation
        }
    }
}

fun Vector3f.toLocation(world: World): Location {
    return Location(
        world,
        this.x.toDouble(),
        this.y.toDouble(),
        this.z.toDouble()
    )
}

fun Location.toVector3f(): Vector3f {
    return Vector3f(this.x.toFloat(), this.y.toFloat(), this.z.toFloat())
}
fun Location.toJmeVector3f(): com.jme3.math.Vector3f {
    return com.jme3.math.Vector3f(this.x.toFloat(), this.y.toFloat(), this.z.toFloat())
}
fun Location.toJomlVector3f(): org.joml.Vector3f {
    return org.joml.Vector3f(this.x.toFloat(), this.y.toFloat(), this.z.toFloat())
}

fun PhysicsRigidBody.getGeometricCenter(rotation: Quat4f): Vector3f {
    val matrix4f = Matrix4f()
    matrix4f.set(rotation)
    val aabbCenter = this.collisionShape.aabbCenter(null).toVecmathVector3f()
    matrix4f.transform(aabbCenter)
    val physicsLocation = getPhysicsLocation(null)
    return Vector3f(
        aabbCenter.x + physicsLocation.x,
        aabbCenter.y + physicsLocation.y,
        aabbCenter.z + physicsLocation.z
    )
}


fun Vector3f.relativeTo(other: Vector3f): Vector3f {
    return Vector3f(this.x - other.x, this.y - other.y, this.z - other.z)
}
fun Vector3f.toJmeVector3f(): com.jme3.math.Vector3f {
    return com.jme3.math.Vector3f(this.x, this.y, this.z)
}
fun org.joml.Vector3f.toJmeVector3f(): com.jme3.math.Vector3f {
    return com.jme3.math.Vector3f(this.x, this.y, this.z)
}
fun com.jme3.math.Vector3f.toVecmathVector3f(): Vector3f {
    return Vector3f(this.x, this.y, this.z)
}
fun com.jme3.math.Vector3f.toLocation(world: World): Location {
    return Location(world, this.x.toDouble(), this.y.toDouble(), this.z.toDouble())
}

fun Quaternion.toVecmathQuat4f(): Quat4f{
    return Quat4f(this.x, this.y, this.z, this.w)
}
fun Quaternion.toJomlQuaternionf(): Quaternionf{
    return Quaternionf(this.x.toFloat(), this.y.toFloat(), this.z.toFloat(), this.w.toFloat())
}
fun org.joml.Vector3f.toVecmath(): Vector3f {
    return Vector3f(this.x.toFloat(), this.y.toFloat(), this.z.toFloat())
}

fun javax.vecmath.Vector3f.toJomlVector3f(): org.joml.Vector3f {
    return org.joml.Vector3f(this.x.toFloat(), this.y.toFloat(), this.z.toFloat())
}