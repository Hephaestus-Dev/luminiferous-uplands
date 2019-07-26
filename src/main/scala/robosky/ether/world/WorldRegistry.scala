package robosky.ether.world

import java.util.function.Supplier

import com.github.draylar.dimension.api.{FabricChunkGeneratorFactory, FabricChunkGeneratorType, FabricDimensionType}
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import net.minecraft.world.biome.source.BiomeSource
import net.minecraft.world.gen.chunk.{ChunkGenerator, ChunkGeneratorConfig}
import robosky.ether.MixinHack
import robosky.ether.world.gen.{EtherChunkGenConfig, EtherChunkGenerator}

object WorldRegistry {
  val ETHER_CHUNK_GENERATOR: FabricChunkGeneratorType[EtherChunkGenConfig.type, EtherChunkGenerator] =
    registerChunkGeneratorType(new Identifier("ether_dim", "etherchunkgen"),
      new EtherChunkGenerator(_, _), () => EtherChunkGenConfig, appearsOnBuffet = false)

  val ETHER_DIMENSION: FabricDimensionType = registerDimensionType(EtherDimensionType)

  def init(): Unit = Unit

  private def registerChunkGeneratorType[C <: ChunkGeneratorConfig, T <: ChunkGenerator[C]](id: Identifier, factory: (World, BiomeSource) => T, supplier: () => C, appearsOnBuffet: Boolean): FabricChunkGeneratorType[C, T]
  = registerChunkGeneratorType2(id, (world: World, source: BiomeSource, _: C) => factory(world, source), () => supplier(), appearsOnBuffet)

  private def registerChunkGeneratorType2[C <: ChunkGeneratorConfig, T <: ChunkGenerator[C]](id: Identifier, factory: FabricChunkGeneratorFactory[C, T], supplier: Supplier[C], appearsOnBuffet: Boolean): FabricChunkGeneratorType[C, T] =
    Registry.register[FabricChunkGeneratorType[C, T]](Registry.CHUNK_GENERATOR_TYPE, id,
      new FabricChunkGeneratorType[C, T](factory, appearsOnBuffet, supplier))

  private def registerDimensionType(dimType: FabricDimensionType) = {
    val t = Registry.register[FabricDimensionType](Registry.DIMENSION, dimType.getNumericalID, dimType.getIdentifier.toString, dimType)
    MixinHack.ETHER_DIMTYPE = t
    t
  }
}