package com.wiyuka.phymodels.physics.separation

import com.jme3.bullet.collision.shapes.CompoundCollisionShape
import com.jme3.bullet.collision.shapes.infos.ChildCollisionShape
import com.wiyuka.phymodels.model.Model
import com.wiyuka.phymodels.model.getNeighbors
import org.joml.Vector3i
import com.wiyuka.phymodels.physics.objmanager.ObjectManager

class SeparateCalculations {
    companion object{
        private fun checkShape(){
            val needCheck = ObjectManager.livingModels.filter { it.value.needCheckShape }
            needCheck.forEach {
                val modelEntity = it.value
                val modelCollisionShape = it.value.rigidBody.collisionShape as CompoundCollisionShape

                val listChildren = modelCollisionShape.listChildren()
                val independents = countIndependentlyShape(listChildren.toList())
                independents.forEach { (index, shapes) ->
                    //re-calc shape center
                    val xMax = shapes.maxOf { it.x }
                    val xMin = shapes.minOf { it.x }
                    val yMax = shapes.maxOf { it.y }
                    val yMin = shapes.minOf { it.y }
                    val zMax = shapes.maxOf { it.z }
                    val zMin = shapes.minOf { it.z }

                }
            }
        }
        private fun countIndependentlyShape(children: List<ChildCollisionShape>): MutableMap<Int, List<Vector3i>> { //返回每个独立的形状
            val v3iMap = children.map {
                val offset = it.copyOffset(null)
                Vector3i(offset.x.toInt(), offset.y.toInt(), offset.z.toInt())
            }
            val independentlyShape = mutableMapOf<Int, List<Vector3i>>()
            var index = 0
            for (shapes in children) {
                val shapeLoc = shapes.copyOffset(null)
                val shapeLocV3i = Vector3i(shapeLoc.x.toInt(), shapeLoc.y.toInt(), shapeLoc.z.toInt())
                var has = false
                independentlyShape.forEach {
                    if(it.value.contains(shapeLocV3i)) has = true
                }
                if(has) continue
                independentlyShape[index] = (bfs(v3iMap, shapeLocV3i))
                index++
            }
            return independentlyShape
        }
        fun bfs(locationMatrix: List<Vector3i>, startPos: Vector3i): List<Vector3i> {
            val queue = ArrayDeque<Vector3i>()
            val visited = mutableSetOf<Vector3i>()
            val result = mutableListOf<Vector3i>()
            queue.add(startPos)
            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                if (current in visited) continue
                visited.add(current)
                result.add(current)
                // 检查相邻的块
                for (neighbor in current.getNeighbors()) if (neighbor in locationMatrix && neighbor !in visited)
                    queue.add(neighbor)
            }
            return result
        }
    }
}