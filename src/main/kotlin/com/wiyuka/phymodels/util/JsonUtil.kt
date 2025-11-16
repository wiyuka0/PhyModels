package com.wiyuka.phymodels.util

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.jme3.bullet.collision.shapes.BoxCollisionShape
import com.jme3.bullet.collision.shapes.CollisionShape
import com.jme3.bullet.collision.shapes.CompoundCollisionShape
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.math.Quaternion
import com.jme3.math.Vector3f

object JsonUtil {
    fun PhysicsRigidBody.toJson(): JsonObject {
        val json = JsonObject()
        json.addProperty("mass", this.mass)
        json.addProperty("friction", this.friction)
        json.addProperty("restitution", this.restitution)

        json.addProperty("userObject", this.userObject as String)


        val locationJson = JsonArray()
        val rotationJson = JsonArray()

        val location = this.getPhysicsLocation(null)
        val rotation = this.getPhysicsRotation(null)

        locationJson.add(location.x)
        locationJson.add(location.y)
        locationJson.add(location.z)

        rotationJson.add(rotation.x)
        rotationJson.add(rotation.y)
        rotationJson.add(rotation.z)
        rotationJson.add(rotation.w)

        val transformation = JsonObject()

        transformation.add("location", locationJson)
        transformation.add("rotation", rotationJson)

        json.add("transformation", transformation)

        val velocityJson = JsonObject()

        val linearVelocity = this.getLinearVelocity(null)
        val angularVelocity = this.getAngularVelocity(null)

        val linearVelJson = JsonArray()
        val angularVelJson = JsonArray()

        linearVelJson.add(linearVelocity.x)
        linearVelJson.add(linearVelocity.y)
        linearVelJson.add(linearVelocity.z)

        angularVelJson.add(angularVelocity.x)
        angularVelJson.add(angularVelocity.y)
        angularVelJson.add(angularVelocity.z)

        velocityJson.add("linearVelocity", linearVelJson)
        velocityJson.add("angularVelocity", angularVelJson)

        json.add("velocity", velocityJson)

        val shapeJson = JsonObject()

        if(collisionShape is CompoundCollisionShape) {
            shapeJson.addProperty("type", "Compound")
            val subBoxes = JsonArray()
            for (childCollisionShape in (collisionShape as CompoundCollisionShape).listChildren()) {
                if(childCollisionShape.shape is BoxCollisionShape) {
                    val boxShape = childCollisionShape.shape as BoxCollisionShape
                    val boxData = JsonObject()
                    boxData.addProperty("type", "Box")

                    val scaleJson = JsonArray()
                    val scale = boxShape.getScale(null)
                    scaleJson.add(scale.x)
                    scaleJson.add(scale.y)
                    scaleJson.add(scale.z)

                    boxData.add("scale", scaleJson)

                    val translationJson = JsonArray()
                    val translation = childCollisionShape.copyOffset(null)
                    translationJson.add(translation.x)
                    translationJson.add(translation.y)
                    translationJson.add(translation.z)

                    boxData.add("translation", translationJson)

                    subBoxes.add(boxData)
                }
            }
            shapeJson.add("subBoxes", subBoxes)
        }else if (collisionShape is BoxCollisionShape) {
            shapeJson.addProperty("type", "Box")
            val scaleJson = JsonArray()
            val scale = collisionShape.getScale(null)
            scaleJson.add(scale.x)
            scaleJson.add(scale.y)
            scaleJson.add(scale.z)
            shapeJson.add("scale", scaleJson)
        }

        json.add("shape", shapeJson)

        return json
    }

    fun JsonObject.toPhysicsRigidBody(): PhysicsRigidBody {
        val mass = this.get("mass").asFloat
        val friction = this.get("friction").asFloat
        val restitution = this.get("restitution").asFloat
        val userObject = this.get("userObject").asString

        val transformJson = this.getAsJsonObject("transformation")

        val locationArr = transformJson.getAsJsonArray("location")
        val rotationArr = transformJson.getAsJsonArray("rotation")

        val location = Vector3f(
            locationArr[0].asFloat,
            locationArr[1].asFloat,
            locationArr[2].asFloat
        )

        val rotation = Quaternion(
            rotationArr[0].asFloat,
            rotationArr[1].asFloat,
            rotationArr[2].asFloat,
            rotationArr[3].asFloat
        )

        val shapeJson = this.getAsJsonObject("shape")
        val shapeType = shapeJson.get("type").asString

        val shape: CollisionShape = when (shapeType) {
            "Box" -> {
                val scaleArr = shapeJson.getAsJsonArray("scale")
                val halfExtents = Vector3f(
                    scaleArr[0].asFloat,
                    scaleArr[1].asFloat,
                    scaleArr[2].asFloat
                )
                BoxCollisionShape(halfExtents)
            }
            "Compound" -> {
                val compound = CompoundCollisionShape()

                val subBoxes = shapeJson.getAsJsonArray("subBoxes")
                for (sub in subBoxes) {
                    val subObj = sub.asJsonObject
                    val type = subObj.get("type").asString

                    if (type == "Box") {
                        val scaleArr = subObj.getAsJsonArray("scale")
                        val scale = Vector3f(
                            scaleArr[0].asFloat,
                            scaleArr[1].asFloat,
                            scaleArr[2].asFloat
                        )
                        val box = BoxCollisionShape(scale)

                        val transArr = subObj.getAsJsonArray("translation")
                        val translation = Vector3f(
                            transArr[0].asFloat,
                            transArr[1].asFloat,
                            transArr[2].asFloat
                        )

                        compound.addChildShape(box, translation)
                    }
                }

                compound
            }
            else -> throw IllegalArgumentException("Unknown shape type: $shapeType")
        }

        val body = PhysicsRigidBody(shape, mass)
        body.friction = friction
        body.restitution = restitution
        body.userObject = userObject

        body.setPhysicsLocation(location)
        body.setPhysicsRotation(rotation)

        val velocityJson = this.getAsJsonObject("velocity")

        val linearArr = velocityJson.getAsJsonArray("linearVelocity")
        val angularArr = velocityJson.getAsJsonArray("angularVelocity")

        body.setLinearVelocity(
            Vector3f(
                linearArr[0].asFloat,
                linearArr[1].asFloat,
                linearArr[2].asFloat
            )
        )
        body.setAngularVelocity(
            Vector3f(
                angularArr[0].asFloat,
                angularArr[1].asFloat,
                angularArr[2].asFloat
            )
        )

        return body
    }
}