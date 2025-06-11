package com.volatila.phymodels.model

import com.google.gson.GsonBuilder
import com.volatila.phymodels.physics.obj.toLocation
import com.volatila.phymodels.physics.obj.toVector3f
import com.volatila.phymodels.util.LocationUtil
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.plugin.Plugin
import java.io.File
import java.nio.file.Files
import javax.vecmath.Matrix4f
import javax.vecmath.Quat4f
import javax.vecmath.Vector3f

data class Model(
    val blocks: MutableMap<Vector3i, Material>, //储存相对于模型原点的每个方块的坐标和种类
    val mass: Float,
    val name: String
){
    data class Vector3i(var x: Int, var y: Int, var z: Int){
        override fun toString(): String {
            return "${x}_${y}_${z}"
        }
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Vector3i) return false
            return x == other.x && y == other.y && z == other.z
        }

        override fun hashCode(): Int {
            var result = x
            result = 31 * result + y
            result = 31 * result + z
            return result
        }

        fun sub(x: Int, y: Int, z: Int) : Vector3i{
            this.x -= x
            this.y -= y
            this.z -= z
            return this
        }

        fun sub(vector: Vector3i): Vector3i {
            this.x -= vector.x
            this.y -= vector.y
            this.z -= vector.z
            return this
        }

        fun add(x: Int, y: Int, z: Int): Vector3i {
            this.x += x
            this.y += y
            this.z += z
            return this
        }

        fun add(vector: Vector3i): Vector3i {
            this.x += vector.x
            this.y += vector.y
            this.z += vector.z
            return this
        }

        fun toVector3f(): Vector3f {
            return Vector3f(x.toFloat(), y.toFloat(), z.toFloat())
        }

        fun getNeighbors(): List<Vector3i> {
            val neighbors = ArrayList<Vector3i>()
            neighbors.add(Vector3i(this.x + 1, this.y, this.z))
            neighbors.add(Vector3i(this.x - 1, this.y, this.z))
            neighbors.add(Vector3i(this.x, this.y + 1, this.z))
            neighbors.add(Vector3i(this.x, this.y - 1, this.z))
            neighbors.add(Vector3i(this.x, this.y, this.z + 1))
            neighbors.add(Vector3i(this.x, this.y, this.z - 1))

            return neighbors
        }
    }
    companion object{
        class Vector3iSerializer : com.google.gson.JsonSerializer<Vector3i> {
            override fun serialize(src: Vector3i?, typeOfSrc: java.lang.reflect.Type?, context: com.google.gson.JsonSerializationContext?): com.google.gson.JsonElement {
                return com.google.gson.JsonPrimitive("${src?.x}_${src?.y}_${src?.z}")
            }
        }
        class Vector3iDeserializer : com.google.gson.JsonDeserializer<Vector3i> {
            override fun deserialize(json: com.google.gson.JsonElement?, typeOfT: java.lang.reflect.Type?, context: com.google.gson.JsonDeserializationContext?): Vector3i {
                val parts = json?.asString?.split("_") ?: return Vector3i(0, 0, 0)
                return Vector3i(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
            }
        }
        fun setup(plugin: Plugin){
            plugin.logger.info { "loading models..." }
            val startTime = System.currentTimeMillis()
            staticModels.addAll(getModels(plugin))
            val endTime = System.currentTimeMillis()
            plugin.logger.info { "loaded ${staticModels.size} models in ${endTime - startTime}ms" }
        }

        val staticModels = mutableListOf<Model>()
        fun modelFolder(plugin: Plugin): File {
            return File(plugin.dataFolder, "models").apply { if (!exists()) mkdirs() }
        }
        private val jsonDecoder = GsonBuilder().registerTypeAdapter(Vector3i::class.java, Vector3iSerializer()).setPrettyPrinting().registerTypeAdapter(Vector3i::class.java, Vector3iDeserializer()).create()
        private fun getModels(plugin: Plugin): List<Model>{

            val modelFolder = modelFolder(plugin)
            val result = mutableListOf<Model>()

            for (file in modelFolder.listFiles()) {
                val startTime = System.currentTimeMillis()
//                if(!file.name.endsWith(".json")) continue
                val instance = jsonDecoder.fromJson(Files.readString(file.toPath())
                    .replace("__SPACE__", " ")
                    .replace("__DOT__", ".")
                    .replace("__ADD__", "+")
                    .replace("__SUB__", "-")
                    .replace("__LEFT__BRACES", "(")
                    .replace("__RIGHT__BRACES", ")")
                , Model::class.java)
                result.add(instance)

                val endTime = System.currentTimeMillis()
                plugin.logger.info("Loaded model ${file.name} in ${endTime - startTime}ms")
            }
            return result
        }
        fun getModelByName(modelName: String): Model? = staticModels.find { it.name == modelName }
    }

    fun getSize(): Vector3i {
        if (blocks.isEmpty()) return Vector3i(0, 0, 0)

        return LocationUtil.getVector3iSize(blocks.keys.toSet())
    }

    /**
     * @param center 模型原点的位置
     * @return 模型的几何中心在世界上的位置
     */
    /**
     * 计算模型几何中心在世界坐标系中的位置
     * @param center 模型原点的世界坐标位置
     * @return 模型几何中心的世界坐标位置
     */
    fun getGeometricCenter(center: Location, scale: Float): Location {
        if (this.blocks.isEmpty()) {
            throw IllegalStateException("无法计算空模型的几何中心")
        }

        // 初始化各轴的最小值和最大值
        var minX = Double.MAX_VALUE
        var maxX = Double.MIN_VALUE
        var minY = Double.MAX_VALUE
        var maxY = Double.MIN_VALUE
        var minZ = Double.MAX_VALUE
        var maxZ = Double.MIN_VALUE

        // 单次遍历获取所有轴的极值
        this.blocks.keys.forEach { pos ->
            if (pos.x + 0.5 < minX) minX = pos.x + 0.5
            if (pos.x + 0.5 > maxX) maxX = pos.x + 0.5
            if (pos.y + 0.5 < minY) minY = pos.y + 0.5
            if (pos.y + 0.5 > maxY) maxY = pos.y + 0.5
            if (pos.z + 0.5 < minZ) minZ = pos.z + 0.5
            if (pos.z + 0.5 > maxZ) maxZ = pos.z + 0.5
        }

        // 计算相对坐标系的几何中心
        val xCenterRel = ((minX + maxX) / 2.0) * scale
        val yCenterRel = ((minY + maxY) / 2.0) * scale
        val zCenterRel = ((minZ + maxZ) / 2.0) * scale

        // 转换为世界坐标系：相对中心 + 模型原点位置
        return Location(
            center.world,
            xCenterRel + center.x,
            yCenterRel + center.y,
            zCenterRel + center.z
        )
    }

    fun getGeometricCenter(center: Location, scale: Float, rotation: Quat4f) {

    }
    /*
            如何通过几何中心反推模型的原点？
            1. 假设模型的原点在(0,0,0)
            2. 计算模型的几何中心在模型坐标系中的位置，例如计算出的几何中心是(0.5,0.5,0.5)
            3. 将几何中心作为模型的原点，计算旧的原点相对几何中心的偏移量，例如(0,0,0) - (0.5,0.5,0.5) = (-0.5,-0.5,-0.5)
            4. 将传入的模型原点(1, 1, 1)加上偏移量，得到返回的结果(0.5, 0.5, 0.5)，即在几何中心坐标为(1, 1, 1)时，模型的原点在(0.5, 0.5, 0.5)
             */

    fun getOriginByGeometricCenterRel(scale: Float): Location{
        val simGeometricCenter = getGeometricCenter(Location(Bukkit.getWorlds()[0], 0.0, 0.0, 0.0), scale)
        val offset = Location(simGeometricCenter.world, 0.0 - simGeometricCenter.x, 0.0 - simGeometricCenter.y, 0.0 - simGeometricCenter.z)
        val newX = (offset.x)
        val newY = (offset.y)
        val newZ = (offset.z)
        return Location(null, newX, newY, newZ)
    }
    
    fun getOriginByGeometricCenterWithRotation(geometricCenter: Location, rotation: Quat4f, scale: Float): Location{
        val locRel = getOriginByGeometricCenterRel(scale).toVector3f()
        Matrix4f().apply { set(rotation) }.transform(locRel)
        return locRel.toLocation(geometricCenter.world).add(geometricCenter)
    }
}