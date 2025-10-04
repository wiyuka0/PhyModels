package com.wiyuka.phymodels.virtualworld

import com.wiyuka.phymodels.PhyModels
import com.wiyuka.phymodels.util.LocationUtil
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.block.data.BlockData
import org.bukkit.generator.ChunkGenerator
import org.joml.Vector3i
import java.util.Random
import kotlin.system.measureTimeMillis

object VirtualWorld {
    private class VoidWorldGenerator(): ChunkGenerator() {
        override fun generateChunkData(world: World, random: Random, x: Int, z: Int, biome: BiomeGrid): ChunkData {
            return createChunkData(world)
        }
    }
    lateinit var virtualWorld: World
    fun initialize() {
        val worldCreator = WorldCreator("virtual_world")
        val generateCost = measureTimeMillis { worldCreator.generator(VoidWorldGenerator()) }
        val world = worldCreator.createWorld()
        if(world == null) throw NullPointerException("Virtual world was null.")
        virtualWorld = world
        PhyModels.logger.info("VirtualWorld initialization took $generateCost ms")
    }

    /**
     * @param loc1 Position 1 on world
     * @param loc2 Position 2 on world
     * @return Area start position on virtual world
     */
    fun copyStructure(loc1: Location, loc2: Location): Location {
        require(loc1.world == loc2.world) { "Virtual world does not have a corresponding location" }
        val blocks = LocationUtil.getBlocksInArea(loc1, loc2)
        return loc1
    }

    fun getBlockData(location: Location): BlockData{
        location.world = virtualWorld

        return location.block.blockData
    }

    private var allocated = 0
    fun allocate(size: Vector3i): Location {
        return Location(virtualWorld, allocated * 100.0, 0.0, 0.0)
        allocated++
    }
}