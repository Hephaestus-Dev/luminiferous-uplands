package robosky.ether.world

import net.fabricmc.api.{EnvType, Environment}
import net.minecraft.util.math.{BlockPos, ChunkPos, Vec3d}
import net.minecraft.world.World
import net.minecraft.world.biome.source.BiomeSourceType
import net.minecraft.world.dimension.{Dimension, DimensionType}
import net.minecraft.world.gen.chunk.ChunkGenerator
import robosky.ether.world.biome.BiomeRegistry
import robosky.ether.world.gen.EtherChunkGenConfig

class EtherDimension(world: World, dimensionType: DimensionType) extends Dimension(world, dimensionType) {
  private val colorsSunriseSunset = new Array[Float](4)

  override def createChunkGenerator(): ChunkGenerator[_] = WorldRegistry.ETHER_CHUNK_GENERATOR.create(world,
    BiomeSourceType.FIXED.applyConfig(BiomeSourceType.FIXED.getConfig.setBiome(BiomeRegistry.ETHER_HIGHLANDS_BIOME)),
    EtherChunkGenConfig)

  override def getSpawningBlockInChunk(chunkPos: ChunkPos, b: Boolean): BlockPos = null

  override def getTopSpawningBlockPosition(i: Int, i1: Int, b: Boolean): BlockPos = null

  override def getSkyAngle(worldTime: Long, partialTicks: Float): Float = {
    val i = (worldTime % 24000L).toInt
    var f = (i.toFloat + partialTicks) / 24000.0F - 0.25F
    if (f < 0.0F) f += 1
    if (f > 1.0F) f -= 1
    val f1 = 1.0F - ((Math.cos(f.toDouble * 3.141592653589793D) + 1.0D) / 2.0D).toFloat
    f += (f1 - f) / 3.0F
    f
  }

  override def hasVisibleSky = true

  override def getFogColor(celestialAngle: Float, partialTicks: Float): Vec3d = {
    import net.minecraft.util.math.MathHelper
    var multiplier = MathHelper.cos(celestialAngle * 6.2831855F) * 2.0F + 0.5F

    multiplier = MathHelper.clamp(multiplier, 0.0F, 1.0F)

    var r = 0.6019608F
    var g = 0.6019608F
    var b = 0.627451F

    r *= multiplier * 0.94F + 0.06F
    g *= multiplier * 0.94F + 0.06F
    b *= multiplier * 0.91F + 0.09F

    new Vec3d(r.toDouble, g.toDouble, b.toDouble)
  }

  override def canPlayersSleep: Boolean = true

  override def shouldRenderFog(i: Int, i1: Int): Boolean = false

  override def getType: DimensionType = dimensionType

  import net.minecraft.util.math.MathHelper

  @Environment(EnvType.CLIENT)
  override def getBackgroundColor(celestialAngle: Float, partialTicks: Float): Array[Float] = {
    val f3 = MathHelper.cos(celestialAngle * 3.141593F * 2.0F) - 0.0F
    if (f3 >= -0.4F && f3 <= 0.4F) {
      val f5 = f3 / 0.4F * 0.5F + 0.5F
      var f6 = 1.0F - (1.0F - MathHelper.sin(f5 * 3.141593F)) * 0.99F
      f6 *= f6
      this.colorsSunriseSunset(0) = f5 * 0.3F + 0.1F
      this.colorsSunriseSunset(1) = f5 * f5 * 0.7F + 0.2F
      this.colorsSunriseSunset(2) = f5 * f5 * 0.7F + 0.2F
      this.colorsSunriseSunset(3) = f6
      return this.colorsSunriseSunset
    }
    null
  }
}