package robosky.ether

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.item.{Item, ItemGroup, ItemStack}
import net.minecraft.tag.{Tag, ItemTags}
import net.minecraft.util.Identifier
import robosky.ether.block.BlockRegistry
import robosky.ether.block.machine.{MachineRegistry, RecipeRegistry}
import robosky.ether.item.ItemRegistry
import robosky.ether.world.WorldRegistry
import robosky.ether.world.biome.BiomeRegistry
import robosky.ether.world.feature.FeatureRegistry

object UplandsMod extends ModInitializer {

  val GROUP: ItemGroup = FabricItemGroupBuilder
    .create(this :/ "general")
    .icon(() => new ItemStack(BlockRegistry.ETHER_GRASS))
    .build()

  val BOSSROOM_TECHNICAL_TAG: Tag[Item] = new ItemTags.CachingTag(this :/ "bossroom_technical")

  override def onInitialize(): Unit = {
    ItemRegistry.init()
    BlockRegistry.init()
    FeatureRegistry.init()
    BiomeRegistry.init()
    WorldRegistry.init()
    MachineRegistry.init()
    RecipeRegistry.init()
  }

  def :/(name: String) = new Identifier("luminiferous_uplands", name)
}
