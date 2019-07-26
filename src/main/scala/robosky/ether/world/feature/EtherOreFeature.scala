package robosky.ether.world.feature

import java.util.Random

import com.mojang.datafixers
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.gen.chunk.{ChunkGenerator, ChunkGeneratorConfig}
import net.minecraft.world.gen.feature.{DefaultFeatureConfig, Feature}
import robosky.ether.block.{BlocksEther, EtherOreBlock}

import scala.util.control.Breaks._

object EtherOreFeature extends Feature[DefaultFeatureConfig]((t: datafixers.Dynamic[_]) => DefaultFeatureConfig.deserialize(t)) {
  var SPHERES: IndexedSeq[Clump] = (1 to 9).map(_.toFloat).map(Clump.of)

  def generate(world: IWorld, generator: ChunkGenerator[_ <: ChunkGeneratorConfig], rand: Random, pos: BlockPos, uselessConfig: DefaultFeatureConfig): Boolean = {
    val toGenerateIn: Chunk = world.getChunk(pos)
    for (s <- EtherOreBlock.oreTypes) {
      val settings: EtherOreBlock.OreGenSettings = s.genSettings()
      if (settings.ores.nonEmpty) {
        var clusters = settings.clusterCount
        if (clusters < 1) clusters = 1
        var blocksGenerated = 0
        for (_ <- 0 until clusters) {
          val overbleed = 0
          var radius = Math.log(settings.clusterSize).toInt + 1
          if (radius > 7) radius = 7
          breakable {
            for (j <- SPHERES.indices) {
              val clump = SPHERES(j)
              if (clump.size >= settings.clusterSize) {
                radius = j + 1
                break // FIXME: I AM SO SORRY
              }
            }
          }
          var clusterX = rand.nextInt(16 + overbleed - (radius * 2)) + radius
          var clusterZ = rand.nextInt(16 + overbleed - (radius * 2)) + radius
          var heightRange = settings.maxHeight - settings.minHeight
          if (heightRange < 1) heightRange = 1
          val clusterY = rand.nextInt(heightRange) + settings.minHeight
          clusterX += toGenerateIn.getPos.getStartX
          clusterZ += toGenerateIn.getPos.getStartZ
          val generatedThisCluster = generateVeinPartGaussianClump(world, clusterX, clusterY, clusterZ, settings.clusterSize, radius, settings.ores, 85, rand)
          blocksGenerated += generatedThisCluster
        }
      }
    }
    false
  }

  protected def generateVeinPartGaussianClump(world: IWorld, x: Int, y: Int, z: Int, clumpSize: Int, radius: Int, states: Set[BlockState], density: Int, rand: Random): Int = {
    val radIndex = radius - 1
    val clump = if (radIndex < SPHERES.length) SPHERES(radIndex).copy
    else Clump.of(radius)
    //int rad2 = radius * radius;
    val blocks = states.toArray
    var replaced = 0
    for (_ <- 0 until clump.size) {
      if (!clump.isEmpty) {
        val pos = clump.removeGaussian(rand, x, y, z)
        if (replace(world, pos.getX, pos.getY, pos.getZ, blocks, rand)) {
          replaced += 1
          if (replaced >= clumpSize) return replaced
        }
      }
    }
    replaced
  }

  def replace(world: IWorld, x: Int, y: Int, z: Int, states: Array[BlockState], rand: Random): Boolean = {
    val pos = new BlockPos(x, y, z)
    val toReplace = world.getBlockState(pos)
    val replaceWith = states(rand.nextInt(states.length))
    if (toReplace.getBlock != BlocksEther.ETHER_STONE) return false
    world.setBlockState(pos, replaceWith, 3)
    true
  }

  protected def generateVeinPart(world: IWorld, x: Int, y: Int, z: Int, clumpSize: Int, radius: Int, states: Set[BlockState], density: Int, rand: Random): Int = {
    val rad2 = radius * radius
    val blocks = states.toArray
    var replaced = 0
    for {
      zi <- (z - radius) to (z + radius)
      yi <- (y - radius) to (y + radius)
      xi <- (x - radius) to (x + radius)
      if yi >= 0 && yi <= 255
      if rand.nextInt(100) <= density
    } {
      val dx = xi - x
      val dy = yi - y
      val dz = zi - z
      val dist2 = dx * dx + dy * dy + dz * dz
      if (dist2 <= rad2) if (replace(world, xi, yi, zi, blocks, rand)) {
        replaced += 1
        if (replaced >= clumpSize) return replaced
      }
    }

    replaced
  }

  protected def generateVeinPartGaussian(world: IWorld, x: Int, y: Int, z: Int, clumpSize: Int, radius: Int, states: Set[BlockState], density: Int, rand: Random): Int = {
    val rad2 = radius * radius
    val blocks = states.toArray
    var replaced = 0
    for (_ <- 0 until 200) {
      val xi = (x + (rand.nextGaussian * radius)).asInstanceOf[Int]
      val yi = (y + (rand.nextGaussian * radius)).asInstanceOf[Int]
      val zi = (z + (rand.nextGaussian * radius)).asInstanceOf[Int]
      val dx = xi - x
      val dy = yi - y
      val dz = zi - z
      val dist2 = dx * dx + dy * dy + dz * dz
      if (dist2 <= rad2) if (replace(world, xi, yi, zi, blocks, rand)) {
        replaced += 1
        if (replaced >= clumpSize) return replaced
      }
    }
    replaced
  }
}